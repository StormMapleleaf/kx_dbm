// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.product.oceanbase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class OceanbaseUtils {

  public static boolean isOceanBaseUseMysqlMode(Connection connection) {
    String sql = "show global variables where variable_name = 'ob_compatibility_mode'";
    try (PreparedStatement statement = connection.prepareStatement(sql)) {
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String value = resultSet.getString(2);
          if (Objects.nonNull(value)) {
            if (value.toUpperCase().contains("MYSQL")) {
              return true;
            } else {
              return false;
            }
          } else {
            throw new RuntimeException("Execute SQL[" + sql + "] return null value");
          }
        } else {
          throw new RuntimeException("Execute SQL[" + sql + "] no result");
        }
      }
    } catch (SQLException sqlException) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to execute sql :{}, and guesses OceanBase is MySQL Mode!", sql);
      }
    }
    return true;
  }
}
