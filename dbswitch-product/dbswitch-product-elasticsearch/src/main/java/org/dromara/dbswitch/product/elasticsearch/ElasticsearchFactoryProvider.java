package org.dromara.dbswitch.product.elasticsearch;

import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.features.DefaultProductFeatures;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import javax.sql.DataSource;

@Product(ProductTypeEnum.ELASTICSEARCH)
public class ElasticsearchFactoryProvider extends AbstractFactoryProvider {

  public ElasticsearchFactoryProvider(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return new DefaultProductFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    return new ElasticsearchMetadataQueryProvider(this);
  }

  @Override
  public TableDataQueryProvider createTableDataQueryProvider() {
    return new ElasticsearchTableDataQueryProvider(this);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new ElasticsearchTableDataWriteProvider(this);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    return new ElasticsearchTableManageProvider(this);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new ElasticsearchTableDataSynchronizer(this);
  }
}
