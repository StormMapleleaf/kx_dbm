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

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.product.oracle.OracleTableManageProvider;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.manage.DefaultTableManageProvider;
import org.dromara.dbswitch.core.provider.manage.TableManageProvider;

public class OceanbaseTableManageProvider extends DefaultTableManageProvider {

  private final TableManageProvider delegate;
  private final ProductTypeEnum dialect;

  public OceanbaseTableManageProvider(ProductFactoryProvider factoryProvider, TableManageProvider delegate) {
    super(factoryProvider);
    this.delegate = delegate;
    if (delegate instanceof OracleTableManageProvider) {
      this.dialect = ProductTypeEnum.ORACLE;
    } else {
      this.dialect = ProductTypeEnum.MYSQL;
    }
  }

  @Override
  protected String quoteName(String name) {
    return this.dialect.quoteName(name);
  }

  @Override
  protected String quoteSchemaTableName(String schemaName, String tableName) {
    return this.dialect.quoteSchemaTableName(schemaName, tableName);
  }

  @Override
  public void truncateTableData(String schemaName, String tableName) {
    this.delegate.truncateTableData(schemaName, tableName);
  }

  @Override
  public void dropTable(String schemaName, String tableName) {
    this.delegate.dropTable(schemaName, tableName);
  }
}
