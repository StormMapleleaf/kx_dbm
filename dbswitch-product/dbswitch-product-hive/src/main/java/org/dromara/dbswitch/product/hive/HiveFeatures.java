package org.dromara.dbswitch.product.hive;

import org.dromara.dbswitch.core.features.DefaultProductFeatures;

public class HiveFeatures extends DefaultProductFeatures {


  public boolean useCTAS() {
    return false;
  }
}
