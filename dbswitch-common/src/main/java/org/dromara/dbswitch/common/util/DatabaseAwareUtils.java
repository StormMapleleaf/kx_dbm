package org.dromara.dbswitch.common.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class DatabaseAwareUtils {


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
