package org.dromara.dbswitch.product.sqlite;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SqliteTableDataWriteProvider extends AutoCastTableDataWriteProvider {

  public SqliteTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  protected TransactionDefinition getDefaultTransactionDefinition() {
    DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
    definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    return definition;
  }

}
