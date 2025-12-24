package org.dromara.dbswitch.product.sr;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.sync.AutoCastTableDataSynchronizeProvider;

@Slf4j
public class StarrocksTableDataSynchronizer extends AutoCastTableDataSynchronizeProvider {

  private List<String> fieldNames;
  private final CloseableDataSource dataSource;

  private final StarRocksUtils starRocksUtils = new StarRocksUtils();

  public StarrocksTableDataSynchronizer(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
    dataSource = (CloseableDataSource) factoryProvider.getDataSource();
  }

  @Override
  public void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks) {
    this.fieldNames = fieldNames;
    super.prepare(schemaName, tableName, fieldNames, pks);
    try {
      starRocksUtils.init(schemaName, tableName, dataSource);
    } catch (Exception e) {
      log.warn("Failed to init by StarRocksUtils#init(),information:: {}", e.getMessage());
    }
  }

  @Override
  public long executeInsert(List<Object[]> recordValues) {
    try {
      return starRocksUtils.addOrUpdateData(fieldNames, recordValues);
    } catch (Exception e) {
      log.warn("Failed to addOrUpdateData by StarRocksUtils#addOrUpdateData(),information:: {}", e.getMessage());
      return super.executeInsert(recordValues);
    }
  }

  @Override
  public long executeUpdate(List<Object[]> recordValues) {
    try {
      return starRocksUtils.addOrUpdateData(fieldNames, recordValues);
    } catch (Exception e) {
      log.warn("Failed to addOrUpdateData by StarRocksUtils#addOrUpdateData(),information:: {}", e.getMessage());
      return super.executeUpdate(recordValues);
    }
  }

}
