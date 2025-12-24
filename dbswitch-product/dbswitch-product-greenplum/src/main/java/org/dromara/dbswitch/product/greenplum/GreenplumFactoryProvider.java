package org.dromara.dbswitch.product.greenplum;

import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.features.DefaultProductFeatures;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.product.postgresql.PostgresTableCopyWriteProvider;
import org.dromara.dbswitch.product.postgresql.PostgresTableManageProvider;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.sync.AutoCastTableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import javax.sql.DataSource;

@Product(ProductTypeEnum.GREENPLUM)
public class GreenplumFactoryProvider extends AbstractFactoryProvider {

  public GreenplumFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return new DefaultProductFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new GreenplumMetadataQueryProvider(this);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    return new PostgresTableManageProvider(this);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return useInsert
        ? new AutoCastTableDataWriteProvider(this)
        : new PostgresTableCopyWriteProvider(this);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new AutoCastTableDataSynchronizeProvider(this);
  }
}
