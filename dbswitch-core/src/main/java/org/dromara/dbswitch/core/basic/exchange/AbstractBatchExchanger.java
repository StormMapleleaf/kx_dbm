package org.dromara.dbswitch.core.basic.exchange;

import org.dromara.dbswitch.common.util.ExamineUtils;
import org.dromara.dbswitch.core.basic.robot.RobotReader;
import org.dromara.dbswitch.core.basic.robot.RobotWriter;
import org.springframework.core.task.AsyncTaskExecutor;

public abstract class AbstractBatchExchanger {

  private MemChannel memChannel;
  private AsyncTaskExecutor readThreadExecutor;
  private AsyncTaskExecutor writeThreadExecutor;

  public AbstractBatchExchanger(AsyncTaskExecutor readExecutor, AsyncTaskExecutor writeExecutor, int channelMaxSize) {
    ExamineUtils.checkNotNull(readExecutor, "readExecutor");
    ExamineUtils.checkNotNull(writeExecutor, "writeExecutor");
    this.memChannel = MemChannel.createNewChannel(channelMaxSize);
    this.readThreadExecutor = readExecutor;
    this.writeThreadExecutor = writeExecutor;
  }

  public MemChannel getMemChannel() {
    return memChannel;
  }

  public int getChannelWaitingNum() {
    return memChannel.size();
  }

  public void exchange(RobotReader reader, RobotWriter writer) {
    reader.setChannel(this.memChannel);
    writer.setChannel(this.memChannel);

    reader.init(readThreadExecutor);
    writer.init(writeThreadExecutor);

    writer.startWork();
    reader.startWork();

    writer.waitForFinish();

    Throwable throwable = collectPerfStats(reader, writer);
    if (null != throwable) {
      if (throwable instanceof RuntimeException) {
        throw (RuntimeException) throwable;
      }
      throw new RuntimeException(throwable);
    }
  }

  protected abstract Throwable collectPerfStats(RobotReader reader, RobotWriter writer);
}
