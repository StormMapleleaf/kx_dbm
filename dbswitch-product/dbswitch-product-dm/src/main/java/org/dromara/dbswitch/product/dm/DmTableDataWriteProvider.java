package org.dromara.dbswitch.product.dm;

import org.dromara.dbswitch.common.util.ObjectCastUtils;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.write.DefaultTableDataWriteProvider;
import java.util.List;

public class DmTableDataWriteProvider extends DefaultTableDataWriteProvider {

  public DmTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    recordValues.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        row[i] = ObjectCastUtils.castByDetermine(row[i]);
      }
    });
    return super.write(fieldNames, recordValues);
  }
}
