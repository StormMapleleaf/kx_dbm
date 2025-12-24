package org.dromara.dbswitch.core.provider.transform;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import java.util.List;


public class DefaultTransformProvider implements RecordTransformProvider {

  private ProductFactoryProvider factoryProvider;

  public DefaultTransformProvider(ProductFactoryProvider factoryProvider) {
    this.factoryProvider = factoryProvider;
  }

  protected ProductFactoryProvider getFactoryProvider() {
    return this.factoryProvider;
  }

  @Override
  public String getTransformerName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public Object[] doTransform(String schema, String table, List<String> fieldNames, Object[] recordValue) {
    return recordValue;
  }

}
