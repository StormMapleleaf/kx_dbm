// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.admin.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobStatusEnum {

  INIT(0, "待执行"),
  RUNNING(1, "执行中"),
  FAIL(2, "执行异常"),
  PASS(3, "执行成功"),
  CANCEL(4, "任务取消"),
  ;

  private int value;
  private String name;

  public static JobStatusEnum of(String name) {
    for (JobStatusEnum status : JobStatusEnum.values()) {
      if (status.name().equalsIgnoreCase(name)) {
        return status;
      }
    }

    throw new IllegalArgumentException("cannot find enum name: " + name);
  }

  public static JobStatusEnum of(int value) {
    for (JobStatusEnum status : JobStatusEnum.values()) {
      if (status.value == value) {
        return status;
      }
    }

    throw new IllegalArgumentException("cannot find enum value: " + value);
  }

}
