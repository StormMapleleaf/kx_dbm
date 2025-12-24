package org.dromara.dbswitch.core.basic.robot;

import org.dromara.dbswitch.core.basic.exchange.MemChannel;
import org.dromara.dbswitch.core.basic.task.TaskResult;
import java.util.Optional;

public abstract class AbstractRobot<R extends TaskResult> implements Robot {

  private MemChannel channel;

  public void setChannel(MemChannel channel) {
    this.channel = channel;
  }

  public MemChannel getChannel() {
    return this.channel;
  }

  public void clearChannel() {
    this.channel.clear();
  }

  public abstract Optional<R> getWorkResult();
}
