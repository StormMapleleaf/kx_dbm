package org.dromara.dbswitch.product.tdengine;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TDengineTableDataWriteProvider extends AutoCastTableDataWriteProvider {

  public TDengineTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  protected TransactionDefinition getDefaultTransactionDefinition() {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    return definition;
  }

}
