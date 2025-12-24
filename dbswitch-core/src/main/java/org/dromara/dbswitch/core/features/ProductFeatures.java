// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.core.features;

/**
 * 数据库特征定义
 */
public interface ProductFeatures {

  /**
   * FetchSize转换
   *
   * @param fetchSize
   * @return 转换后的fetchSize
   */
  int convertFetchSize(int fetchSize);

}
