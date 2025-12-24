package org.dromara.dbswitch.admin.controller.converter;

import cn.hutool.extra.spring.SpringUtil;
import org.dromara.dbswitch.admin.dao.AssignmentConfigDAO;
import org.dromara.dbswitch.admin.dao.AssignmentJobDAO;
import org.dromara.dbswitch.admin.dao.DatabaseConnectionDAO;
import org.dromara.dbswitch.admin.model.response.AssignmentInfoResponse;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.common.converter.AbstractConverter;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AssignmentInfoConverter extends
    AbstractConverter<AssignmentTaskEntity, AssignmentInfoResponse> {

  private final static String UNKNOWN_CONNECTION_TYPE = "Unknown";
  private AssignmentConfigDAO assignmentConfigDAO = SpringUtil.getBean(AssignmentConfigDAO.class);
  private DatabaseConnectionDAO databaseConnectionDAO = SpringUtil.getBean(DatabaseConnectionDAO.class);
  private AssignmentJobDAO assignmentJobDAO = SpringUtil.getBean(AssignmentJobDAO.class);

  @Override
  public AssignmentInfoResponse convert(AssignmentTaskEntity assignmentTaskEntity) {
    Long taskId = assignmentTaskEntity.getId();
    AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO.getByAssignmentTaskId(taskId);
    Long sourceConnectionId = assignmentConfigEntity.getSourceConnectionId();
    Long targetConnectionId = assignmentConfigEntity.getTargetConnectionId();
    List<Long> connectionIds = Lists.newArrayList(sourceConnectionId, targetConnectionId);
    Map<Long, DatabaseConnectionEntity> databaseConnectionMap = databaseConnectionDAO.getByIds(connectionIds)
        .stream().collect(Collectors.toMap(DatabaseConnectionEntity::getId, Function.identity()));

    AssignmentInfoResponse response = new AssignmentInfoResponse();
    response.setId(assignmentTaskEntity.getId());
    response.setName(assignmentTaskEntity.getName());
    response.setDescription(assignmentTaskEntity.getDescription());
    response.setScheduleMode(assignmentTaskEntity.getScheduleMode());
    response.setCronExpression(assignmentTaskEntity.getCronExpression());
    response.setIsPublished(assignmentTaskEntity.getPublished());
    response.setCreateTime(assignmentTaskEntity.getCreateTime());
    response.setUpdateTime(assignmentTaskEntity.getUpdateTime());
    response.setSourceSchema(assignmentConfigEntity.getSourceSchema());
    response.setTargetSchema(assignmentConfigEntity.getTargetSchema());

    if (databaseConnectionMap.containsKey(sourceConnectionId)) {
      response.setSourceType(databaseConnectionMap.get(sourceConnectionId).getType().getName());
    } else {
      response.setSourceType(UNKNOWN_CONNECTION_TYPE);
    }
    if (databaseConnectionMap.containsKey(targetConnectionId)) {
      response.setTargetType(databaseConnectionMap.get(targetConnectionId).getType().getName());
    } else {
      response.setTargetType(UNKNOWN_CONNECTION_TYPE);
    }

    AssignmentJobEntity assignmentJobEntity = assignmentJobDAO.getLatestJobEntity(taskId);
    Integer status = (assignmentJobEntity == null || assignmentJobEntity.getStatus() == null) ?
        JobStatusEnum.INIT.getValue() :
        assignmentJobEntity.getStatus();
    response.setRunStatus(JobStatusEnum.of(status).getName());
    response.setScheduleTime(assignmentJobEntity == null ? null : assignmentJobEntity.getStartTime());

    return response;
  }
}
