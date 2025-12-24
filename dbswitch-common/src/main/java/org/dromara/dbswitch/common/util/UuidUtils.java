// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.util;

import java.util.UUID;
import lombok.experimental.UtilityClass;

/**
 * UUID工具类
 */
@UtilityClass
public final class UuidUtils {

  public static String generateUuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
