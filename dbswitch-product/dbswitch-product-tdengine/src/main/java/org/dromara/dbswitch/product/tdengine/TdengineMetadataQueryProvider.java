package org.dromara.dbswitch.product.tdengine;

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
import org.apache.commons.lang3.StringUtils;
import org.dromara.dbswitch.common.consts.Constants;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.SourceProperties;
import org.dromara.dbswitch.core.schema.TableDescription;

@Slf4j
public class TdengineMetadataQueryProvider extends AbstractMetadataProvider {

  private static final String SHOW_CREATE_TABLE_SQL = "SHOW CREATE TABLE `%s`.`%s` ";
  private static final String SHOW_CREATE_VIEW_SQL = "SHOW CREATE VIEW `%s`.`%s` ";
  private static final String QUERY_STABLE_LIST_SQL =
      "SELECT `db_name`,`stable_name` as table_name,'TABLE' as `type`,`table_comment` "
          + "FROM `information_schema`.`ins_stables` WHERE `db_name`= ? ";

  private static final String QUERY_TABLE_LIST_SQL =
      "SELECT `db_name`,`table_name`,`type`,`table_comment` "
          + "FROM `information_schema`.`ins_tables` WHERE `db_name`= ? ";
  private static final String QUERY_TABLE_METADATA_SQL =
      "SELECT `table_comment`,`type` FROM `information_schema`.`ins_tables` "
          + "WHERE `db_name` = ? AND `table_name` = ?";

  public TdengineMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
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
    try (PreparedStatement ps = connection.prepareStatement(QUERY_STABLE_LIST_SQL)) {
      ps.setString(1, schemaName);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          TableDescription td = new TableDescription();
          td.setSchemaName(rs.getString("db_name"));
          td.setTableName(rs.getString("table_name"));
          td.setRemarks(rs.getString("table_comment"));
          String tableType = rs.getString("type");
          if (tableType.equalsIgnoreCase("VIEW")) {
            td.setTableType("VIEW");
          } else {
            td.setTableType("TABLE");
          }

          result.add(td);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    try (PreparedStatement ps = connection.prepareStatement(QUERY_TABLE_LIST_SQL)) {
      ps.setString(1, schemaName);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          TableDescription td = new TableDescription();
          td.setSchemaName(rs.getString("db_name"));
          td.setTableName(rs.getString("table_name"));
          td.setRemarks(rs.getString("table_comment"));
          String tableType = rs.getString("type");
          if (tableType.equalsIgnoreCase("VIEW")) {
            td.setTableType("VIEW");
          } else {
            td.setTableType("TABLE");
          }

          result.add(td);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return result;
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

        try (ResultSet columns = connection.getMetaData()
        .getColumns(schemaName, null, tableName, null)) {
      while (columns.next()) {
        String columnName = columns.getString("COLUMN_NAME");
        String remarks = columns.getString("REMARKS");
        String columnDefault = columns.getString("COLUMN_DEF");
        for (ColumnDescription cd : ret) {
          if (columnName.equals(cd.getFieldName())) {
            cd.setRemarks(remarks);
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
    List<String> ret = new ArrayList<>();
    try (ResultSet primaryKeys = connection.getMetaData()
        .getPrimaryKeys(schemaName, null, tableName)) {
      while (primaryKeys.next()) {
        ret.add(primaryKeys.getString("COLUMN_NAME"));
      }
      return ret.stream().distinct().collect(Collectors.toList());
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
        retval += "TIMESTAMP";
        break;
      case ColumnMetaData.TYPE_TIME:
        retval += "TIME";
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
            retval += "BIGINT AUTO_INCREMENT NOT NULL";
          } else {
            retval += "BIGINT NOT NULL";
          }
        } else {
                    if (precision == 0) {
            if (length > 9) {
              if (length < 19) {
                                                retval += "BIGINT";
              } else {
                retval += "DECIMAL(" + length + ")";
              }
            } else {
              retval += "INT";
            }
          } else {
                        if (length > 65) {
              length = 65;
            }
            if (length > 15) {
              retval += "DECIMAL(" + length;
              if (precision > 0) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
                                                        retval += "DOUBLE";
            }
          }
        }
        break;
      case ColumnMetaData.TYPE_STRING:
        if (length > 0) {
          if (length == 1) {
            retval += "CHAR(1)";
          } else if (length < 256) {
            retval += "VARCHAR(" + length + ")";
          } else if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {

            retval += "VARCHAR(254) BINARY";
          } else if (length < 65536) {
            retval += "TEXT";
          } else if (length < 16777216) {
            retval += "MEDIUMTEXT";
          } else {
            retval += "LONGTEXT";
          }
        } else {
          retval += "TINYTEXT";
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval += "LONGBLOB";
        break;
      default:
        retval += "LONGTEXT";
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
  public void preAppendCreateTableSql(StringBuilder builder) {
      }

  @Override
  public void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties) {
    if (StringUtils.isNotBlank(tblComment)) {
      builder.append(String.format(" COMMENT='%s' ", tblComment.replace("'", "\\'")));
    }
  }

  @Override
  public void appendPrimaryKeyForCreateTableSql(StringBuilder builder, List<String> primaryKeys) {
      }
}
