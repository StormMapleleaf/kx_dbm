package org.dromara.dbswitch.product.elasticsearch;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.TableDescription;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElasticsearchMetadataQueryProvider extends AbstractMetadataProvider {

  protected ElasticsearchMetadataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    return null;
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    return null;
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    return Collections.emptyList();
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    List<ColumnDescription> ret = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData().getColumns(null, schemaName, tableName, null);) {
      while (rs.next()) {
        ColumnDescription cd = new ColumnDescription();
        cd.setFieldName(rs.getString("COLUMN_NAME"));
        cd.setLabelName(rs.getString("COLUMN_NAME"));
        cd.setFieldType(Integer.parseInt(rs.getString("DATA_TYPE")));
        cd.setFieldTypeName(rs.getString("TYPE_NAME"));
        cd.setFiledTypeClassName(rs.getString("TYPE_NAME"));
        cd.setDisplaySize(Integer.parseInt(rs.getString("COLUMN_SIZE")));
        cd.setPrecisionSize(0);
        cd.setScaleSize(0);
        cd.setAutoIncrement(false);
        cd.setNullable(true);
        cd.setProductType(getProductType());
        ret.add(cd);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return ret;
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    try {
      List<String> schemas = querySchemaList(connection);
      connection.getMetaData().getTables(null, schemas.get(0), null, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName, String tableName) {
    return Collections.emptyList();
  }

  @Override
  public List<IndexDescription> queryTableIndexes(Connection connection, String schemaName, String tableName) {
    return Collections.emptyList();
  }

  @Override
  public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
    return String.format("%s.%s", schemaName, tableName);
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr,
      boolean withRemarks) {
    return null;
  }

  @Override
  public String getPrimaryKeyAsString(List<String> pks) {
    return null;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td, List<ColumnDescription> cds) {
    return Collections.emptyList();
  }
}
