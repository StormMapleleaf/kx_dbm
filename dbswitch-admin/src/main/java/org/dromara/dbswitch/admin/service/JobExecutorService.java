// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.service;

import org.dromara.dbswitch.admin.execution.ExecuteJobTaskRunnable;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * <p>
 * 如果你使用了@PersistJobDataAfterExecution注解，则强烈建议你同时使用@DisallowConcurrentExecution注解，
 * <p>
 * 因为当同一个job（JobDetail）的两个实例被并发执行时，由于竞争，JobDataMap中存储的数据很可能是不确定的。
 * <p>
 */
@Slf4j
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobExecutorService extends QuartzJobBean implements InterruptableJob {

  public final static String GROUP = "dbswitch";
  public final static String TASK_ID = "taskId";
  public final static String SCHEDULE = "schedule";

  /**
   * 作为一个是否被中断的标识
   */
  private volatile boolean interrupted = false;

  /**
   * 记录当前线程
   */
  private Thread currentThread;

  /**
   * 因为在QuartzConfig中进行了注入配置，所以 Quartz会将数据注入到jobKey变量中
   */
  private String taskId;

  /**
   * JOB实体
   */
  private ExecuteJobTaskRunnable taskRunnable;

  /**
   * 实现setter方法，Quartz会给成员变量taskId注入值
   *
   * @param taskId Task的ID
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    log.info("Quartz Schedule Task job is interrupting : taskId={} ", taskId);
    interrupted = true;
    if (Objects.nonNull(taskRunnable)) {
      taskRunnable.interrupt();
    }
    currentThread.interrupt();
  }

  @Override
  public void executeInternal(JobExecutionContext context) throws JobExecutionException {
    currentThread = Thread.currentThread();
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    if (interrupted) {
      log.info("Quartz task id:{} interrupted when thread begin", jobDataMap.getLong(TASK_ID));
      return;
    }

    JobKey key = context.getJobDetail().getKey();
    Long taskId = jobDataMap.getLongValue(TASK_ID);
    Integer schedule = jobDataMap.getIntValue(SCHEDULE);
    taskRunnable = new ExecuteJobTaskRunnable(taskId, schedule, key.getName());
    taskRunnable.run();
  }

}
