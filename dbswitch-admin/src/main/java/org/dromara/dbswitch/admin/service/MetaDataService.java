package org.dromara.dbswitch.admin.service;

import org.dromara.dbswitch.admin.common.response.PageResult;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.model.request.OnlineSqlDataRequest;
import org.dromara.dbswitch.admin.model.response.MetadataColumnDetailResponse;
import org.dromara.dbswitch.admin.model.response.MetadataSchemaDetailResponse;
import org.dromara.dbswitch.admin.model.response.MetadataTableDetailResponse;
import org.dromara.dbswitch.admin.model.response.MetadataTableInfoResponse;
import org.dromara.dbswitch.admin.model.response.OnlineSqlDataResponse;
import org.dromara.dbswitch.admin.model.response.OnlineSqlDataResponse.ColumnItem;
import org.dromara.dbswitch.admin.model.response.OnlineSqlDataResponse.SqlInput;
import org.dromara.dbswitch.admin.model.response.OnlineSqlDataResponse.SqlResult;
import org.dromara.dbswitch.admin.model.response.SchemaTableDataResponse;
import org.dromara.dbswitch.admin.util.DBSqlUtils;
import org.dromara.dbswitch.admin.util.DBSqlUtils.ScriptExecuteResult;
import org.dromara.dbswitch.admin.util.PageUtils;
import org.dromara.dbswitch.admin.entity.DatabaseConnectionEntity;
import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.schema.SchemaTableData;
import org.dromara.dbswitch.core.schema.SchemaTableMeta;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.service.MetadataService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

@Service
public class MetaDataService {

  @Resource
  private ConnectionService connectionService;

  public PageResult<MetadataSchemaDetailResponse> allSchemas(Long id, Integer page, Integer size) {
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(id);
    MetadataService metaDataService = connectionService.getMetaDataCoreService(dbConn);
    try {
      List<String> schemas = metaDataService.querySchemaList();
      List<MetadataSchemaDetailResponse> responses = schemas.stream()
          .map(s -> new MetadataSchemaDetailResponse(dbConn.getName(), s))
          .collect(Collectors.toList());
      return PageUtils.getPage(responses, page, size);
    } finally {
      metaDataService.close();
    }
  }

  public PageResult<MetadataTableInfoResponse> allTables(Long id, String schema, Integer page,
      Integer size) {
    MetadataService metaDataService = connectionService.getMetaDataCoreService(id);
    try {
      List<TableDescription> tables = metaDataService.queryTableList(schema);
      List<MetadataTableInfoResponse> responses = tables.stream()
          .map(one -> MetadataTableInfoResponse.builder()
              .tableName(one.getTableName())
              .schemaName(one.getSchemaName())
              .remarks(one.getRemarks())
              .type(one.getTableType())
              .build()
          ).collect(Collectors.toList());
      return PageUtils.getPage(responses, page, size);
    } finally {
      metaDataService.close();
    }
  }

  public Result<MetadataTableDetailResponse> tableDetail(Long id, String schema, String table) {
    MetadataService metaDataService = connectionService.getMetaDataCoreService(id);
    try {
      SchemaTableMeta tableMeta = metaDataService.queryTableMeta(schema, table);
      List<String> pks = tableMeta.getPrimaryKeys();
      List<MetadataColumnDetailResponse> columnDetailResponses = tableMeta.getColumns().stream()
          .map(one -> MetadataColumnDetailResponse.builder()
              .fieldName(one.getFieldName())
              .typeName(one.getFieldTypeName())
              .typeClassName(one.getFiledTypeClassName())
              .fieldType(String.valueOf(one.getFieldType()))
              .displaySize(String.valueOf(one.getDisplaySize()))
              .precisionSize(String.valueOf(one.getPrecisionSize()))
              .scaleSize(String.valueOf(one.getScaleSize()))
              .isPrimaryKey(
                  toStr(
                      CollectionUtils.isNotEmpty(pks)
                          && pks.contains(one.getFieldName())))
              .isAutoIncrement(toStr(one.isAutoIncrement()))
              .isNullable(toStr(one.isNullable()))
              .remarks(one.getRemarks())
              .build()
          ).collect(Collectors.toList());
      return Result.success(MetadataTableDetailResponse.builder()
          .tableName(tableMeta.getTableName())
          .schemaName(tableMeta.getSchemaName())
          .remarks(tableMeta.getRemarks())
          .type(tableMeta.getTableType())
          .createSql(tableMeta.getCreateSql())
          .primaryKeys(tableMeta.getPrimaryKeys())
          .columns(columnDetailResponses)
          .indexes(tableMeta.getIndexes())
          .build());
    } finally {
      metaDataService.close();
    }
  }

  public Result<SchemaTableDataResponse> tableData(Long id, String schema, String table) {
    MetadataService metaDataService = connectionService.getMetaDataCoreService(id);
    try {
      SchemaTableData data = metaDataService.queryTableData(schema, table, 10);
      List<String> headers = data.getColumns().stream()
          .map(one -> specialReplace(one))
          .collect(Collectors.toList());
      return Result.success(SchemaTableDataResponse.builder()
          .schemaName(data.getSchemaName())
          .tableName(data.getTableName())
          .columns(headers)
          .rows(convertRows(headers, data.getRows()))
          .build()
      );
    } finally {
      metaDataService.close();
    }
  }

  public Result<OnlineSqlDataResponse> sqlData(Long id, OnlineSqlDataRequest request) {
    DatabaseConnectionEntity databaseConn = connectionService.getDatabaseConnectionById(id);
    ProductTypeEnum productType = databaseConn.getType();
    if (!productType.isUseSql()) {
      throw new RuntimeException("不支持的SQL操作数据库类型:" + productType.name());
    }
    List<String> statements = new ArrayList<>();
    ScriptUtils.splitSqlScript(
        null,
        request.getScript(),
        ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
        ScriptUtils.DEFAULT_COMMENT_PREFIX,
        ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER,
        statements);
    Integer page = Optional.ofNullable(request.getPage()).orElse(1);
    Integer size = Optional.ofNullable(request.getSize()).orElse(100);
    try (CloseableDataSource dataSource = connectionService.getDataSource(databaseConn)) {
      try (Connection connection = dataSource.getConnection()) {
        List<SqlInput> summaries = new ArrayList<>(statements.size());
        List<SqlResult> results = new ArrayList<>(statements.size());
        for (String sql : statements) {
          try {
            ScriptExecuteResult result = DBSqlUtils.execute(connection, sql, page, size);
            summaries.add(SqlInput.builder().sql(sql).summary(result.getResultSummary()).build());
            if (CollectionUtils.isNotEmpty(result.getResultHeader())) {
              results.add(
                  SqlResult.builder()
                      .columns(result.getResultHeader().stream()
                          .map(one ->
                              ColumnItem.builder()
                                  .columnName(specialReplace(one.getKey()))
                                  .columnType(one.getValue())
                                  .build()
                          ).collect(Collectors.toList()))
                      .rows(convertRows(result.getResultData()))
                      .build());
            }
          } catch (Exception e) {
            summaries.add(
                SqlInput.builder()
                    .sql(sql)
                    .summary(e.getMessage())
                    .build());
          }
        }
        return Result.success(
            OnlineSqlDataResponse.builder()
                .summaries(summaries)
                .results(results)
                .build());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String specialReplace(String str) {
    if (null == str || str.isEmpty()) {
      return str;
    }
    return str.replaceAll("\\.", "_");
  }

  private List<Map<String, Object>> convertRows(List<String> columns, List<List<Object>> rows) {
    if (null == rows || rows.isEmpty()) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> result = new ArrayList<>(rows.size());
    for (List<Object> row : rows) {
      Map<String, Object> map = new LinkedHashMap<>();
      for (int i = 0; i < columns.size(); ++i) {
        Object v = row.get(i);
        map.put(columns.get(i), Objects.nonNull(v) ? v.toString() : null);
      }
      result.add(map);
    }
    return result;
  }

  private List<Map<String, Object>> convertRows(List<Map<String, Object>> rows) {
    if (null == rows || rows.isEmpty()) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> result = new ArrayList<>(rows.size());
    for (Map<String, Object> row : rows) {
      Map<String, Object> map = new LinkedHashMap<>();
      for (Map.Entry<String, Object> entry : row.entrySet()) {
        Object v = entry.getValue();
        map.put(entry.getKey(), Objects.nonNull(v) ? v.toString() : null);
      }
      result.add(map);
    }
    return result;
  }

  private String toStr(Boolean value) {
    if (null == value) {
      return "未知";
    }
    if (value) {
      return "是";
    }

    return "否";
  }

}
