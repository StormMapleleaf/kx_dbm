package org.dromara.dbswitch.product.db2;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class DB2TableManageProvider extends DefaultTableManageProvider {

  public DB2TableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String sql = String.format("TRUNCATE TABLE \"%s\".\"%s\" IMMEDIATE ", schemaName, tableName);
    this.executeSql(sql);
  }
  
}
