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

import org.dromara.dbswitch.core.basic.task.TaskResult;

public abstract class RobotReader<R extends TaskResult> extends AbstractRobot<R> {

  public abstract void startRead();

  @Override
  public void startWork() {
    startRead();
  }

  public abstract long getRemainingCount();
}
