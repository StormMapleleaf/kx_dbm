// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.core.schema;

/**
 * 数据库表列的值
 *
 * @author tang
 */
public class ColumnValue {

  private int jdbcType;
  private Object value;

  public ColumnValue(int jdbcType, Object value) {
    this.jdbcType = jdbcType;
    this.value = value;
  }

  public int getJdbcType() {
    return jdbcType;
  }

  public Object getValue() {
    return value;
  }

}
