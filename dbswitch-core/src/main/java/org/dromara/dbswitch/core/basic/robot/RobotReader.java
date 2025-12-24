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
