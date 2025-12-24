package org.dromara.dbswitch.product.tdengine;

import javax.sql.DataSource;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.core.features.DefaultProductFeatures;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;

@Product(ProductTypeEnum.TDENGINE)
public class TdengineFactoryProvider extends AbstractFactoryProvider {

  public TdengineFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return new DefaultProductFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new TdengineMetadataQueryProvider(this);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new TDengineTableDataWriteProvider(this);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new TdengineTableSynchronizer(this);
  }

  @Override
  public TableDataQueryProvider createTableDataQueryProvider() {
    return new TdengineTableDataQueryProvider(this);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    return new TdengineTableManageProvider(this);
  }

}
