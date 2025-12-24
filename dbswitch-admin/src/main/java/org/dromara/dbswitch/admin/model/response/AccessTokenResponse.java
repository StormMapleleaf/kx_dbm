package org.dromara.dbswitch.admin.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("TOKEN信息")
public class AccessTokenResponse {

  @ApiModelProperty("实际名称")
  private String realName;

  @ApiModelProperty("token字符串")
  private String accessToken;

  @ApiModelProperty("有效时间(单位秒)")
  private Long expireSeconds;
}
