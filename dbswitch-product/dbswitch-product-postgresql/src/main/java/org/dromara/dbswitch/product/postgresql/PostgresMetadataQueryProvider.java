// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.postgresql;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.util.DDLFormatterUtils;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.TableDescription;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
public class PostgresMetadataQueryProvider extends AbstractMetadataProvider {

  protected static Set<String> systemSchemas = new HashSet<>();

  private static final String SHOW_CREATE_VIEW_SQL_1 =
      "SELECT pg_get_viewdef((select pg_class.relfilenode from pg_catalog.pg_class \n"
          + "join pg_catalog.pg_namespace on pg_class.relnamespace = pg_namespace.oid \n"
          + "where pg_namespace.nspname='%s' and pg_class.relname ='%s'),true) ";
  private static final String SHOW_CREATE_VIEW_SQL_2 =
      "select pg_get_viewdef('\"%s\".\"%s\"', true)";
  private static final String SHOW_SUB_PARTITIONED_TABLE = "SELECT c.relname AS table_name \n"
      + "FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace JOIN pg_inherits i ON c.oid = i.inhrelid \n"
      + "WHERE n.nspname = '%s'";

  static {
    systemSchemas.add("pg_temp");
    systemSchemas.add("information_schema");
    systemSchemas.add("pg_catalog");
    systemSchemas.add("pg_bitmapindex");
    systemSchemas.add("pg_toast");
    systemSchemas.add("partman");
    systemSchemas.add("repack");
  }

  public PostgresMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    List<String> schemas = super.querySchemaList(connection);
    return schemas.stream()
        .filter(s -> !systemSchemas.contains(s))
        .collect(Collectors.toList());
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> tableList = super.queryTableList(connection, schemaName);
    Set<String> partitionedTable = getPartitionedTable(connection, schemaName);
    return tableList.stream().filter(t -> !partitionedTable.contains(t.getTableName()))
        .collect(Collectors.toList());
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    String sql = PostgresqlConst.CREATE_TABLE_SQL_TPL
        .replace(PostgresqlConst.TPL_KEY_SCHEMA, schemaName)
        .replace(PostgresqlConst.TPL_KEY_TABLE, tableName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return DDLFormatterUtils.format(rs.getString(1));
          }
        }
      }
    } catch (SQLException e) {
      //throw new RuntimeException(e);
    }

    // 低版本的PostgreSQL的表的DDL获取方法
    String ddlSql = PostgresUtils.getTableDDL(this, connection, schemaName, tableName);
    return DDLFormatterUtils.format(ddlSql);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL_1, schemaName, tableName);
    try (Statement st = connection.createStatement()) {
      try {
        if (st.execute(sql)) {
          try (ResultSet rs = st.getResultSet()) {
            if (rs != null && rs.next()) {
              return rs.getString(1);
            }
          }
        }
      } catch (SQLException se) {
        sql = String.format(SHOW_CREATE_VIEW_SQL_2, schemaName, tableName);
        if (st.execute(sql)) {
          try (ResultSet rs = st.getResultSet()) {
            if (rs != null && rs.next()) {
              return rs.getString(1);
            }
          }
        }
      }
    } catch (SQLException e) {
      // throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0 ", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("SELECT * FROM \"%s\".\"%s\"  ", schemaName, tableName);
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    String testQuerySql = String.format("explain %s", sql.replace(";", ""));
    if (log.isDebugEnabled()) {
      log.debug("Execute sql :{}", testQuerySql);
    }
    try (Statement st = connection.createStatement()) {
      st.execute(testQuerySql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc,
      boolean addCr, boolean withRemarks) {
    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    String retval = " \"" + fieldname + "\"   ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "TIME";
        break;
      case ColumnMetaData.TYPE_DATE:
        retval += "DATE";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "BOOLEAN";
        if (v.isHaveDefault()) {
          boolean b = Boolean.getBoolean(v.getDefaultValue());
          if (b) {
            retval += " DEFAULT true";
          } else {
            retval += " DEFAULT false";
          }
        }
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            retval += "BIGSERIAL";
          } else {
            retval += "BIGINT";
          }
        } else {
          if (length > 0) {
            if (precision > 0 || length > 18) {
              if ((length + precision) > 0 && precision > 0) {
                // Numeric(Precision, Scale): Precision = total length; Scale = decimal places
                retval += "NUMERIC(" + (length + precision) + ", " + precision + ")";
              } else {
                retval += "DOUBLE PRECISION";
              }
              if (v.isHaveDefault()) {
                retval += " DEFAULT " + NumberUtils.toDouble(v.getDefaultValue());
              }
            } else {
              if (length > 9) {
                retval += "BIGINT";
              } else {
                if (length < 5) {
                  retval += "SMALLINT";
                } else {
                  retval += "INTEGER";
                }
              }
              if (v.isHaveDefault()) {
                retval += " DEFAULT " + NumberUtils.toInt(v.getDefaultValue());
              }
            }

          } else {
            retval += "DOUBLE PRECISION";
            if (v.isHaveDefault()) {
              retval += " DEFAULT " + NumberUtils.toDouble(v.getDefaultValue());
            }
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length < 1 || length >= Constants.CLOB_LENGTH) {
          retval += "TEXT";
        } else {
          if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
            retval += "VARCHAR(" + length + ")";
          } else {
            retval += "VARCHAR(" + length + ")";
          }
        }
        if (v.isHaveDefault()) {
          retval += " DEFAULT " + "'" + v.getDefaultValue() + "'";
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "BYTEA";
        break;
      default:
        retval += "TEXT";
        break;
    }

    if (addCr) {
      retval += Constants.CR;
    }

    return retval;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td,
      List<ColumnDescription> cds) {
    List<String> results = new ArrayList<>();
    if (StringUtils.isNotBlank(td.getRemarks())) {
      results.add(String
          .format("COMMENT ON TABLE \"%s\".\"%s\" IS '%s' ",
              td.getSchemaName(), td.getTableName(),
              td.getRemarks().replace("'", "")));
    }

    for (ColumnDescription cd : cds) {
      if (StringUtils.isNotBlank(cd.getRemarks())) {
        results.add(String
            .format("COMMENT ON COLUMN \"%s\".\"%s\".\"%s\" IS '%s' ",
                td.getSchemaName(), td.getTableName(), cd.getFieldName(),
                cd.getRemarks().replace("'", "")));
      }
    }

    return results;
  }

  protected Set<String> getPartitionedTable(Connection connection, String schemaName) {
    String query = String.format(SHOW_SUB_PARTITIONED_TABLE, schemaName);
    Set<String> partitionedTable = new HashSet<>();
    try (Statement st = connection.createStatement()) {
      ResultSet resultSet = st.executeQuery(query);
      while (resultSet.next()) {
        partitionedTable.add(resultSet.getString(1));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return partitionedTable;
  }

}
