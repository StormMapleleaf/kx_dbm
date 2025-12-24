package org.dromara.dbswitch.product.sqlite;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class SqliteTableManageProvider extends DefaultTableManageProvider {

  public SqliteTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String sql = String.format("DELETE FROM \"%s\".\"%s\" ", schemaName, tableName);
    this.executeSql(sql);

    try {
      sql = String.format("DELETE FROM sqlite_sequence WHERE name = '%s' ", tableName);
      this.executeSql(sql);
    } catch (Exception e) {
          }

  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE \"%s\".\"%s\" ", schemaName, tableName);
    this.executeSql(sql);
  }

}
