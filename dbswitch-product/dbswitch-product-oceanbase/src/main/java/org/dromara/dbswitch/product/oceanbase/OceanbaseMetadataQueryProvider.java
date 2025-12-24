package org.dromara.dbswitch.product.oceanbase;

import cn.hutool.core.text.StrPool;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.product.mysql.MysqlMetadataQueryProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.AbstractMetadataProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnMetaData;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.SourceProperties;
import org.dromara.dbswitch.core.schema.TableDescription;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class OceanbaseMetadataQueryProvider extends AbstractMetadataProvider {

  private final MetadataProvider delegate;
  private final ProductTypeEnum dialect;

  public OceanbaseMetadataQueryProvider(ProductFactoryProvider factoryProvider, MetadataProvider delegate) {
    super(factoryProvider);
    this.delegate = delegate;
    if (this.delegate instanceof MysqlMetadataQueryProvider) {
      this.dialect = ProductTypeEnum.MYSQL;
    } else {
      this.dialect = ProductTypeEnum.ORACLE;
    }
  }

  @Override
  protected String quoteName(String name) {
    return this.dialect.quoteName(name);
  }

  @Override
  protected String quoteSchemaTableName(String schemaName, String tableName) {
    return this.dialect.quoteSchemaTableName(schemaName, tableName);
  }

  @Override
  public List<String> querySchemaList(Connection connection) {
    return this.delegate.querySchemaList(connection);
  }

  @Override
  public List<TableDescription> queryTableList(Connection connection, String schemaName) {
    return this.delegate.queryTableList(connection, schemaName);
  }

  @Override
  public TableDescription queryTableMeta(Connection connection, String schemaName, String tableName) {
    return this.delegate.queryTableMeta(connection, schemaName, tableName);
  }

  @Override
  public String getTableDDL(Connection connection, String schemaName, String tableName) {
    return this.delegate.getTableDDL(connection, schemaName, tableName);
  }

  @Override
  public String getViewDDL(Connection connection, String schemaName, String tableName) {
    return this.delegate.getViewDDL(connection, schemaName, tableName);
  }

  @Override
  public List<String> queryTableColumnName(Connection connection, String schemaName,
      String tableName) {
    return this.delegate.queryTableColumnName(connection, schemaName, tableName);
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(Connection connection, String schemaName,
      String tableName) {
    return this.delegate.queryTableColumnMeta(connection, schemaName, tableName);
  }

  @Override
  public List<ColumnDescription> querySelectSqlColumnMeta(Connection connection, String sql) {
    return this.delegate.querySelectSqlColumnMeta(connection, sql);
  }

  @Override
  public List<String> queryTablePrimaryKeys(Connection connection, String schemaName, String tableName) {
    return this.delegate.queryTablePrimaryKeys(connection, schemaName, tableName);
  }

  @Override
  public List<IndexDescription> queryTableIndexes(Connection connection, String schemaName, String tableName) {
    return this.delegate.queryTableIndexes(connection, schemaName, tableName);
  }

  @Override
  public void testQuerySQL(Connection connection, String sql) {
    if (StringUtils.equals(ProductTypeEnum.OCEANBASE.getSql(), sql)) {
      this.delegate.testConnection(connection, this.dialect.getSql());
    } else {
      this.delegate.testConnection(connection, sql);
    }
  }

  @Override
  public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
    return quoteSchemaTableName(schemaName, tableName);
  }

  @Override
  public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean useAutoInc, boolean addCr,
      boolean withRemarks) {
    return this.delegate.getFieldDefinition(v, pks, useAutoInc, addCr, withRemarks);
  }

  @Override
  public void preAppendCreateTableSql(StringBuilder builder) {
    this.delegate.preAppendCreateTableSql(builder);
  }

  @Override
  public void appendPrimaryKeyForCreateTableSql(StringBuilder builder, List<String> primaryKeys) {
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      String primaryKeyAsString = getPrimaryKeyAsString(primaryKeys);
      builder.append(", PRIMARY KEY (").append(primaryKeyAsString).append(")");
    }
  }

  @Override
  public void postAppendCreateTableSql(StringBuilder builder, String tblComment, List<String> primaryKeys,
      SourceProperties tblProperties) {
    this.delegate.postAppendCreateTableSql(builder, tblComment, primaryKeys, tblProperties);
  }

  @Override
  public String getPrimaryKeyAsString(List<String> pks) {
    if (!pks.isEmpty()) {
      return quoteName(
          StringUtils.join(
              pks.stream().distinct().collect(Collectors.toList())
              , quoteName(StrPool.COMMA)
          )
      );
    }
    return StringUtils.EMPTY;
  }

  @Override
  public List<String> getTableColumnCommentDefinition(TableDescription td, List<ColumnDescription> cds) {
    return this.delegate.getTableColumnCommentDefinition(td, cds);
  }

  @Override
  public List<String> getCreateTableSqlList(List<ColumnDescription> fieldNames, List<String> primaryKeys,
      String schemaName, String tableName, String tableRemarks, boolean autoIncr, SourceProperties tblProperties) {
    return this.delegate
        .getCreateTableSqlList(fieldNames, primaryKeys, schemaName, tableName, tableRemarks, autoIncr, tblProperties);
  }
}
