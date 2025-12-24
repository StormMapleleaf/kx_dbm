package org.dromara.dbswitch.product.elasticsearch;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.sync.DefaultTableDataSynchronizeProvider;
import java.util.List;

public class ElasticsearchTableDataSynchronizer extends DefaultTableDataSynchronizeProvider {

  public ElasticsearchTableDataSynchronizer(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks) {
  }

  @Override
  public long executeInsert(List<Object[]> records) {
    return 0;
  }

  @Override
  public long executeUpdate(List<Object[]> records) {
    return 0;
  }

  @Override
  public long executeDelete(List<Object[]> records) {
    return 0;
  }

}
