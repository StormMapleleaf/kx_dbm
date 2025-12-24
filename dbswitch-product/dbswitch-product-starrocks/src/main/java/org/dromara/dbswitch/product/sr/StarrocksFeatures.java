package org.dromara.dbswitch.product.sr;

import org.dromara.dbswitch.core.features.ProductFeatures;

public class StarrocksFeatures implements ProductFeatures {

  @Override
  public int convertFetchSize(int fetchSize) {
    return Integer.MIN_VALUE;
  }

}
