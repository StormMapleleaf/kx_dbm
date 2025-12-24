package org.dromara.dbswitch.product.postgresql;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresTableManageProvider extends DefaultTableManageProvider {

  public PostgresTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String sql = String.format("TRUNCATE TABLE \"%s\".\"%s\" RESTART IDENTITY ",
        schemaName, tableName);
    this.executeSql(sql);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE \"%s\".\"%s\" CASCADE ",
        schemaName, tableName);
    this.executeSql(sql);
  }
}
