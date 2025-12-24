// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.hive;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.query.DefaultTableDataQueryProvider;
import java.sql.Connection;
import java.sql.SQLException;

public class HiveTableDataQueryProvider extends DefaultTableDataQueryProvider {

  public HiveTableDataQueryProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  protected void beforeExecuteQuery(Connection connection, String schema, String table) {
    try {
      HivePrepareUtils.prepare(connection, schema, table);
    } catch (SQLException t) {
      throw new RuntimeException(t);
    }
  }

}
