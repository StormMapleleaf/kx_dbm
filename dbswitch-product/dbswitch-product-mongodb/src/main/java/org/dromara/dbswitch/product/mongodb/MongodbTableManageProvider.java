package org.dromara.dbswitch.product.mongodb;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class MongodbTableManageProvider extends DefaultTableManageProvider {

  public MongodbTableManageProvider(ProductFactoryProvider factoryProvider) {
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
    String sql = String.format("%s.getCollection('%s').drop();", schemaName, tableName);
    this.executeSql(sql);
  }
}
