// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.core.basic.robot;

import org.springframework.core.task.AsyncTaskExecutor;

public interface Robot {

  void init(AsyncTaskExecutor threadExecutor);

  void startWork();

  void interrupt();
}
