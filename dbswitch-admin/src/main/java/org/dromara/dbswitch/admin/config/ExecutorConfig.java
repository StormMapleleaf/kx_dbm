package org.dromara.dbswitch.admin.config;

import org.dromara.dbswitch.data.util.DataSourceUtils;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration("dbswitchExecutorConfig")
public class ExecutorConfig {

  public final static String TASK_READ_EXECUTOR_BEAN_NAME = "readerTaskExecutor";
  public final static String TASK_WRITE_EXECUTOR_BEAN_NAME = "writerTaskExecutor";

  /**
   * 创建一个异步读取任务线程池
   *
   * @return ThreadPoolTaskExecutor
   */
  @Bean(TASK_READ_EXECUTOR_BEAN_NAME)
  public AsyncTaskExecutor createReaderTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(DataSourceUtils.MAX_THREAD_COUNT / 2);
    taskExecutor.setMaxPoolSize(DataSourceUtils.MAX_THREAD_COUNT / 2);
    taskExecutor.setQueueCapacity(10000);
    taskExecutor.setKeepAliveSeconds(1800);
    taskExecutor.setDaemon(true);
    taskExecutor.setThreadGroupName("dbswitch");
    taskExecutor.setThreadNamePrefix("dbswitch-reader-");
    taskExecutor.setBeanName(TASK_READ_EXECUTOR_BEAN_NAME);
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    taskExecutor.initialize();
    return taskExecutor;
  }

  /**
   * 创建一个异步写入任务线程池
   *
   * @return ThreadPoolTaskExecutor
   */
  @Bean(TASK_WRITE_EXECUTOR_BEAN_NAME)
  public AsyncTaskExecutor createWriterTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(DataSourceUtils.MAX_THREAD_COUNT / 2);
    taskExecutor.setMaxPoolSize(DataSourceUtils.MAX_THREAD_COUNT / 2);
    taskExecutor.setQueueCapacity(10000);
    taskExecutor.setKeepAliveSeconds(1800);
    taskExecutor.setDaemon(true);
    taskExecutor.setThreadGroupName("dbswitch");
    taskExecutor.setThreadNamePrefix("dbswitch-writer-");
    taskExecutor.setBeanName(TASK_WRITE_EXECUTOR_BEAN_NAME);
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    taskExecutor.initialize();
    return taskExecutor;
  }

}
