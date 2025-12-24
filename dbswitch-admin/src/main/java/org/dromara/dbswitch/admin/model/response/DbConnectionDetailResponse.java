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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.dbswitch.common.type.ProductTypeEnum;

@NoArgsConstructor
@Data
@ApiModel("连接详情")
public class DbConnectionDetailResponse {

  @ApiModelProperty("ID编号")
  private Long id;

  @ApiModelProperty("标题")
  private String name;

  @ApiModelProperty("数据库类型")
  private ProductTypeEnum type;

  @ApiModelProperty("数据库类型名称")
  private String typeName;

  @ApiModelProperty("驱动版本")
  private String version;

  @ApiModelProperty("驱动类")
  private String driver;

  @ApiModelProperty("连接地址")
  private String address;

  @ApiModelProperty("连接端口号")
  private String port;

  @ApiModelProperty("数据库名")
  private String databaseName;

  @ApiModelProperty("编码格式")
  private String characterEncoding;

  @ApiModelProperty("URL连接串")
  private String url;

  @ApiModelProperty("账号名")
  private String username;

  @ApiModelProperty("密码")
  private String password;

  @ApiModelProperty("创建时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Timestamp createTime;

  @ApiModelProperty("更新时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Timestamp updateTime;
}
