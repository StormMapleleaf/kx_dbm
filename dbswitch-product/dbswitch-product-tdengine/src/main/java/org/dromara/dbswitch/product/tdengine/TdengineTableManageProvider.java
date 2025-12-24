package org.dromara.dbswitch.product.tdengine;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;

public class TdengineTableManageProvider extends DefaultTableManageProvider {

  public TdengineTableManageProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    String sql = String.format("DELETE FROM %s.%s ",
        schemaName, tableName);
    this.executeSql(sql);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    String sql = String.format("DROP TABLE %s.%s ",
        schemaName, tableName);
    this.executeSql(sql);
  }

}
