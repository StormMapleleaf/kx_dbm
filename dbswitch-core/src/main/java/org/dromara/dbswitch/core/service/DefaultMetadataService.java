package org.dromara.dbswitch.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.ProductProviderFactory;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.ColumnValue;
import org.dromara.dbswitch.core.schema.IndexDescription;
import org.dromara.dbswitch.core.schema.SchemaTableData;
import org.dromara.dbswitch.core.schema.SchemaTableMeta;
import org.dromara.dbswitch.core.schema.SourceProperties;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.util.GenerateSqlUtils;

public class DefaultMetadataService implements MetadataService {

  private DataSource dataSource;
  private ProductFactoryProvider factoryProvider;
  private MetadataProvider metaQueryProvider;
  private TableDataQueryProvider dataQueryProvider;

  public DefaultMetadataService(DataSource dataSource, ProductTypeEnum type) {
    this.dataSource = dataSource;
    this.factoryProvider = ProductProviderFactory
        .newProvider(type, dataSource);
    this.metaQueryProvider = factoryProvider.createMetadataQueryProvider();
    this.dataQueryProvider = factoryProvider.createTableDataQueryProvider();
  }

  @Override
  public void close() {
    if (dataSource instanceof AutoCloseable) {
      try {
        ((AutoCloseable) dataSource).close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public List<String> querySchemaList() {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.querySchemaList(connection);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<TableDescription> queryTableList(String schemaName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.queryTableList(connection, schemaName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getTableDDL(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.getTableDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getTableRemark(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      TableDescription td = metaQueryProvider.queryTableMeta(connection, schemaName, tableName);
      return null == td ? null : td.getRemarks();
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public String getViewDDL(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.getViewDDL(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> queryTableColumnName(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.queryTableColumnName(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.queryTableColumnMeta(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<ColumnDescription> querySqlColumnMeta(String querySql) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.querySelectSqlColumnMeta(connection, querySql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.queryTablePrimaryKeys(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<IndexDescription> queryTableIndexes(String schemaName, String tableName) {
    try (Connection connection = dataSource.getConnection()) {
      return metaQueryProvider.queryTableIndexes(connection, schemaName, tableName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableMeta queryTableMeta(String schemaName, String tableName) {
    SchemaTableMeta tableMeta = new SchemaTableMeta();

    try (Connection connection = dataSource.getConnection()) {
      TableDescription tableDesc = metaQueryProvider.queryTableMeta(connection, schemaName, tableName);
      if (null == tableDesc) {
        throw new IllegalArgumentException("Table Or View Not Exist");
      }

      List<ColumnDescription> columns = metaQueryProvider.queryTableColumnMeta(
          connection, schemaName, tableName);

      List<String> pks;
      String createSql;
      List<IndexDescription> indexes;
      if (tableDesc.isViewTable()) {
        pks = Collections.emptyList();
        createSql = metaQueryProvider.getViewDDL(connection, schemaName, tableName);
        indexes = Collections.emptyList();
      } else {
        pks = metaQueryProvider.queryTablePrimaryKeys(connection, schemaName, tableName);
        createSql = metaQueryProvider.getTableDDL(connection, schemaName, tableName);
        indexes = metaQueryProvider.queryTableIndexes(connection, schemaName, tableName);
      }

      tableMeta.setSchemaName(schemaName);
      tableMeta.setTableName(tableName);
      tableMeta.setTableType(tableDesc.getTableType());
      tableMeta.setRemarks(tableDesc.getRemarks());
      tableMeta.setColumns(columns);
      tableMeta.setPrimaryKeys(pks);
      tableMeta.setCreateSql(createSql);
      tableMeta.setIndexes(indexes);

      return tableMeta;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public SchemaTableData queryTableData(String schemaName, String tableName, int rowCount) {
    try (Connection connection = dataSource.getConnection()) {
      return dataQueryProvider.queryTableData(connection, schemaName, tableName, rowCount);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public ColumnValue queryIncrementPoint(String schemaName, String tableName, String filedName) {
    try (Connection connection = dataSource.getConnection()) {
      return dataQueryProvider.queryFieldMaxValue(connection, schemaName, tableName, filedName);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public void testQuerySQL(String sql) {
    try (Connection connection = dataSource.getConnection()) {
      metaQueryProvider.testQuerySQL(connection, sql);
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

  @Override
  public List<String> getDDLCreateTableSQL(MetadataProvider provider,
      List<ColumnDescription> fieldNames, List<String> primaryKeys, String schemaName,
      String tableName, String tableRemarks, boolean autoIncr, SourceProperties tblProperties) {
    return GenerateSqlUtils.getDDLCreateTableSQL(
        provider, fieldNames, primaryKeys, schemaName, tableName, tableRemarks, autoIncr, tblProperties);
  }

}
