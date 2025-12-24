package org.dromara.dbswitch.product.hive;

import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import javax.sql.DataSource;

@Product(ProductTypeEnum.HIVE)
public class HiveFactoryProvider extends AbstractFactoryProvider {

  public HiveFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return new HiveFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new HiveMetadataQueryProvider(this);
  }

  @Override
  public TableDataQueryProvider createTableDataQueryProvider() {
    return new HiveTableDataQueryProvider(this);
  }

}
