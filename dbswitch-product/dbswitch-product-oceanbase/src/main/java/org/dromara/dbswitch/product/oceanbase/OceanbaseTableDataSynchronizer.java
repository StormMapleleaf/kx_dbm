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
import org.dromara.dbswitch.product.oracle.OracleTableDataSynchronizer;
import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.sync.DefaultTableDataSynchronizeProvider;
import org.dromara.dbswitch.core.provider.sync.TableDataSynchronizeProvider;
import java.util.List;

public class OceanbaseTableDataSynchronizer extends DefaultTableDataSynchronizeProvider {

  private final TableDataSynchronizeProvider delegate;
  private final ProductTypeEnum dialect;

  public OceanbaseTableDataSynchronizer(ProductFactoryProvider factoryProvider, TableDataSynchronizeProvider delegate) {
    super(factoryProvider);
    this.delegate = delegate;
    if (delegate instanceof OracleTableDataSynchronizer) {
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
  public void prepare(String schemaName, String tableName, List<String> fieldNames, List<String> pks) {
    this.delegate.prepare(schemaName, tableName, fieldNames, pks);
  }

  @Override
  public long executeInsert(List<Object[]> records) {
    return this.delegate.executeInsert(records);
  }

  @Override
  public long executeUpdate(List<Object[]> records) {
    return this.delegate.executeUpdate(records);
  }

  @Override
  public long executeDelete(List<Object[]> records) {
    return this.delegate.executeDelete(records);
  }
}
