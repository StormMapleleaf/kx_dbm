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
import org.dromara.dbswitch.core.provider.write.DefaultTableDataWriteProvider;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OracleTableDataWriteProvider extends DefaultTableDataWriteProvider {

  public OracleTableDataWriteProvider(ProductFactoryProvider factoryProvider) {
    super(factoryProvider);
  }

  @Override
  public long write(List<String> fieldNames, List<Object[]> recordValues) {
    List<InputStream> iss = new ArrayList<>();
    recordValues.parallelStream().forEach((Object[] row) -> {
      for (int i = 0; i < row.length; ++i) {
        int dataType = this.columnType.get(fieldNames.get(i));
        row[i] = OracleCastUtils.castByJdbcType(dataType, row[i], iss);
      }
    });

    try {
      return super.write(fieldNames, recordValues);
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
