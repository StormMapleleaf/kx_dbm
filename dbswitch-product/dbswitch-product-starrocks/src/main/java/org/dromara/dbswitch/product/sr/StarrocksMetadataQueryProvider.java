// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.sr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.SourceProperties;
import org.dromara.dbswitch.core.schema.TableDescription;

/**
 * https://docs.starrocks.io/zh/docs/3.1/quick_start/Create_table/
 * <p>
 * https://docs.starrocks.io/zh/docs/3.1/quick_start/deploy_with_docker/
 */
@Slf4j
public class StarrocksMetadataQueryProvider extends AbstractMetadataProvider {

  private static final String SHOW_CREATE_TABLE_SQL = "SHOW CREATE TABLE `%s`.`%s` ";
  private static final String SHOW_CREATE_VIEW_SQL = "SHOW CREATE VIEW `%s`.`%s` ";
  private static final String QUERY_TABLE_LIST_SQL =
      "SELECT `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_TYPE`,`TABLE_COMMENT` "
          + "FROM `information_schema`.`TABLES` WHERE `TABLE_SCHEMA`= ? ";
  private static final String QUERY_TABLE_METADATA_SQL =
      "SELECT `TABLE_COMMENT`,`TABLE_TYPE` FROM `information_schema`.`TABLES` "
          + "WHERE `TABLE_SCHEMA` = ? AND `TABLE_NAME` = ?";
  private static final String QUERY_TABLE_PRIMARY_KEY_SQL = " SELECT COLUMN_NAME \n" +
      " from information_schema.columns\n" +
      " where TABLE_SCHEMA=? and TABLE_NAME=?\n" +
      " and TABLE_CATALOG is null\n" +
      " and COLUMN_KEY = 'PRI'";

  public StarrocksMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    List<String> result = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getCatalogs()) {
      while (rs.next()) {
        Optional.ofNullable(rs.getString(1)).ifPresent(result::add);
      }
      return result.stream().distinct().collect(Collectors.toList());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    List<TableDescription> result = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(QUERY_TABLE_LIST_SQL)) {
      ps.setString(1, schemaName);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          TableDescription td = new TableDescription();
          td.setSchemaName(rs.getString("TABLE_SCHEMA"));
          td.setTableName(rs.getString("TABLE_NAME"));
          td.setRemarks(rs.getString("TABLE_COMMENT"));
          String tableType = rs.getString("TABLE_TYPE");
          if (tableType.equalsIgnoreCase("VIEW")) {
            td.setTableType("VIEW");
          } else {
            td.setTableType("TABLE");
          }

          result.add(td);
        }

        return result;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TableDescription queryTableMeta(Connection connection, String schemaName, String tableName) {
    try (PreparedStatement ps = connection.prepareStatement(QUERY_TABLE_METADATA_SQL)) {
      ps.setString(1, schemaName);
      ps.setString(2, tableName);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          TableDescription td = new TableDescription();
          td.setSchemaName(schemaName);
          td.setTableName(tableName);
          td.setRemarks(rs.getString(1));

          String tableType = rs.getString(2);
          if (tableType.equalsIgnoreCase("VIEW")) {
            td.setTableType("VIEW");
          } else {
            td.setTableType("TABLE");
          }

          return td;
        }
        return null;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> queryTableColumnName(Connection connection, String schemaName, String tableName) {
    List<String> columns = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData()
        .getColumns(schemaName, null, tableName, null)) {
      while (rs.next()) {
        columns.add(rs.getString("COLUMN_NAME"));
      }
      return columns.stream().distinct().collect(Collectors.toList());
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
        .getColumns(schemaName, null, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String remarks = columns.getString("REMARKS");
        String columnDefault = columns.getString("COLUMN_DEF");
        for (ColumnDescription cd : ret) {
          if (columnName.equals(cd.getFieldName())) {
            cd.setRemarks(remarks);
            cd.setProductType(ProductTypeEnum.STARROCKS);
            // 补充默认值信息
            cd.setDefaultValue(columnDefault);
          }
        }
      }
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName, String tableName) {
    try (PreparedStatement statement = connection.prepareStatement(QUERY_TABLE_PRIMARY_KEY_SQL)) {
      statement.setString(1, schemaName);
      statement.setString(2, tableName);
      try (ResultSet primaryKeys = statement.executeQuery()) {
        List<String> ret = new ArrayList<>();
        while (primaryKeys.next()) {
          ret.add(primaryKeys.getString(1));
        }
        return ret.stream().distinct().collect(Collectors.toList());
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized List<IndexDescription> queryTableIndexes(Connection connection, String schemaName,
      String tableName) {
    setCatalogName(schemaName);
    return super.queryTableIndexes(connection, schemaName, tableName);
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    List<String> result = new ArrayList<>();
    try (Statement st = connection.createStatement()) {
      if (st.execute(String.format(SHOW_CREATE_TABLE_SQL, schemaName, tableName))) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null) {
            while (rs.next()) {
              String value = rs.getString(2);
              Optional.ofNullable(value).ifPresent(result::add);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return result.stream().findAny().orElse(null);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    List<String> result = new ArrayList<>();
    try (Statement st = connection.createStatement()) {
      if (st.execute(String.format(SHOW_CREATE_VIEW_SQL, schemaName, tableName))) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null) {
            while (rs.next()) {
              String value = rs.getString(2);
              Optional.ofNullable(value).ifPresent(result::add);
            }
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return result.stream().findAny().orElse(null);
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(" %s LIMIT 0,1", sql.replace(";", ""));
    return this.getSelectSqlColumnMeta(connection, querySQL);
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

    String retval = " `" + fieldname + "`  ";

    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
        retval += "DATETIME";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "DATETIME";
        break;
      case ColumnMetaData.TYPE_DATE:
        retval += "DATE";
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval += "TINYINT";
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_INTEGER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          if (useAutoInc) {
            //see: https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/table_bucket_part_index/CREATE_TABLE/#column_definition
            //fix: AUTO_INCREMENT should be after not null
            retval += "BIGINT NOT NULL AUTO_INCREMENT";
          } else {
            retval += "BIGINT NOT NULL";
          }
        } else {
          // Integer values...
          if (precision == 0) {
            if (length > 9) {
              if (length < 19) {
                // can hold signed values between -9223372036854775808 and 9223372036854775807
                // 18 significant digits
                retval += "BIGINT";
              } else {
                retval += "DECIMAL(" + (length > 38 ? 38 : length) + ")";
              }
            } else {
              retval += "INT";
            }
          } else {
            // Floating point values...
            if (length > 15) {
              int p = (length > 38 ? 38 : length);
              retval += "DECIMAL(" + p;
              if (precision > 0) {
                retval += ", " + (precision > p ? p : precision);
              }
              retval += ")";
            } else {
              // A double-precision floating-point number is accurate to approximately 15
              // decimal places.
              // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
              retval += "DOUBLE";
            }
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        //see: https://docs.starrocks.io/zh/docs/category/string/
        long newLength = length * 3;
        if (newLength < 255) {
          retval += "VARCHAR(" + newLength + ")";
        } else if (newLength <= 65533) {
          retval += "STRING";
        } else if (newLength <= 1048576) {
          retval += "VARCHAR(" + newLength + ")";
        } else {
          retval += "VARCHAR(1048576)";
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "VARBINARY ";
        break;
      default:
        retval += "VARCHAR(1048576)";
        break;
    }

    if (withRemarks && StringUtils.isNotBlank(v.getRemarks())) {
      retval += String.format(" COMMENT '%s' ", v.getRemarks().replace("'", "\\'"));
    }

    if (addCr) {
      retval += Constants.CR;
    }

    return retval;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td, List<ColumnDescription> cds) {
    return Collections.emptyList();
  }

  @Override
  public void appendPrimaryKeyForCreateTableSql(StringBuilder builder, List<String> primaryKeys) {
    // StarRocks主键需要在postAppendCreateTableSql函数里组装
  }

  @Override
  public void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties) {
    // https://docs.mirrorship.cn/zh/docs/table_design/table_types/primary_key_table/
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      String pk = getPrimaryKeyAsString(primaryKeys);
      builder.append("PRIMARY KEY (").append(pk).append(")");
      builder.append("\n DISTRIBUTED BY HASH(").append(pk).append(")");
    }
  }
}
