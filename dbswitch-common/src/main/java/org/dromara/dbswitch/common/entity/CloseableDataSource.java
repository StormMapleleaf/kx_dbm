// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.entity;

import java.io.Closeable;
import javax.sql.DataSource;

public interface CloseableDataSource extends DataSource, Closeable {

  String getJdbcUrl();

  String getDriverClass();

  String getUserName();

  String getPassword();
}
