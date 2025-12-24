package org.dromara.dbswitch.core.basic.task;

import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

public abstract class TaskProcessor<R extends TaskResult> implements Supplier<R> {

  protected void checkInterrupt() {
    if (Thread.currentThread().isInterrupted()) {
      throw new CancellationException("task is interrupted");
    }
  }

  protected void beforeProcess() {

  }


  protected abstract R doProcess();


  protected abstract R exceptProcess(Throwable t);

  protected void afterProcess() {

  }

  @Override
  public R get() {
    try {
      checkInterrupt();
      beforeProcess();
      checkInterrupt();
      return doProcess();
    } catch (Throwable t) {
      return exceptProcess(t);
    } finally {
      afterProcess();
    }
  }

}
