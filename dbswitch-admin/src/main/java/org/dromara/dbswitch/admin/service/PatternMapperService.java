package org.dromara.dbswitch.admin.service;

import org.dromara.dbswitch.admin.common.exception.DbswitchException;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.common.response.ResultCode;
import org.dromara.dbswitch.admin.model.request.PreviewColumnNameMapperRequest;
import org.dromara.dbswitch.admin.model.request.PreviewTableNameMapperRequest;
import org.dromara.dbswitch.admin.model.response.PreviewNameMapperResponse;
import org.dromara.dbswitch.common.util.JdbcTypesUtils;
import org.dromara.dbswitch.common.util.PatterNameUtils;
import org.dromara.dbswitch.core.schema.ColumnDescription;
import org.dromara.dbswitch.core.schema.TableDescription;
import org.dromara.dbswitch.core.service.MetadataService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PatternMapperService {

  private final String STRING_EMPTY = "<!空>";
  private final String STRING_DELETE = "<!删除>";

  @Resource
  private ConnectionService connectionService;

  public Result<List<PreviewNameMapperResponse>> previewTableNamesMapper(
      PreviewTableNameMapperRequest request) {
    boolean include = true;
    if (null != request.getIsInclude() && !request.getIsInclude()) {
      include = false;
    }
    List<PreviewNameMapperResponse> result = new ArrayList<>();
    if (CollectionUtils.isEmpty(request.getTableNames())) {
      for (TableDescription td : getAllTableNames(request)) {
        String targetName = PatterNameUtils.getFinalName(
            td.getTableName(), request.getNameMapper());
        result.add(PreviewNameMapperResponse.builder()
            .originalName(td.getTableName())
            .targetName(StringUtils.isNotBlank(targetName) ? targetName : STRING_EMPTY)
            .build());
      }
    } else {
      if (include) {
        for (String name : request.getTableNames()) {
          if (StringUtils.isNotBlank(name)) {
            String targetName = PatterNameUtils.getFinalName(
                name, request.getNameMapper());
            result.add(PreviewNameMapperResponse.builder()
                .originalName(name)
                .targetName(StringUtils.isNotBlank(targetName) ? targetName : STRING_EMPTY)
                .build());
          }
        }
      } else {
        for (TableDescription td : getAllTableNames(request)) {
          if (!request.getTableNames().contains(td.getTableName())) {
            result.add(PreviewNameMapperResponse.builder()
                .originalName(td.getTableName())
                .targetName(
                    PatterNameUtils.getFinalName(td.getTableName(), request.getNameMapper()))
                .build());
          }
        }
      }
    }
    result.forEach(
        r -> r.setTargetName(request.getTableNameCase().convert(r.getTargetName()))
    );
    return Result.success(result);
  }

  public Result<List<PreviewNameMapperResponse>> previewColumnNamesMapper(
      PreviewColumnNameMapperRequest request) {
    if (null == request.getId() || StringUtils.isBlank(request.getSchemaName())
        || StringUtils.isBlank(request.getTableName())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT,
          "id or schemaName or tableName");
    }

    List<PreviewNameMapperResponse> result = new ArrayList<>();
    MetadataService service = connectionService.getMetaDataCoreService(request.getId());
    try {
      List<ColumnDescription> tables = service.queryTableColumnMeta(request.getSchemaName(),
          request.getTableName());
      for (ColumnDescription cd : tables) {
        String targetName = PatterNameUtils.getFinalName(cd.getFieldName(), request.getNameMapper());
        if (StringUtils.isNotBlank(targetName)) {
          result.add(PreviewNameMapperResponse.builder()
              .originalName(cd.getFieldName())
              .targetName(targetName)
              .typeName(cd.getFieldTypeName())
              .fieldType(cd.getFieldType())
              .canIncrement(JdbcTypesUtils.isIncrement(cd.getFieldType()))
              .build());
        } else {
          result.add(PreviewNameMapperResponse.builder()
              .originalName(cd.getFieldName())
              .targetName(STRING_DELETE)
              .typeName(cd.getFieldTypeName())
              .fieldType(cd.getFieldType())
              .canIncrement(false)
              .build());
        }
      }

      result.forEach(
          r -> r.setTargetName(request.getColumnNameCase().convert(r.getTargetName()))
      );
      return Result.success(result);
    } finally {
      service.close();
    }
  }

  private List<TableDescription> getAllTableNames(PreviewTableNameMapperRequest request) {
    if (null == request.getId() || StringUtils.isBlank(request.getSchemaName())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT, "id or schemaName");
    }
    MetadataService service = connectionService.getMetaDataCoreService(request.getId());
    try {
      return service.queryTableList(request.getSchemaName()).stream().filter(td -> !td.isViewTable())
          .collect(Collectors.toList());
    } finally {
      service.close();
    }
  }

}
