// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.oracle;

import org.dromara.dbswitch.core.provider.ProductFactoryProvider;
import org.dromara.dbswitch.core.provider.sync.DefaultTableDataSynchronizeProvider;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OracleTableDataSynchronizer extends DefaultTableDataSynchronizeProvider {

  public OracleTableDataSynchronizer(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public long executeInsert(List<Object[]> records) {
    List<InputStream> iss = new ArrayList<>();
    records.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        int jdbcType = this.columnType.get(this.fieldOrders.get(i));
        row[i] = OracleCastUtils.castByJdbcType(jdbcType, row[i], iss);
      }
    });

    try {
      return super.executeInsert(records);
    } finally {
      iss.forEach(is -> {
        try {
          is.close();
        } catch (Exception ignore) {
        }
      });
    }
  }

  @Override
  public long executeUpdate(List<Object[]> records) {
    List<InputStream> iss = new ArrayList<>();
    records.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        int jdbcType = this.columnType.get(this.fieldOrders.get(i));
        row[i] = OracleCastUtils.castByJdbcType(jdbcType, row[i], iss);
      }
    });

    try {
      return super.executeUpdate(records);
    } finally {
      iss.forEach(is -> {
        try {
          is.close();
        } catch (Exception ignore) {
        }
      });
    }
  }

}
