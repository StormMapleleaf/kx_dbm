package org.dromara.dbswitch.core.provider.meta;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.schema.SourceProperties;
import java.sql.Connection;
import java.util.List;


public interface MetadataProvider {


  ProductTypeEnum getProductType();

  void testConnection(Connection connection, String pingSql);

  List<String> querySchemaList(Connection connection);

  List<TableDescription> queryTableList(Connection connection, String schemaName);

  TableDescription queryTableMeta(Connection connection, String schemaName, String tableName);

  String getTableDDL(Connection connection, String schemaName, String tableName);

  String getViewDDL(Connection connection, String schemaName, String tableName);

  List<String> queryTableColumnName(Connection connection, String schemaName,
      String tableName);

  List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName);

  List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql);

  List<String> queryTablePrimaryKeys(Connection connection, String schemaName, String tableName);

  List<IndexDescription> queryTableIndexes(Connection connection, String schemaName, String tableName);

  void testQuerySQL(Connection connection, String sql);

  String getQuotedSchemaTableCombination(String schemaName, String tableName);

  String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr,
      boolean withRemarks);

  void preAppendCreateTableSql(StringBuilder builder);


  void appendPrimaryKeyForCreateTableSql(StringBuilder builder, List<String> primaryKeys);

  void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties);

  String getPrimaryKeyAsString(List<String> pks);

  List<String> getTableColumnCommentDefinition(TableDescription td, List<ColumnDescription> cds);

  default List<String> getCreateTableSqlList(List<ColumnDescription> fieldNames, List<String> primaryKeys,
      String schemaName, String tableName, String tableRemarks, boolean autoIncr, SourceProperties tblProperties) {
    throw new UnsupportedOperationException("Unsupported function!");
  }
}
