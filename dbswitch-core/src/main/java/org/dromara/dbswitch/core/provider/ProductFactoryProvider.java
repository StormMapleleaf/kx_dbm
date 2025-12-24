package org.dromara.dbswitch.core.provider;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.DefaultTableDataQueryProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.provider.sync.DefaultTableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.transform.MappedTransformProvider;
import org.dromara.dbswitch.core.provider.transform.RecordTransformProvider;
import org.dromara.dbswitch.core.provider.write.DefaultTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import javax.sql.DataSource;

public interface ProductFactoryProvider {

  ProductTypeEnum getProductType();

  DataSource getDataSource();

  ProductFeatures getProductFeatures();

  MetadataProvider createMetadataQueryProvider();

  default TableDataQueryProvider createTableDataQueryProvider() {
    return new DefaultTableDataQueryProvider(this);
  }

  default RecordTransformProvider createRecordTransformProvider() {
    return new MappedTransformProvider(this);
  }

  default TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    return new DefaultTableDataWriteProvider(this);
  }

  default TableManageProvider createTableManageProvider() {
    return new DefaultTableManageProvider(this);
  }

  default TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    return new DefaultTableDataSynchronizeProvider(this);
  }

}
