// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.execution;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.dromara.dbswitch.admin.dao.AssignmentConfigDAO;
import org.dromara.dbswitch.admin.dao.AssignmentJobDAO;
import org.dromara.dbswitch.admin.dao.AssignmentTaskDAO;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.config.ExecutorConfig;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.logback.LogbackAppenderRegister;
import org.dromara.dbswitch.common.entity.MdcKeyValue;
import org.dromara.dbswitch.data.config.DbswichPropertiesConfiguration;
import org.dromara.dbswitch.data.service.MigrationService;
import org.dromara.dbswitch.data.util.JsonUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;

@Slf4j
public class ExecuteJobTaskRunnable implements Runnable {

  private final static String MDC_KEY = LogbackAppenderRegister.LOG_MDC_KEY_NAME;

  // 相同taskId的任务限制并发执行的粒度锁缓存对象
  private static Cache<String, ReentrantLock> mutexes = CacheBuilder.newBuilder()
      .expireAfterWrite(24 * 60L, TimeUnit.MINUTES)
      .build();

  private volatile boolean interrupted = false;

  private MigrationService migrationService;

  private AssignmentTaskDAO assignmentTaskDAO;

  private AssignmentConfigDAO assignmentConfigDAO;

  private AssignmentJobDAO assignmentJobDAO;

  private AsyncTaskExecutor readerTaskExecutor;

  private AsyncTaskExecutor writerTaskExecutor;

  @Getter
  private Long taskId;

  private Integer schedule;

  private String keyName;

  public ExecuteJobTaskRunnable(Long taskId, Integer schedule, String keyName) {
    this.assignmentTaskDAO = SpringUtil.getBean(AssignmentTaskDAO.class);
    this.assignmentConfigDAO = SpringUtil.getBean(AssignmentConfigDAO.class);
    this.assignmentJobDAO = SpringUtil.getBean(AssignmentJobDAO.class);
    this.readerTaskExecutor = SpringUtil.getBean(
        ExecutorConfig.TASK_READ_EXECUTOR_BEAN_NAME, AsyncTaskExecutor.class);
    this.writerTaskExecutor = SpringUtil.getBean(
        ExecutorConfig.TASK_WRITE_EXECUTOR_BEAN_NAME, AsyncTaskExecutor.class);
    this.taskId = taskId;
    this.schedule = schedule;
    this.keyName = keyName;
  }

  public void interrupt() {
    this.interrupted = true;
    if (null != this.migrationService) {
      this.migrationService.interrupt();
    }
  }

  @Override
  public void run() {
    AssignmentJobEntity assignmentJobEntity = assignmentJobDAO
        .newAssignmentJob(taskId, schedule, keyName);
    MdcKeyValue mdcKeyValue = new MdcKeyValue(MDC_KEY, assignmentJobEntity.getId().toString());

    try {
      ReentrantLock lock = mutexes.get(taskId.toString(), ReentrantLock::new);
      while (!lock.tryLock(1, TimeUnit.SECONDS)) {
        if (interrupted) {
          log.info("Quartz task id:{} interrupted when get lock", taskId);
          return;
        }
        TimeUnit.SECONDS.sleep(1);
      }

      try {
        log.info("Execute Job, and task id is : {} , job id is: {}",
            taskId, assignmentJobEntity.getId());

        AssignmentTaskEntity task = assignmentTaskDAO.getById(taskId);
        AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO
            .getByAssignmentTaskId(task.getId());

        log.info("Execute Assignment [taskId={}],Task Name: {} ,configuration properties：{}",
            task.getId(),
            task.getName(),
            task.getContent());

        try {
          DbswichPropertiesConfiguration properties = JsonUtils.toBeanObject(
              task.getContent(), DbswichPropertiesConfiguration.class);

          /**
           * 下面通过一个三元组来控制同步的方式
           * <targetDrop,onlyCreate,changeDataSync>
           *   <ul>
           *     <li>目标端建表并同步数据：false,false,true</li>
           *     <li>目标端只创建物理表：true,true,false</li>
           *     <li>目标端只同步表里数据：false,false,true</li>
           *   </ul>
           */
          if (!assignmentConfigEntity.getTargetDropTable() && !assignmentConfigEntity.getTargetOnlyCreate()) {
            properties.getTarget().setTargetDrop(false);
            properties.getTarget().setOnlyCreate(false);
            properties.getTarget().setChangeDataSync(true);
          } else {
            if (assignmentConfigEntity.getTargetDropTable() && assignmentConfigEntity.getTargetOnlyCreate()) {
              properties.getTarget().setTargetDrop(true);
              properties.getTarget().setOnlyCreate(true);
              properties.getTarget().setChangeDataSync(false);
            } else {
              if (assignmentConfigEntity.getFirstFlag()) {
                // 首次同步，需要自动建表，然后全量加载数据同步
                properties.getTarget().setTargetDrop(true);
                properties.getTarget().setOnlyCreate(false);
                properties.getTarget().setChangeDataSync(false);
              } else {
                // 非首次，可能无需建表了，后续执行变化数据同步
                properties.getTarget().setTargetDrop(false);
                properties.getTarget().setOnlyCreate(false);
                properties.getTarget().setChangeDataSync(true);
              }
            }
          }

          migrationService = new MigrationService(properties, readerTaskExecutor, writerTaskExecutor);
          if (interrupted) {
            log.info("Quartz task id:{} interrupted when prepare stage", taskId);
            return;
          }

          // 实际执行JOB
          migrationService.setMdcKeyValue(mdcKeyValue);
          migrationService.run();

          if (assignmentConfigEntity.getFirstFlag()) {
            AssignmentConfigEntity config = new AssignmentConfigEntity();
            config.setId(assignmentConfigEntity.getId());
            config.setFirstFlag(Boolean.FALSE);
            assignmentConfigDAO.updateSelective(config);
          }

          assignmentJobEntity.setStatus(JobStatusEnum.PASS.getValue());
          log.info("Execute Assignment Success [taskId={},jobId={}],Task Name: {}",
              task.getId(), assignmentJobEntity.getId(), task.getName());
        } catch (Throwable e) {
          assignmentJobEntity.setStatus(JobStatusEnum.FAIL.getValue());
          assignmentJobEntity.setErrorLog(ExceptionUtil.stacktraceToString(e));
          log.info("Execute Assignment Failed [taskId={},jobId={}],Task Name: {}, Message: {}",
              task.getId(), assignmentJobEntity.getId(), task.getName(), e.getMessage());
        } finally {
          AssignmentJobEntity latestJobEntity = assignmentJobDAO.getById(assignmentJobEntity.getId());
          if (Objects.nonNull(latestJobEntity)) {
            // 注意，这里有可能用户手动取消任务后，直接删除了任务和这个作业，导致查询不到了
            latestJobEntity.setFinishTime(new Timestamp(System.currentTimeMillis()));
            latestJobEntity.setErrorLog(assignmentJobEntity.getErrorLog());
            if (JobStatusEnum.CANCEL.getValue() != latestJobEntity.getStatus()) {
              latestJobEntity.setStatus(assignmentJobEntity.getStatus().intValue());
            }
            assignmentJobDAO.updateSelective(latestJobEntity);
          }
        }
      } finally {
        lock.unlock();
      }
    } catch (ExecutionException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
