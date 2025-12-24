package org.dromara.dbswitch.core.service;

import java.util.List;
import javax.sql.DataSource;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnValue;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.SchemaTableData;
import org.dromara.dbswitch.core.schema.SchemaTableMeta;
import org.dromara.dbswitch.core.schema.SourceProperties;
import org.dromara.dbswitch.core.schema.TableDescription;

public interface MetadataService {


  void close();

  DataSource getDataSource();

  List<String> querySchemaList();

  List<TableDescription> queryTableList(String schemaName);

  String getTableDDL(String schemaName, String tableName);

  String getTableRemark(String schemaName, String tableName);

  String getViewDDL(String schemaName, String tableName);

  List<String> queryTableColumnName(String schemaName, String tableName);

  List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);

  List<ColumnDescription> querySqlColumnMeta(String querySql);

  List<String> queryTablePrimaryKeys(String schemaName, String tableName);

  List<IndexDescription> queryTableIndexes(String schemaName, String tableName);


  void testQuerySQL(String sql);

 
  SchemaTableMeta queryTableMeta(String schemaName, String tableName);


  SchemaTableData queryTableData(String schemaName, String tableName, int rowCount);


  ColumnValue queryIncrementPoint(String schemaName, String tableName, String filedName);

  List<String> getDDLCreateTableSQL(MetadataProvider provider, List<ColumnDescription> fieldNames,
      List<String> primaryKeys, String schemaName, String tableName, String tableRemarks,
      boolean autoIncr, SourceProperties tblProperties);
}
