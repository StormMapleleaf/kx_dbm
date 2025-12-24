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

@Slf4j
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobExecutorService extends QuartzJobBean implements InterruptableJob {

  public final static String GROUP = "dbswitch";
  public final static String TASK_ID = "taskId";
  public final static String SCHEDULE = "schedule";


  private volatile boolean interrupted = false;


  private Thread currentThread;


  private String taskId;


  private ExecuteJobTaskRunnable taskRunnable;


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
