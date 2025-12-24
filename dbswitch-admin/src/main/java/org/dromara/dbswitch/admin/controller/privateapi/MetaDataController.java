package org.dromara.dbswitch.admin.controller.privateapi;

import org.dromara.dbswitch.admin.common.annotation.TokenCheck;
import org.dromara.dbswitch.admin.common.response.PageResult;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.config.SwaggerConfig;
import org.dromara.dbswitch.admin.model.request.OnlineSqlDataRequest;
import org.dromara.dbswitch.admin.model.response.MetadataSchemaDetailResponse;
import org.dromara.dbswitch.admin.model.response.MetadataTableDetailResponse;
import org.dromara.dbswitch.admin.model.response.MetadataTableInfoResponse;
import org.dromara.dbswitch.admin.model.response.OnlineSqlDataResponse;
import org.dromara.dbswitch.admin.model.response.SchemaTableDataResponse;
import org.dromara.dbswitch.admin.service.MetaDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"元数据查询接口"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/metadata")
public class MetaDataController {

  @Resource
  private MetaDataService metaDataService;

  @TokenCheck
  @ApiOperation(value = "模式列表")
  @GetMapping(value = "/schemas/{id}/{page}/{size}", produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResult<MetadataSchemaDetailResponse> allSchemas(
      @PathVariable("id") Long id,
      @PathVariable(value = "page", required = false) Integer page,
      @PathVariable(value = "size", required = false) Integer size) {
    return metaDataService.allSchemas(id, page, size);
  }

  @TokenCheck
  @ApiOperation(value = "物理表/视图表列表")
  @GetMapping(value = "/tables/{id}/{page}/{size}", produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResult<MetadataTableInfoResponse> allTables(
      @PathVariable("id") Long id,
      @RequestParam("schema") String schema,
      @PathVariable(value = "page", required = false) Integer page,
      @PathVariable(value = "size", required = false) Integer size) {
    return metaDataService.allTables(id, schema, page, size);
  }

  @TokenCheck
  @ApiOperation(value = "物理表/视图表信息")
  @GetMapping(value = "/meta/table/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<MetadataTableDetailResponse> tableMeta(
      @PathVariable("id") Long id,
      @RequestParam("schema") String schema,
      @RequestParam("table") String table) {
    return metaDataService.tableDetail(id, schema, table);
  }

  @TokenCheck
  @ApiOperation(value = "物理表/视图表的数据内容")
  @GetMapping(value = "/data/table/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<SchemaTableDataResponse> tableData(
      @PathVariable("id") Long id,
      @RequestParam("schema") String schema,
      @RequestParam("table") String table) {
    return metaDataService.tableData(id, schema, table);
  }

  @TokenCheck
  @ApiOperation(value = "SQL脚本结果集数据内容")
  @PostMapping(value = "/data/sql/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<OnlineSqlDataResponse> sqlData(
      @PathVariable("id") Long id,
      @RequestBody OnlineSqlDataRequest request) {
    return metaDataService.sqlData(id, request);
  }

}
