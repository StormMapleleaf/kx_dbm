package org.dromara.dbswitch.admin.service;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.PageResult;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.controller.converter.AssignmentDetailConverter;
import org.dromara.dbswitch.admin.controller.converter.AssignmentInfoConverter;
import org.dromara.dbswitch.admin.controller.converter.AssignmentsConverter;
import org.dromara.dbswitch.admin.dao.AssignmentConfigDAO;
import org.dromara.dbswitch.admin.dao.AssignmentJobDAO;
import org.dromara.dbswitch.admin.dao.AssignmentTaskDAO;
import org.dromara.dbswitch.admin.dao.DatabaseConnectionDAO;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.entity.AssignmentJobEntity;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.admin.model.request.AssigmentCreateRequest;
import org.dromara.dbswitch.admin.model.request.AssigmentUpdateRequest;
import org.dromara.dbswitch.admin.model.request.AssignmentSearchRequest;
import org.dromara.dbswitch.admin.model.response.AssignmentDetailResponse;
import org.dromara.dbswitch.admin.model.response.AssignmentInfoResponse;
import org.dromara.dbswitch.admin.model.response.AssignmentsDataResponse;
import org.dromara.dbswitch.admin.type.JobStatusEnum;
import org.dromara.dbswitch.admin.type.ScheduleModeEnum;
import org.dromara.dbswitch.admin.util.ExcelUtils;
import org.dromara.dbswitch.admin.util.PageUtils;
import org.dromara.dbswitch.common.converter.ConverterFactory;
import org.dromara.dbswitch.common.entity.TableColumnPair;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.data.config.DbswichPropertiesConfiguration;
import org.dromara.dbswitch.data.entity.GlobalParamConfigProperties;
import org.dromara.dbswitch.data.entity.SourceDataSourceProperties;
import org.dromara.dbswitch.data.entity.TargetDataSourceProperties;
import org.dromara.dbswitch.data.util.JsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {

  @Resource
  private AssignmentTaskDAO assignmentTaskDAO;

  @Resource
  private AssignmentConfigDAO assignmentConfigDAO;

  @Resource
  private AssignmentJobDAO assignmentJobDAO;

  @Resource
  private ScheduleService scheduleService;

  @Resource
  private DatabaseConnectionDAO databaseConnectionDAO;

  @Resource
  private DriverLoadService driverLoadService;

  @Transactional(rollbackFor = Exception.class)
  public AssignmentInfoResponse createAssignment(AssigmentCreateRequest request) {
    AssignmentTaskEntity assignment = request.toAssignmentTask();
    assignmentTaskDAO.insert(assignment);

    AssignmentConfigEntity assignmentConfigEntity = request.toAssignmentConfig(assignment.getId());
    assignmentConfigDAO.insert(assignmentConfigEntity);

    Long targetConnectionId = assignmentConfigEntity.getTargetConnectionId();
    DatabaseConnectionEntity targetEntity = databaseConnectionDAO.getById(targetConnectionId);
    if (ProductTypeEnum.SQLITE3 == targetEntity.getType()) {
      if (ProductTypeEnum.isUnsupportedTargetSqlite(targetEntity.getUrl())) {
        throw new DbswitchException(ResultCode.ERROR_INVALID_ASSIGNMENT_CONFIG,
            "不支持目的端数据源为远程服务器上的SQLite或内存方式下的SQLite");
      }
    }

    Long sourceConnectionId = assignmentConfigEntity.getSourceConnectionId();
    DatabaseConnectionEntity sourceEntity = databaseConnectionDAO.getById(sourceConnectionId);
    if (ProductTypeEnum.ELASTICSEARCH == sourceEntity.getType()) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ASSIGNMENT_CONFIG,
          "不支持源端数据源为ElasticSearch类型");
    }

    return ConverterFactory.getConverter(AssignmentInfoConverter.class)
        .convert(assignmentTaskDAO.getById(assignment.getId()));
  }

  public void deleteAssignment(Long id) {
    AssignmentTaskEntity taskEntity = assignmentTaskDAO.getById(id);
    if (null != taskEntity && null != taskEntity.getPublished() && taskEntity.getPublished()) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_HAS_DEPLOY,
          "已经发布的任务需先下线后方可执行删除操作");
    }
    assignmentTaskDAO.deleteById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateAssignment(AssigmentUpdateRequest request) {
    AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(request.getId());
    if (Objects.isNull(assignmentTaskEntity)) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + request.getId());
    } else if (assignmentTaskEntity.getPublished()) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_HAS_DEPLOY, "ID=" + request.getId());
    }

    AssignmentTaskEntity newAssignmentTaskEntity = request.toAssignmentTask();
    assignmentTaskDAO.updateById(newAssignmentTaskEntity);

    AssignmentConfigEntity assignmentConfigEntity = request
        .toAssignmentConfig(assignmentTaskEntity.getId());
    assignmentConfigDAO.deleteByAssignmentTaskId(assignmentTaskEntity.getId());
    assignmentConfigDAO.insert(assignmentConfigEntity);

    Long targetConnectionId = assignmentConfigEntity.getTargetConnectionId();
    DatabaseConnectionEntity entity = databaseConnectionDAO.getById(targetConnectionId);
    if (ProductTypeEnum.SQLITE3 == entity.getType()) {
      if (ProductTypeEnum.isUnsupportedTargetSqlite(entity.getUrl())) {
        throw new DbswitchException(ResultCode.ERROR_INVALID_ASSIGNMENT_CONFIG,
            "不支持目的端数据源为远程服务器上的SQLite或内存方式下的SQLite");
      }
    }
  }

  public PageResult<AssignmentInfoResponse> listAll(AssignmentSearchRequest request) {
    Supplier<List<AssignmentInfoResponse>> method = () ->
        ConverterFactory.getConverter(AssignmentInfoConverter.class)
            .convert(assignmentTaskDAO.listAll(request.getSearchText()));
    return PageUtils.getPage(method, request.getPage(), request.getSize());
  }

  public Result<AssignmentDetailResponse> detailAssignment(Long id) {
    AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
    if (Objects.isNull(assignmentTaskEntity)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + id);
    }

    AssignmentDetailResponse detailResponse = ConverterFactory
        .getConverter(AssignmentDetailConverter.class).convert(assignmentTaskEntity);
    return Result.success(detailResponse);
  }

  public Result<AssignmentInfoResponse> infoAssignment(Long id) {
    AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
    if (Objects.isNull(assignmentTaskEntity)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + id);
    }

    AssignmentInfoResponse infoResponse = ConverterFactory
        .getConverter(AssignmentInfoConverter.class).convert(assignmentTaskEntity);
    return Result.success(infoResponse);
  }

  @Transactional(rollbackFor = Exception.class)
  public void deployAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);
    ids.forEach(id -> {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (assignmentTaskEntity.getPublished()) {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_HAS_DEPLOY, "ID=" + id);
      }
    });

    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO.getByAssignmentTaskId(id);

      DbswichPropertiesConfiguration properties = new DbswichPropertiesConfiguration();
      properties.setSource(this.getSourceDataSourceProperties(assignmentConfigEntity));
      properties.setTarget(this.getTargetDataSourceProperties(assignmentConfigEntity));
      properties.setConfig(this.getGlobalParamConfigProperties(assignmentConfigEntity));

      assignmentTaskEntity.setPublished(Boolean.TRUE);
      assignmentTaskEntity.setContent(JsonUtils.toJsonString(properties));
      assignmentTaskDAO.updateById(assignmentTaskEntity);

      ScheduleModeEnum systemScheduled = ScheduleModeEnum.SYSTEM_SCHEDULED;
      if (assignmentTaskEntity.getScheduleMode() == systemScheduled) {
        scheduleService.scheduleTask(assignmentTaskEntity.getId(), systemScheduled);
      }
    }

  }

  @Transactional(rollbackFor = Exception.class)
  public void runAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);
    List<AssignmentTaskEntity> tasks = new ArrayList<>();
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (assignmentTaskEntity.getPublished()) {
        tasks.add(assignmentTaskEntity);
      } else {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_DEPLOY, assignmentTaskEntity.getName());
      }
    }

    tasks.forEach(assignmentTask -> {
      scheduleService.scheduleTask(assignmentTask.getId(), ScheduleModeEnum.MANUAL);
    });

  }

  @Transactional(rollbackFor = Exception.class)
  public void retireAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (Objects.nonNull(assignmentTaskEntity.getPublished())
          && assignmentTaskEntity.getPublished()) {
        String jobKey = assignmentTaskEntity.getJobKey();
        scheduleService.cancelByJobKey(jobKey);
        scheduleService.cancelManualJob(id);
        assignmentTaskEntity.setPublished(Boolean.FALSE);
        assignmentTaskEntity.setContent("{}");
        assignmentTaskEntity.setJobKey("");
        assignmentTaskDAO.updateById(assignmentTaskEntity);
      }
    }
  }

  private void checkAssignmentAllExist(List<Long> ids) {
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (Objects.isNull(assignmentTaskEntity)) {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + id);
      }
      AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO.getByAssignmentTaskId(id);
      Long sourceConnectionId = assignmentConfigEntity.getSourceConnectionId();
      Long targetConnectionId = assignmentConfigEntity.getTargetConnectionId();
      List<Long> connectionIds = Lists.newArrayList(sourceConnectionId, targetConnectionId);
      if (databaseConnectionDAO.getByIds(connectionIds).size() != connectionIds.size()) {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS,
            "ConnectionID=" + connectionIds);
      }
    }
  }

  private SourceDataSourceProperties getSourceDataSourceProperties(
      AssignmentConfigEntity assignmentConfigEntity) {
    SourceDataSourceProperties sourceDataSourceProperties = new SourceDataSourceProperties();
    DatabaseConnectionEntity sourceDatabaseConnectionEntity = databaseConnectionDAO.getById(
        assignmentConfigEntity.getSourceConnectionId()
    );
    File driverVersionFile = driverLoadService.getVersionDriverFile(
        sourceDatabaseConnectionEntity.getType(),
        sourceDatabaseConnectionEntity.getVersion());
    sourceDataSourceProperties.setType(sourceDatabaseConnectionEntity.getType());
    sourceDataSourceProperties.setUrl(sourceDatabaseConnectionEntity.getUrl());
    sourceDataSourceProperties.setDriverClassName(sourceDatabaseConnectionEntity.getDriver());
    sourceDataSourceProperties.setDriverPath(driverVersionFile.getAbsolutePath());
    sourceDataSourceProperties.setUsername(sourceDatabaseConnectionEntity.getUsername());
    sourceDataSourceProperties.setPassword(sourceDatabaseConnectionEntity.getPassword());

    String sourceSchema = assignmentConfigEntity.getSourceSchema();
    if (assignmentConfigEntity.getExcludedFlag()) {
      if (CollectionUtils.isEmpty(assignmentConfigEntity.getSourceTables())) {
        sourceDataSourceProperties.setSourceExcludes("");
      } else {
        sourceDataSourceProperties.setSourceExcludes(
            assignmentConfigEntity.getSourceTables()
                .stream().collect(Collectors.joining(","))
        );
      }
    } else {
      if (CollectionUtils.isEmpty(assignmentConfigEntity.getSourceTables())) {
        sourceDataSourceProperties.setSourceIncludes("");
      } else {
        sourceDataSourceProperties.setSourceIncludes(
            assignmentConfigEntity.getSourceTables()
                .stream().collect(Collectors.joining(","))
        );
      }
    }
    sourceDataSourceProperties.setSourceSchema(sourceSchema);

    Map<String, String> incrTableColumns = Optional
        .ofNullable(assignmentConfigEntity.getIncrTableColumns())
        .orElseGet(ArrayList::new)
        .stream().collect(Collectors.toMap(TableColumnPair::getTableName,
            TableColumnPair::getColumnName, (a, b) -> b));

    sourceDataSourceProperties.setIncrTableColumns(incrTableColumns);
    sourceDataSourceProperties.setBeforeSqlScripts(assignmentConfigEntity.getPreSqlScripts());
    sourceDataSourceProperties.setAfterSqlScripts(assignmentConfigEntity.getPostSqlScripts());
    sourceDataSourceProperties.setRegexTableMapper(assignmentConfigEntity.getTableNameMap());
    sourceDataSourceProperties.setRegexColumnMapper(assignmentConfigEntity.getColumnNameMap());
    sourceDataSourceProperties.setFetchSize(assignmentConfigEntity.getBatchSize());
    sourceDataSourceProperties.setTableType(assignmentConfigEntity.getTableType().name());
    return sourceDataSourceProperties;
  }

  private TargetDataSourceProperties getTargetDataSourceProperties(
      AssignmentConfigEntity assignmentConfigEntity) {
    TargetDataSourceProperties targetDataSourceProperties = new TargetDataSourceProperties();
    DatabaseConnectionEntity targetDatabaseConnectionEntity = databaseConnectionDAO
        .getById(assignmentConfigEntity.getTargetConnectionId());
    File driverVersionFile = driverLoadService.getVersionDriverFile(
        targetDatabaseConnectionEntity.getType(),
        targetDatabaseConnectionEntity.getVersion());
    targetDataSourceProperties.setType(targetDatabaseConnectionEntity.getType());
    targetDataSourceProperties.setUrl(targetDatabaseConnectionEntity.getUrl());
    targetDataSourceProperties.setDriverClassName(targetDatabaseConnectionEntity.getDriver());
    targetDataSourceProperties.setDriverPath(driverVersionFile.getAbsolutePath());
    targetDataSourceProperties.setUsername(targetDatabaseConnectionEntity.getUsername());
    targetDataSourceProperties.setPassword(targetDatabaseConnectionEntity.getPassword());
    targetDataSourceProperties.setTargetSchema(assignmentConfigEntity.getTargetSchema());
    if (assignmentConfigEntity.getTargetDropTable()) {
      targetDataSourceProperties.setTargetDrop(Boolean.TRUE);
      targetDataSourceProperties.setChangeDataSync(Boolean.FALSE);
    } else {
      targetDataSourceProperties.setTargetDrop(Boolean.FALSE);
      targetDataSourceProperties.setChangeDataSync(Boolean.TRUE);
    }
    if (assignmentConfigEntity.getTargetOnlyCreate()) {
      targetDataSourceProperties.setOnlyCreate(Boolean.TRUE);
    }
    if (assignmentConfigEntity.getTargetAutoIncrement()) {
      targetDataSourceProperties.setCreateTableAutoIncrement(Boolean.TRUE);
    }
    targetDataSourceProperties.setTableNameCase(assignmentConfigEntity.getTableNameCase());
    targetDataSourceProperties.setColumnNameCase(assignmentConfigEntity.getColumnNameCase());
    targetDataSourceProperties.setTargetSyncOption(assignmentConfigEntity.getTargetSyncOption());
    targetDataSourceProperties.setBeforeSqlScripts(assignmentConfigEntity.getBeforeSqlScripts());
    targetDataSourceProperties.setAfterSqlScripts(assignmentConfigEntity.getAfterSqlScripts());

    return targetDataSourceProperties;
  }

  private GlobalParamConfigProperties getGlobalParamConfigProperties(
      AssignmentConfigEntity assignmentConfigEntity) {
    GlobalParamConfigProperties configProperties = new GlobalParamConfigProperties();
    configProperties.setChannelQueueSize(assignmentConfigEntity.getChannelSize());
    return configProperties;
  }

  public void exportAssignments(List<Long> ids, HttpServletResponse response) {
    checkAssignmentAllExist(ids);
    List<AssignmentsDataResponse> assignmentsDataResponses = new ArrayList<>();
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      AssignmentsDataResponse assignmentsDataResponse = ConverterFactory.getConverter(AssignmentsConverter.class)
          .convert(assignmentTaskEntity);

      AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO.getByAssignmentTaskId(id);

      Long sourceConnectionId = assignmentConfigEntity.getSourceConnectionId();
      DatabaseConnectionEntity databaseConnectionEntity = databaseConnectionDAO.getById(sourceConnectionId);
      String sourceSchema = assignmentConfigEntity.getSourceSchema();
      assignmentsDataResponse.setSourceSchema(sourceSchema);
      String sourceType = databaseConnectionEntity.getType().getName();
      assignmentsDataResponse.setSourceType(sourceType);

      Long targetConnectionId = assignmentConfigEntity.getTargetConnectionId();
      DatabaseConnectionEntity databaseConnectionEntity1 = databaseConnectionDAO.getById(targetConnectionId);
      String targetSchema = assignmentConfigEntity.getTargetSchema();
      assignmentsDataResponse.setTargetSchema(targetSchema);
      String targetType = databaseConnectionEntity1.getType().getName();
      assignmentsDataResponse.setTargetType(targetType);

      AssignmentJobEntity assignmentJobEntity = assignmentJobDAO.getLatestJobEntity(id);
      Integer status = (assignmentJobEntity == null || assignmentJobEntity.getStatus() == null)
          ? JobStatusEnum.INIT.getValue()
          : assignmentJobEntity.getStatus();
      assignmentsDataResponse.setRunStatus(JobStatusEnum.of(status).getName());
      assignmentsDataResponses.add(assignmentsDataResponse);
    }
    ExcelUtils.write(response, AssignmentsDataResponse.class, assignmentsDataResponses, "任务管理", "任务管理列表");
  }
}
