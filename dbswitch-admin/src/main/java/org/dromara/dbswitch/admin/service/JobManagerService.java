package org.dromara.dbswitch.admin.service;

import org.dromara.dbswitch.admin.common.response.PageResult;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.controller.converter.TaskJobDetailConverter;
import org.dromara.dbswitch.admin.dao.AssignmentJobDAO;
import org.dromara.dbswitch.admin.model.response.TaskJobDetailResponse;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.util.PageUtils;
import org.dromara.dbswitch.common.converter.ConverterFactory;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobManagerService {

  @Resource
  private AssignmentJobDAO assignmentJobDAO;
  @Resource
  private ScheduleService scheduleService;

  @EventListener(ApplicationReadyEvent.class)
  public void initAfterRestart() {
    String errorLog = "Job was canceled by restart dbswitch program! ";
    try {
      assignmentJobDAO.updateStatus(JobStatusEnum.RUNNING, JobStatusEnum.FAIL, errorLog);
      log.info("Success to revise job status");
    } catch (Throwable t) {
      log.error("Error when revise job status from running to failed:", t);
    }
  }

  public PageResult<TaskJobDetailResponse> listJobs(Long assignmentId, Integer page, Integer size) {
    Supplier<List<TaskJobDetailResponse>> method = () -> {
      List<AssignmentJobEntity> jobs = assignmentJobDAO.getByAssignmentId(assignmentId);
      return ConverterFactory.getConverter(TaskJobDetailConverter.class).convert(jobs);
    };

    return PageUtils.getPage(method, page, size);
  }

  public Result<TaskJobDetailResponse> detailJob(Long jobId) {
    AssignmentJobEntity job = assignmentJobDAO.getById(jobId);
    if (Objects.isNull(job)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "jobId=" + jobId.toString());
    }

    return Result.success(ConverterFactory.getConverter(TaskJobDetailConverter.class).convert(job));
  }

  public Result<Boolean> cancelJob(Long jobId) {
    AssignmentJobEntity job = assignmentJobDAO.getById(jobId);
    if (Objects.isNull(job)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "jobId=" + jobId.toString());
    }

    if (job.getStatus() == JobStatusEnum.RUNNING.getValue()) {
      scheduleService.cancelJob(jobId);
    }

    return Result.success(true);
  }

}
