package org.dromara.dbswitch.admin.controller.converter;

import org.dromara.dbswitch.admin.model.response.TaskJobDetailResponse;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.type.ScheduleModeEnum;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;

public class TaskJobDetailConverter extends
    AbstractConverter<AssignmentJobEntity, TaskJobDetailResponse> {

  @Override
  public TaskJobDetailResponse convert(AssignmentJobEntity assignmentJobEntity) {
    TaskJobDetailResponse response = new TaskJobDetailResponse();
    response.setJobId(assignmentJobEntity.getId());
    response.setAssignmentId(assignmentJobEntity.getAssignmentId());
    response.setStartTime(assignmentJobEntity.getStartTime());
    response.setFinishTime(assignmentJobEntity.getFinishTime());
    if (assignmentJobEntity.getStatus() == JobStatusEnum.RUNNING.getValue()) {
      response.setDuration(
          (System.currentTimeMillis() - assignmentJobEntity.getStartTime().getTime()) / 1000);
    } else {
      response.setDuration(
          (assignmentJobEntity.getFinishTime().getTime() - assignmentJobEntity.getStartTime()
              .getTime())
              / 1000);
    }
    response.setStatus(assignmentJobEntity.getStatus());
    response.setJobStatus(JobStatusEnum.of(assignmentJobEntity.getStatus()).getName());
    response.setScheduleMode(ScheduleModeEnum.of(assignmentJobEntity.getScheduleMode()).getName());
    response.setErrorLog(assignmentJobEntity.getErrorLog());

    return response;
  }

}
