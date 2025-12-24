package org.dromara.dbswitch.product.dm;

import org.dromara.dbswitch.common.consts.Constants;
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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class DmMetadataQueryProvider extends AbstractMetadataProvider {

  private static final String SHOW_CREATE_TABLE_SQL =
      "SELECT DBMS_METADATA.GET_DDL('TABLE','%s','%s') FROM DUAL ";
  private static final String SHOW_CREATE_VIEW_SQL =
      "SELECT DBMS_METADATA.GET_DDL('VIEW','%s','%s') FROM DUAL ";


  public DmMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_TABLE_SQL, tableName, schemaName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    String sql = String.format(SHOW_CREATE_VIEW_SQL, tableName, schemaName);
    try (Statement st = connection.createStatement()) {
      if (st.execute(sql)) {
        try (ResultSet rs = st.getResultSet()) {
          if (rs != null && rs.next()) {
            return rs.getString(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return null;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    String querySQL = String.format(
        "SELECT * from (%s) tmp where ROWNUM<=1 ",
        sql.replace(";", ""));
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

    StringBuilder retval = new StringBuilder(128);
    retval.append(" \"").append(fieldname).append("\"    ");

    int type = v.getType();
    switch (type) {
      case ColumnMetaData.TYPE_TIMESTAMP:
      case ColumnMetaData.TYPE_TIME:
        retval.append("TIMESTAMP");
        break;
      case ColumnMetaData.TYPE_DATE:
        retval.append("DATE");
        break;
      case ColumnMetaData.TYPE_BOOLEAN:
        retval.append("BIT");
        break;
      case ColumnMetaData.TYPE_NUMBER:
      case ColumnMetaData.TYPE_BIGNUMBER:
        if (null != pks && !pks.isEmpty() && pks.contains(fieldname)) {
          retval.append("BIGINT");
        } else {
          retval.append("NUMERIC");
          if (length > 0) {
            if (length > 38) {
              length = 38;
            }

            retval.append('(').append(length);
            if (precision > 0) {
              retval.append(", ").append(precision);
            }
            retval.append(')');
          }
        }
        break;
      case ColumnMetaData.TYPE_INTEGER:
        retval.append("BIGINT");
        break;
      case ColumnMetaData.TYPE_STRING:
        if (null != pks && pks.contains(fieldname)) {
          retval.append("NVARCHAR(" + length + ")");
        } else if (length > 0 && length < 1900) {
                              retval.append("NVARCHAR(").append(length).append(')');
        } else {
          retval.append("TEXT");
        }
        break;
      case ColumnMetaData.TYPE_BINARY:
        retval.append("BLOB");
        break;
      default:
        retval.append("CLOB");
        break;
    }

    if (addCr) {
      retval.append(Constants.CR);
    }

    return retval.toString();
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

}
