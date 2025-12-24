package org.dromara.dbswitch.product.openguass;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class OpenGaussTableManageProvider extends DefaultTableManageProvider {

  public OpenGaussTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE \"%s\".\"%s\" CASCADE ",
        schemaName, tableName);
    this.executeSql(sql);
  }

}
