package org.dromara.dbswitch.product.elasticsearch;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class ElasticsearchTableManageProvider extends DefaultTableManageProvider {

  public ElasticsearchTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    cleanup(schemaName, tableName);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    cleanup(schemaName, tableName);
  }

  private void cleanup(String schemaName, String tableName) {
      }
}
