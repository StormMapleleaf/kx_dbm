package org.dromara.dbswitch.admin.controller.privateapi;

import org.dromara.dbswitch.admin.common.annotation.TokenCheck;
import org.dromara.dbswitch.admin.common.response.Result;
import org.dromara.dbswitch.admin.config.SwaggerConfig;
import org.dromara.dbswitch.admin.model.request.PreviewColumnNameMapperRequest;
import org.dromara.dbswitch.admin.model.request.PreviewTableNameMapperRequest;
import org.dromara.dbswitch.admin.model.response.PreviewNameMapperResponse;
import org.dromara.dbswitch.admin.service.PatternMapperService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"映射关系预览"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/mapper")
public class PatternMapperController {

  @Resource
  private PatternMapperService patternMapperService;

  @TokenCheck
  @ApiOperation(value = "表名映射预览")
  @PostMapping(value = "/preview/table", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<List<PreviewNameMapperResponse>> previewTableNamesMapper(
      @RequestBody PreviewTableNameMapperRequest request) {
    return patternMapperService.previewTableNamesMapper(request);
  }

  @TokenCheck
  @ApiOperation(value = "字段名映射预览")
  @PostMapping(value = "/preview/column", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<List<PreviewNameMapperResponse>> previewColumnNamesMapper(
      @RequestBody PreviewColumnNameMapperRequest request) {
    return patternMapperService.previewColumnNamesMapper(request);
  }
}
