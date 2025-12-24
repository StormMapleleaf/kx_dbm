// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.oceanbase;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.annotation.Product;
import org.dromara.dbswitch.core.features.DefaultProductFeatures;
import org.dromara.dbswitch.core.features.ProductFeatures;
import org.dromara.dbswitch.core.provider.AbstractFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;
import org.dromara.dbswitch.core.provider.meta.MetadataProvider;
import org.dromara.dbswitch.core.provider.query.TableDataQueryProvider;
import org.dromara.dbswitch.core.provider.sync.AutoCastTableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.write.AutoCastTableDataWriteProvider;
import org.dromara.dbswitch.core.provider.write.TableDataWriteProvider;
import org.dromara.dbswitch.product.mysql.MysqlFeatures;
import org.dromara.dbswitch.product.mysql.MysqlMetadataQueryProvider;
import org.dromara.dbswitch.product.oracle.OracleMetadataQueryProvider;
import org.dromara.dbswitch.product.oracle.OracleTableDataSynchronizer;
import org.dromara.dbswitch.product.oracle.OracleTableDataWriteProvider;
import org.dromara.dbswitch.product.oracle.OracleTableManageProvider;

@Slf4j
@Product(ProductTypeEnum.OCEANBASE)
public class OceanbaseFactoryProvider extends AbstractFactoryProvider {

  private Boolean isMySqlMode;

  public OceanbaseFactoryProvider(DataSource dataSource) {
    super(dataSource);

    try (Connection connection = getDataSource().getConnection()) {
      this.isMySqlMode = OceanbaseUtils.isOceanBaseUseMysqlMode(connection);
      if (log.isDebugEnabled()) {
        log.debug("#### Target OceanBase is {} Mode ", this.isMySqlMode ? "MySQL" : "Oracle");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ProductFeatures getProductFeatures() {
    return isMySqlMode ? new MysqlFeatures() : new DefaultProductFeatures();
  }

  @Override
  public MetadataProvider createMetadataQueryProvider() {
    MetadataProvider provider = isMySqlMode
        ? new MysqlMetadataQueryProvider(this)
        : new OracleMetadataQueryProvider(this);
    return new OceanbaseMetadataQueryProvider(this, provider);
  }

  @Override
  public TableDataQueryProvider createTableDataQueryProvider() {
    return new OceanbaseTableDataQueryProvider(this, isMySqlMode);
  }

  @Override
  public TableDataWriteProvider createTableDataWriteProvider(boolean useInsert) {
    TableDataWriteProvider provider = isMySqlMode
        ? new AutoCastTableDataWriteProvider(this)
        : new OracleTableDataWriteProvider(this);
    return new OceanbaseTableDataWriteProvider(this, provider);
  }

  @Override
  public TableManageProvider createTableManageProvider() {
    TableManageProvider provider = isMySqlMode
        ? new DefaultTableManageProvider(this)
        : new OracleTableManageProvider(this);
    return new OceanbaseTableManageProvider(this, provider);
  }

  @Override
  public TableDataSynchronizeProvider createTableDataSynchronizeProvider() {
    TableDataSynchronizeProvider provider = isMySqlMode
        ? new AutoCastTableDataSynchronizeProvider(this)
        : new OracleTableDataSynchronizer(this);
    return new OceanbaseTableDataSynchronizer(this, provider);
  }
}
