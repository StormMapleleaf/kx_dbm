// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.sybase;

import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.util.GenerateSqlUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SybaseMetadataQueryProvider extends AbstractMetadataProvider {

  private static final String SHOW_CREATE_VIEW_SQL = "SELECT sc.text FROM sysobjects so, syscomments sc WHERE user_name(so.uid)=? AND so.name=? and sc.id = so.id ORDER BY sc.colid";

  private static Set<String> excludesSchemaNames;

  static {
    excludesSchemaNames = new HashSet<>();
    excludesSchemaNames.add("keycustodian_role");
    excludesSchemaNames.add("ha_role");
    excludesSchemaNames.add("replication_role");
    excludesSchemaNames.add("sa_role");
    excludesSchemaNames.add("usedb_user");
    excludesSchemaNames.add("replication_maint_role_gp");
    excludesSchemaNames.add("sybase_ts_role");
    excludesSchemaNames.add("dtm_tm_role");
    excludesSchemaNames.add("sso_role");
    excludesSchemaNames.add("navigator_role");
    excludesSchemaNames.add("sa_serverprivs_role");
    excludesSchemaNames.add("probe");
    excludesSchemaNames.add("mon_role");
    excludesSchemaNames.add("webservices_role");
    excludesSchemaNames.add("js_admin_role");
    excludesSchemaNames.add("js_user_role");
    excludesSchemaNames.add("messaging_role");
    excludesSchemaNames.add("js_client_role");
    excludesSchemaNames.add("oper_role");
    excludesSchemaNames.add("hadr_admin_role_gp");
  }

  public SybaseMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    try (ResultSet schemas = connection.getMetaData().getSchemas(connection.getCatalog(), null)) {
      Set<String> ret = new LinkedHashSet<>();
      while (schemas.next()) {
        ret.add(schemas.getString("TABLE_SCHEM"));
      }
      return ret.stream().filter(s -> !excludesSchemaNames.contains(s)).collect(Collectors.toList());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> ret = new ArrayList<>();
    Set<String> uniqueSet = new LinkedHashSet<>();
    String[] types = new String[]{"TABLE", "VIEW"};
    try (ResultSet tables = connection.getMetaData()
        .getTables(connection.getCatalog(), schemaName, "%", types)) {
      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");
        if (uniqueSet.contains(tableName)) {
          continue;
        } else {
          uniqueSet.add(tableName);
        }

        TableDescription td = new TableDescription();
        td.setSchemaName(schemaName);
        td.setTableName(tableName);
        td.setRemarks(tables.getString("REMARKS"));
        td.setTableType(tables.getString("TABLE_TYPE").toUpperCase());
        ret.add(td);
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> queryTableColumnName(Connection connection, String schemaName, String tableName) {
    Set<String> columns = new LinkedHashSet<>();
    try (ResultSet rs = connection.getMetaData()
        .getColumns(connection.getCatalog(), schemaName, tableName, null)) {
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"));
      }
      return new ArrayList<>(columns);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName,
      String tableName) {
    Set<String> ret = new LinkedHashSet<>();
    try (ResultSet primaryKeys = connection.getMetaData()
        .getPrimaryKeys(connection.getCatalog(), schemaName, tableName)) {
      while (primaryKeys.next()) {
        ret.add(primaryKeys.getString("COLUMN_NAME"));
      }
      return new ArrayList<>(ret);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    String sql = this.getTableFieldsQuerySQL(schemaName, tableName);
    List<ColumnDescription> ret = this.querySelectSqlColumnMeta(connection, sql);
    // 补充一下注释信息
    try (ResultSet columns = connection.getMetaData()
        .getColumns(connection.getCatalog(), schemaName, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String remarks = columns.getString("REMARKS");
        for (ColumnDescription cd : ret) {
          if (columnName.equals(cd.getFieldName())) {
            cd.setRemarks(remarks);
          }
        }
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    List<ColumnDescription> columnDescriptions = queryTableColumnMeta(connection, schemaName, tableName);
    List<String> pks = queryTablePrimaryKeys(connection, schemaName, tableName);
    return GenerateSqlUtils.getDDLCreateTableSQL(this,
        columnDescriptions, pks, schemaName, tableName, false);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    try (PreparedStatement ps = connection.prepareStatement(SHOW_CREATE_VIEW_SQL)) {
      ps.setString(1, schemaName);
      ps.setString(2, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        StringBuilder sql = new StringBuilder();
        while (rs.next()) {
          sql.append(rs.getString(1));
        }
        return sql.toString();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format("SELECT TOP 1 * from (%s) tmp ", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
  }

  @Override
  protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
    return String.format("select top 1 * from [%s].[%s] ", schemaName, tableName);
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    String testQuerySql = String.format("SELECT top 1 * from ( %s ) tmp", sql.replace(";", ""));
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

    String retval = " " + ProductTypeEnum.SYBASE.quoteName(fieldname) + " ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
      case ColumnMetaData.TYPE_TIME:
      case ColumnMetaData.TYPE_DATE:
        retval += "DATETIME";
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          retval += " NOT NULL";
        }
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "TINYINT";
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          retval += " NOT NULL";
        }
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            retval += "INTEGER IDENTITY NOT NULL";
          } else {
            retval += "INTEGER NOT NULL";
          }
        } else {
          if (precision != 0 || (precision == 0 && length > 9)) {
            if (precision > 0 && length > 0) {
              retval += "DECIMAL(" + length + ", " + precision + ") NULL";
            } else {
              retval += "DOUBLE PRECISION NULL";
            }
          } else {
            if (length < 3) {
              retval += "TINYINT NULL";
            } else if (length < 5) {
              retval += "SMALLINT NULL";
            } else {
              retval += "INTEGER NULL";
            }
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length >= 2048) {
          retval += "TEXT NULL";
        } else {
          retval += "VARCHAR";
          if (length > 0) {
            retval += "(" + length + ")";
          }
          if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
            retval += " NOT NULL";
          } else {
            retval += " NULL";
          }
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "VARBINARY";
        break;
      default:
        retval += "TEXT NULL";
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
    return Collections.emptyList();
  }

}
