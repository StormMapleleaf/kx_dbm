package org.dromara.dbswitch.core.basic.robot;

import org.springframework.core.task.AsyncTaskExecutor;

public interface Robot {

  void init(AsyncTaskExecutor threadExecutor);

  void startWork();

  void interrupt();
}
