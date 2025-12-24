package org.dromara.dbswitch.data.config;

import org.dromara.dbswitch.data.util.DataSourceUtils;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration("dbswitchTaskExecutorConfig")
public class DbswitchTaskExecutorConfig {

  public final static String TASK_EXECUTOR_READ_NAME = "tableReadExecutor";
  public final static String TASK_EXECUTOR_WRITE_NAME = "tableWriteExecutor";

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
