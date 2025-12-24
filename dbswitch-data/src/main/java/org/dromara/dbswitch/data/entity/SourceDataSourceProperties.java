// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.data.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.dromara.dbswitch.common.entity.PatternMapper;
import org.dromara.dbswitch.common.type.ProductTypeEnum;

/**
 * 源端参数配置
 *
 * @author tang
 */
@Data
public class SourceDataSourceProperties {

  private ProductTypeEnum type;
  private String url;
  private String driverClassName;
  private String username;
  private String password;
  private String driverPath;
  private Long connectionTimeout = TimeUnit.SECONDS.toMillis(60);
  private Long maxLifeTime = TimeUnit.MINUTES.toMillis(60);

  private String beforeSqlScripts;
  private String afterSqlScripts;

  private Integer fetchSize = 5000;
  private String sourceSchema = "";
  private String tableType = "TABLE";
  private String sourceIncludes = "";
  private String sourceExcludes = "";
  private Map<String, String> incrTableColumns = Collections.emptyMap();
  private List<PatternMapper> regexTableMapper = Collections.emptyList();
  private List<PatternMapper> regexColumnMapper = Collections.emptyList();
}
