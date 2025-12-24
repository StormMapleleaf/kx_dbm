package org.dromara.dbswitch.data.handler;

import cn.hutool.core.date.StopWatch;
import org.dromara.dbswitch.core.basic.exchange.BatchElement;
import org.dromara.dbswitch.core.basic.exchange.MemChannel;
import org.dromara.dbswitch.core.basic.robot.RobotReader;
import org.dromara.dbswitch.core.basic.task.TaskProcessor;
import org.dromara.dbswitch.data.domain.WriterTaskParam;
import org.dromara.dbswitch.data.domain.WriterTaskResult;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WriterTaskThread extends TaskProcessor<WriterTaskResult> {

  private final WriterTaskParam taskParam;

  public WriterTaskThread(WriterTaskParam taskParam) {
    this.taskParam = taskParam;
  }

  @Override
  protected WriterTaskResult doProcess() {
    MemChannel memChannel = this.taskParam.getMemChannel();
    RobotReader robotReader = this.taskParam.getRobotReader();
    WriterTaskResult taskResult = WriterTaskResult.builder().success(true).build();
    StopWatch stopWatch = new StopWatch(Thread.currentThread().getName());
    stopWatch.start();

    try {
      BatchElement elem;
      while (!memChannel.isEmpty() || robotReader.getRemainingCount() > 0) {
        elem = memChannel.poll();
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        if (null != elem) {
          try {
            Long ret = Long.valueOf(elem.getArg2().size());
            if (this.taskParam.isConcurrentWrite()) {
              elem.getHandler().apply(elem.getArg1(), elem.getArg2(), log);
            } else {
              synchronized (this.taskParam) {
                elem.getHandler().apply(elem.getArg1(), elem.getArg2(), log);
              }
            }
            Long count = taskResult.getPerf().get(elem.getTableNameMapString());
            Long total = ret + Optional.ofNullable(count).orElse(0L);
            taskResult.getPerf().put(elem.getTableNameMapString(), total);
          } catch (Throwable t) {
            taskResult.setSuccess(false);
            taskResult.getExcept().putIfAbsent(elem.getTableNameMapString(), t);
          }
        }
      }
    } finally {
      stopWatch.stop();
      taskResult.setDuration(stopWatch.getTotalTimeMillis());
    }
    return taskResult;
  }

  @Override
  public WriterTaskResult exceptProcess(Throwable t) {
    return WriterTaskResult.builder()
        .success(false)
        .duration(0)
        .throwable(t)
        .build();
  }

}
