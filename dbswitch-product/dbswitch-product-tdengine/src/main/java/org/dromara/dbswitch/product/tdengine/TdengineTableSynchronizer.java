package org.dromara.dbswitch.product.tdengine;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.sync.AutoCastTableDataSynchronizeProvider;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TdengineTableSynchronizer extends AutoCastTableDataSynchronizeProvider {

  public TdengineTableSynchronizer(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  protected TransactionDefinition getDefaultTransactionDefinition() {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    return definition;
  }

}
