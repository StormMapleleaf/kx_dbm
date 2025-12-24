// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.data.config;

import org.dromara.dbswitch.data.util.DataSourceUtils;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * DBSwitch自用线程池定义
 *
 * @author tang
 */
@Configuration("dbswitchTaskExecutorConfig")
public class DbswitchTaskExecutorConfig {

  public final static String TASK_EXECUTOR_READ_NAME = "tableReadExecutor";
  public final static String TASK_EXECUTOR_WRITE_NAME = "tableWriteExecutor";

  /**
   * 创建一个异步读任务执行线程池
   *
   * @return ThreadPoolTaskExecutor
   */
  @Bean(TASK_EXECUTOR_READ_NAME)
  public AsyncTaskExecutor createTaskReadeExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(DataSourceUtils.MAX_THREAD_COUNT);
    taskExecutor.setMaxPoolSize(DataSourceUtils.MAX_THREAD_COUNT);
    taskExecutor.setQueueCapacity(10000);
    taskExecutor.setKeepAliveSeconds(1800);
    taskExecutor.setDaemon(true);
    taskExecutor.setThreadGroupName("dbswitch-reader");
    taskExecutor.setThreadNamePrefix("dbswitch-read-");
    taskExecutor.setBeanName(TASK_EXECUTOR_READ_NAME);
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    taskExecutor.initialize();
    return taskExecutor;
  }

  /**
   * 创建一个异步写任务执行线程池
   *
   * @return ThreadPoolTaskExecutor
   */
  @Bean(TASK_EXECUTOR_WRITE_NAME)
  public AsyncTaskExecutor createTaskWriteExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(DataSourceUtils.MAX_THREAD_COUNT);
    taskExecutor.setMaxPoolSize(DataSourceUtils.MAX_THREAD_COUNT);
    taskExecutor.setQueueCapacity(10000);
    taskExecutor.setKeepAliveSeconds(1800);
    taskExecutor.setDaemon(true);
    taskExecutor.setThreadGroupName("dbswitch-writer");
    taskExecutor.setThreadNamePrefix("dbswitch-write-");
    taskExecutor.setBeanName(TASK_EXECUTOR_WRITE_NAME);
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    taskExecutor.initialize();
    return taskExecutor;
  }

}
