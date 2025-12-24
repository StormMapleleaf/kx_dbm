package org.dromara.dbswitch.core.features;

public class DefaultProductFeatures implements ProductFeatures {

  @Override
  public int convertFetchSize(int fetchSize) {
    return fetchSize;
  }
}
