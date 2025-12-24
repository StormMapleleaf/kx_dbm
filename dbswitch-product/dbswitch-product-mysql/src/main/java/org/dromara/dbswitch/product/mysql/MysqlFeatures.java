package org.dromara.dbswitch.product.mysql;

import org.dromara.dbswitch.core.features.ProductFeatures;

public class MysqlFeatures implements ProductFeatures {

  @Override
  public int convertFetchSize(int fetchSize) {
    return Integer.MIN_VALUE;
  }

}
