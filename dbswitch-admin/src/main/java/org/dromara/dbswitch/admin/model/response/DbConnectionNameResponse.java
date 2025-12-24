// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("连接名称")
public class DbConnectionNameResponse {

  @ApiModelProperty("ID编号")
  private Long id;

  @ApiModelProperty("名称")
  private String name;

  @ApiModelProperty("数据库类型名")
  private String typeName;

  @ApiModelProperty("类型")
  private Boolean useSql;
}
