package org.dromara.dbswitch.product.oracle;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class OracleTableManageProvider extends DefaultTableManageProvider {

  public OracleTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE \"%s\".\"%s\" CASCADE CONSTRAINTS", schemaName, tableName);
    this.executeSql(sql);
  }

}
