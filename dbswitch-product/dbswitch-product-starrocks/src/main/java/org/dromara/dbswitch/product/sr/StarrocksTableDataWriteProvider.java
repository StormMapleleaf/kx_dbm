// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.sr;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;

@Slf4j
public class StarrocksTableDataWriteProvider extends AutoCastTableDataWriteProvider {

  private final CloseableDataSource dataSource;
  private final StarRocksUtils starRocksUtils = new StarRocksUtils();

  public StarrocksTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
    dataSource = (CloseableDataSource) factoryProvider.getDataSource();
  }

  @Override
  public void prepareWrite(String schemaName, String tableName, List<String> fieldNames) {
    super.prepareWrite(schemaName, tableName, fieldNames);
    try {
      starRocksUtils.init(schemaName, tableName, dataSource);
    } catch (Exception e) {
      log.warn("Failed to init by StarRocksUtils#init(),information: {}", e.getMessage());
    }
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    try {
      return starRocksUtils.addOrUpdateData(fieldNames, recordValues);
    } catch (Exception e) {
      log.warn("Failed to insertOrUpdate data by StarRocksUtils#addOrUpdateData(),information: {}", e.getMessage());
      return super.write(fieldNames, recordValues);
    }
  }

}
