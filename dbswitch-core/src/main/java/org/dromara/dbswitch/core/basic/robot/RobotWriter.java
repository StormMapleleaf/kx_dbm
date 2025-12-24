package org.dromara.dbswitch.core.basic.robot;

import org.dromara.dbswitch.core.basic.task.TaskResult;

public abstract class RobotWriter<R extends TaskResult> extends AbstractRobot<R> {

  public abstract void startWrite();

  @Override
  public void startWork() {
    startWrite();
  }

  public abstract void waitForFinish();
}
