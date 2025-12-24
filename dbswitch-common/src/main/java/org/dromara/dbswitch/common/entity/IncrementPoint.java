// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.entity;

import org.apache.commons.lang3.StringUtils;

public class IncrementPoint {

  public static IncrementPoint EMPTY = new IncrementPoint();

  private String columnName;
  private Object maxValue;
  private int jdbcType;

  private IncrementPoint() {
  }

  public IncrementPoint(String columnName, Object maxValue, int jdbcType) {
    this.columnName = columnName;
    this.maxValue = maxValue;
    this.jdbcType = jdbcType;
  }

  public boolean isWorkable() {
    return StringUtils.isNotBlank(columnName) && null != maxValue;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public Object getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(Object maxValue) {
    this.maxValue = maxValue;
  }

  public int getJdbcType() {
    return jdbcType;
  }

  public void setJdbcType(int jdbcType) {
    this.jdbcType = jdbcType;
  }
}
