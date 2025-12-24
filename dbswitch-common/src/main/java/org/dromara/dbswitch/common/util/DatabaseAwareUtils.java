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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库类型识别工具类
 *
 * @author tang
 */
@Slf4j
@UtilityClass
public final class DatabaseAwareUtils {

  /**
   * 检查MySQL数据库表的存储引擎是否为Innodb
   *
   * @param schemaName schema名
   * @param tableName  table名
   * @param dataSource 数据源
   * @return 为Innodb存储引擎时返回True, 否在为false
   */
  public static boolean isMysqlInnodbStorageEngine(String schemaName, String tableName,
      DataSource dataSource) {
    String sql = "SELECT count(*) as total FROM information_schema.tables "
        + "WHERE table_schema=? AND table_name=? AND ENGINE='InnoDB'";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, schemaName);
      ps.setString(2, tableName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }

      return false;
    } catch (SQLException se) {
      throw new RuntimeException(se);
    }
  }

}
