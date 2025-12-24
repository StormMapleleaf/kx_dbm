package org.dromara.dbswitch.product.doris;

import org.dromara.dbswitch.core.features.ProductFeatures;

public class DorisFeatures implements ProductFeatures {

  @Override
  public int convertFetchSize(int fetchSize) {
    return Integer.MIN_VALUE;
  }

}
