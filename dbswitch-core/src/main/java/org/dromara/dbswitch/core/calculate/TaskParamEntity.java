// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.core.calculate;

import org.dromara.dbswitch.common.type.ProductTypeEnum;
import org.dromara.dbswitch.core.provider.transform.RecordTransformProvider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * 任务参数实体类定义
 *
 * @author tang
 */
@Data
@Builder
@AllArgsConstructor
public class TaskParamEntity {

  /**
   * 老表的数据源
   */
  @NonNull
  private DataSource oldDataSource;

  /**
   * 老表的schema名
   */
  @NonNull
  private String oldSchemaName;

  /**
   * 老表的table名
   */
  @NonNull
  private String oldTableName;

  /**
   * 老表的数据库产品类型
   */
  @NonNull
  private ProductTypeEnum oldProductType;
  
  /**
   * 新表的数据源
   */
  @NonNull
  private DataSource newDataSource;

  /**
   * 新表的schema名
   */
  @NonNull
  private String newSchemaName;

  /**
   * 新表的table名
   */
  @NonNull
  private String newTableName;

  /**
   * 新表的数据库产品类型
   */
  @NonNull
  private ProductTypeEnum newProductType;

  /**
   * 字段列
   */
  private List<String> fieldColumns;

  /**
   * 字段名映射关系
   */
  @NonNull
  @Builder.Default
  private Map<String, String> columnsMap = Collections.emptyMap();

  /**
   * 值转换器
   */
  @NonNull
  private RecordTransformProvider transformer;
}
