package org.dromara.dbswitch.admin.model.request;

import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.type.ScheduleModeEnum;
import org.dromara.dbswitch.admin.util.CronExprUtils;
import org.dromara.dbswitch.common.util.PatterNameUtils;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@NoArgsConstructor
@Data
public class AssigmentCreateRequest extends AssigmentBaseRequest {

  private String name;
  private String description;
  private ScheduleModeEnum scheduleMode;
  private String cronExpression;
  private AssigmentConfig config;

  public AssignmentTaskEntity toAssignmentTask() {
    AssignmentTaskEntity assignment = new AssignmentTaskEntity();
    assignment.setId(null);
    assignment.setName(name);
    assignment.setDescription(description);
    assignment.setScheduleMode(scheduleMode);
    if (ScheduleModeEnum.SYSTEM_SCHEDULED == this.getScheduleMode()) {
      CronExprUtils.checkCronExpressionValid(this.getCronExpression(), CronExprUtils.MIN_INTERVAL_SECONDS);
      assignment.setCronExpression(this.getCronExpression());
    }

    return assignment;
  }

  public AssignmentConfigEntity toAssignmentConfig(Long assignmentId) {
    if (Objects.equals(config.getSourceConnectionId(), config.getTargetConnectionId())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ASSIGNMENT_CONFIG, "源端与目标端不能相同");
    }

    AssignmentConfigEntity assignmentConfigEntity = toAssignmentConfig(assignmentId, config);
    assignmentConfigEntity.setFirstFlag(Boolean.TRUE);

    if (!assignmentConfigEntity.getExcludedFlag()
        && !CollectionUtils.isEmpty(assignmentConfigEntity.getSourceTables())) {
      for (String tableName : assignmentConfigEntity.getSourceTables()) {
        String targetTableName = PatterNameUtils.getFinalName(tableName,
            assignmentConfigEntity.getTableNameMap());
        if (StringUtils.isEmpty(targetTableName)) {
          throw new DbswitchException(
              ResultCode.ERROR_INVALID_ASSIGNMENT_CONFIG,
              "表名的映射关系配置有误，不允许将表[" + tableName + "]映射为空");
        }
      }
    }

    return assignmentConfigEntity;
  }

}
