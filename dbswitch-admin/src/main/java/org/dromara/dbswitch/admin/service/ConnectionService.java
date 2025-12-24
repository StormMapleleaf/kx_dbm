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

import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.PageResult;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.controller.converter.DbConnectionDetailConverter;
import org.dromara.dbswitch.admin.dao.AssignmentConfigDAO;
import org.dromara.dbswitch.admin.dao.AssignmentTaskDAO;
import org.dromara.dbswitch.admin.dao.DatabaseConnectionDAO;
import org.dromara.dbswitch.admin.entity.AssignmentConfigEntity;
import org.dromara.dbswitch.admin.entity.AssignmentTaskEntity;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.admin.model.request.DbConnectionCreateRequest;
import org.dromara.dbswitch.admin.model.request.DbConnectionSearchRequest;
import org.dromara.dbswitch.admin.model.request.DbConnectionUpdateRequest;
import org.dromara.dbswitch.admin.model.response.DatabaseTypeDetailResponse;
import org.dromara.dbswitch.admin.model.response.DatabaseTypeDriverResponse;
import org.dromara.dbswitch.admin.model.response.DbConnectionDetailResponse;
import org.dromara.dbswitch.admin.model.response.DbConnectionNameResponse;
import org.dromara.dbswitch.admin.util.PageUtils;
import org.dromara.dbswitch.common.converter.ConverterFactory;
import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.common.util.JdbcUrlUtils;
import org.dromara.dbswitch.core.service.DefaultMetadataService;
import org.dromara.dbswitch.core.service.MetadataService;
import org.dromara.dbswitch.data.util.DataSourceUtils;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {

  @Resource
  private DriverLoadService driverLoadService;
  @Resource
  private DatabaseConnectionDAO databaseConnectionDAO;
  @Resource
  private AssignmentTaskDAO assignmentTaskDAO;
  @Resource
  private AssignmentConfigDAO assignmentConfigDAO;

  public CloseableDataSource getDataSource(Long id) {
    return getDataSource(getDatabaseConnectionById(id));
  }

  public CloseableDataSource getDataSource(DatabaseConnectionEntity dbConn) {
    String typeName = dbConn.getType().getName().toUpperCase();
    ProductTypeEnum supportDbType = ProductTypeEnum.valueOf(typeName);
    if (supportDbType.hasAddress()) {
      for (String pattern : supportDbType.getUrl()) {
        final Matcher matcher = JdbcUrlUtils.getPattern(pattern).matcher(dbConn.getUrl());
        if (!matcher.matches()) {
          if (1 == supportDbType.getUrl().length) {
            throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
          } else {
            continue;
          }
        }

        String host = matcher.group("host");
        String port = matcher.group("port");
        if (StringUtils.isBlank(port)) {
          port = String.valueOf(supportDbType.getPort());
        }

        if (!JdbcUrlUtils.reachable(host, port)) {
          throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
        }
      }
    }
    File driverVersionFile = driverLoadService.getVersionDriverFile(dbConn.getType(), dbConn.getVersion());
    String driverPath = driverVersionFile.getAbsolutePath();
    return DataSourceUtils.createCommonDataSource(dbConn.getUrl(), dbConn.getDriver(),
        driverPath, dbConn.getUsername(), dbConn.getPassword());
  }

  public MetadataService getMetaDataCoreService(Long id) {
    return getMetaDataCoreService(getDatabaseConnectionById(id));
  }

  public MetadataService getMetaDataCoreService(DatabaseConnectionEntity dbConn) {
    CloseableDataSource dataSource = getDataSource(dbConn);
    MetadataService metaDataService = new DefaultMetadataService(dataSource, dbConn.getType());
    return metaDataService;
  }

  public List<DatabaseTypeDetailResponse> getTypes() {
    List<DatabaseTypeDetailResponse> lists = new ArrayList<>();
    for (ProductTypeEnum type : ProductTypeEnum.values()) {
      lists.add(getTypeDetail(type));
    }
    return lists;
  }

  public DatabaseTypeDetailResponse getTypeDetail(ProductTypeEnum type) {
    DatabaseTypeDetailResponse detail = new DatabaseTypeDetailResponse();
    detail.setId(type.getId());
    detail.setType(type.getName().toUpperCase());
    detail.setDriver(type.getDriver());
    detail.setSample(type.getSample());
    detail.setName(type.getName());
    detail.setUrl(JdbcUrlUtils.getTemplateUrl(type.getUrl()[0]));
    return detail;
  }

  public List<DatabaseTypeDriverResponse> getDrivers(ProductTypeEnum dbTypeEnum) {
    List<DatabaseTypeDriverResponse> lists = new ArrayList<>();
    driverLoadService.getDriverVersionWithPath(dbTypeEnum)
        .forEach(
            (k, v) ->
                lists.add(
                    DatabaseTypeDriverResponse.builder()
                        .driverVersion(k)
                        .driverClass(dbTypeEnum.getDriver())
                        .driverPath(v.getAbsolutePath())
                        .jarFiles(
                            FileUtil.listFileNames(v.getAbsolutePath())
                        )
                        .build()
                )
        );
    return lists;
  }

  public PageResult<DbConnectionDetailResponse> getConnections(
      DbConnectionSearchRequest request) {
    Supplier<List<DbConnectionDetailResponse>> method = () -> {
      List<DatabaseConnectionEntity> databaseConnectionEntities = databaseConnectionDAO
          .listAll(request.getSearchText());
      return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
          .convert(databaseConnectionEntities);
    };

    return PageUtils.getPage(method, request.getPage(), request.getSize());
  }

  public DbConnectionDetailResponse getDetailById(Long id) {
    return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(getDatabaseConnectionById(id));
  }

  public Result test(Long id) {
    DatabaseConnectionEntity dbConn = getDatabaseConnectionById(id);
    MetadataService metaDataService = getMetaDataCoreService(dbConn);
    try {
      metaDataService.testQuerySQL(dbConn.getType().getSql());
    } finally {
      metaDataService.close();
    }
    return Result.success();
  }

  public Result<List<String>> getSchemas(Long id) {
    MetadataService metaDataService = getMetaDataCoreService(id);
    try {
      List<String> schemas = metaDataService.querySchemaList();
      return Result.success(schemas);
    } finally {
      metaDataService.close();
    }
  }

  public Result<List<String>> getSchemaTables(Long id, String schema) {
    MetadataService metaDataService = getMetaDataCoreService(id);
    try {
      List<String> tables = Optional.ofNullable(
          metaDataService.queryTableList(schema))
          .orElseGet(ArrayList::new).stream()
          .filter(t -> !t.isViewTable())
          .map(t -> t.getTableName())
          .collect(Collectors.toList());
      return Result.success(tables);
    } finally {
      metaDataService.close();
    }
  }

  public Result<List<String>> getSchemaViews(Long id, String schema) {
    MetadataService metaDataService = getMetaDataCoreService(id);
    try {
      List<String> tables = Optional.ofNullable(
          metaDataService.queryTableList(schema))
          .orElseGet(ArrayList::new).stream()
          .filter(t -> t.isViewTable())
          .map(t -> t.getTableName())
          .collect(Collectors.toList());
      return Result.success(tables);
    } finally {
      metaDataService.close();
    }
  }

  public Result<DbConnectionDetailResponse> addDatabaseConnection(
      DbConnectionCreateRequest request) {
    if (StringUtils.isBlank(request.getName())) {
      return Result.failed(ResultCode.ERROR_INVALID_ARGUMENT, "name is empty");
    }

    if (Objects.nonNull(databaseConnectionDAO.getByName(request.getName()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_ALREADY_EXISTS, "name=" + request.getName());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    validJdbcUrlFormat(conn);
    databaseConnectionDAO.insert(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public Result<DbConnectionDetailResponse> updateDatabaseConnection(
      DbConnectionUpdateRequest request) {
    if (Objects.isNull(request.getId()) || Objects
        .isNull(databaseConnectionDAO.getById(request.getId()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }

    DatabaseConnectionEntity exist = databaseConnectionDAO.getByName(request.getName());
    if (Objects.nonNull(exist) && !exist.getId().equals(request.getId())) {
      return Result.failed(ResultCode.ERROR_RESOURCE_ALREADY_EXISTS, "name=" + request.getName());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    validJdbcUrlFormat(conn);
    databaseConnectionDAO.updateById(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public void deleteDatabaseConnection(Long id) {
    List<Long> assignmentIds = assignmentConfigDAO.getByConnectionId(id)
        .stream()
        .map(AssignmentConfigEntity::getAssignmentId)
        .collect(Collectors.toList());
    if (assignmentIds.size() > 0) {
      AssignmentTaskEntity taskEntity = assignmentTaskDAO.getById(assignmentIds.get(0));
      throw new DbswitchException(ResultCode.ERROR_RELATED_ASSIGNMENT, "[" + taskEntity.getName() + "]");
    }
    databaseConnectionDAO.deleteById(id);
  }

  public PageResult<DbConnectionNameResponse> getNameList(Integer page, Integer size) {
    Supplier<List<DbConnectionNameResponse>> method = () -> {
      List<DatabaseConnectionEntity> lists = databaseConnectionDAO.listAll(null);
      return lists.stream()
          .map(c ->
              DbConnectionNameResponse.builder()
                  .id(c.getId())
                  .name(c.getName())
                  .typeName(c.getType().getName())
                  .useSql(c.getType().isUseSql())
                  .build())
          .collect(Collectors.toList());
    };

    return PageUtils.getPage(method, page, size);
  }

  public DatabaseConnectionEntity getDatabaseConnectionById(Long id) {
    DatabaseConnectionEntity dbConn = databaseConnectionDAO.getById(id);
    if (Objects.isNull(dbConn)) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    return dbConn;
  }

  private void validJdbcUrlFormat(DatabaseConnectionEntity conn) {
    String typeName = conn.getType().getName().toUpperCase();
    ProductTypeEnum supportDbType = ProductTypeEnum.valueOf(typeName);
    if (!conn.getUrl().startsWith(supportDbType.getUrlPrefix())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
    }

    for (int i = 0; i < supportDbType.getUrl().length; ++i) {
      String pattern = supportDbType.getUrl()[i];
      Matcher matcher = JdbcUrlUtils.getPattern(pattern).matcher(conn.getUrl());
      if (!matcher.matches()) {
        if (i == supportDbType.getUrl().length - 1) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
        }
      } else {
        if (supportDbType.hasDatabaseName() && StringUtils.isBlank(matcher.group("database"))) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL,
              "库名没有指定 :" + conn.getUrl());
        }
        if (supportDbType.hasFilePath() && StringUtils.isBlank(matcher.group("file"))) {
          throw new DbswitchException(ResultCode.ERROR_INVALID_JDBC_URL,
              "文件路径没有指定 :" + conn.getUrl());
        }

        break;
      }
    }
  }

  public Result preTest(DbConnectionCreateRequest request) {
    DatabaseConnectionEntity dbConn = new DatabaseConnectionEntity();
    dbConn.setType(request.getType());
    dbConn.setUsername(request.getUsername());
    dbConn.setPassword(request.getPassword());
    dbConn.setName(request.getName());
    dbConn.setVersion(request.getVersion());
    dbConn.setUrl(request.getUrl());
    dbConn.setDriver(request.getDriver());
    MetadataService metaDataService = getMetaDataCoreService(dbConn);
    try {
      metaDataService.testQuerySQL(dbConn.getType().getSql());
    } finally {
      metaDataService.close();
    }
    return Result.success();
  }
}
