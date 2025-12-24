package org.dromara.dbswitch.product.sybase;

import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.features.DefaultProductFeatures;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.sync.AutoCastTableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import javax.sql.DataSource;

@Product(ProductTypeEnum.SYBASE)
public class SybaseFactoryProvider extends AbstractFactoryProvider {

  public SybaseFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return new DefaultProductFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new SybaseMetadataQueryProvider(this);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new AutoCastTableDataWriteProvider(this);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    return new DefaultTableManageProvider(this);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new AutoCastTableDataSynchronizeProvider(this);
  }

}
