package org.dromara.dbswitch.admin.util;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Pair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public final class DBSqlUtils {

  @Data
  @Builder
  public static class ScriptExecuteResult {

    private boolean isSelect;
    private String sql;
    private String resultSummary;
    private List<Pair<String, String>> resultHeader;
    private List<Map<String, Object>> resultData;
  }

  public static ScriptExecuteResult execute(Connection connection, String sql, int page, int size)
      throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setQueryTimeout(300);
    statement.setFetchSize(isMySqlConnection(connection) ? Integer.MIN_VALUE : (size < 10) ? 100 : size);
    log.info("ExecuteSQL:{}\n{}", sql);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    boolean b = statement.execute();
    stopWatch.stop();
    String seconds = String.format("%.6f s", stopWatch.getTotalTimeSeconds());
    if (b) {
      int skipNumber = size * (page - 1);
      try (ResultSet rs = statement.getResultSet()) {
        List<Pair<String, String>> columns = new ArrayList<>();
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
          String columnName = rs.getMetaData().getColumnLabel(i);
          String columnTypeName = rs.getMetaData().getColumnTypeName(i);
          columns.add(Pair.of(columnName, columnTypeName));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          for (Pair<String, String> column : columns) {
            String columnName = column.getKey();
            Object columnValue = null;
            try {
              columnValue = rs.getObject(columnName);
            } catch (SQLException se) {
              log.warn("Failed to call jdbc ResultSet::getObject(): {}", se.getMessage(), se);
            }
            row.put(columnName, columnValue);
          }
          if (skipNumber <= 0) {
            list.add(row);
            if (list.size() >= size) {
              break;
            }
          } else {
            skipNumber--;
          }
        }
        return ScriptExecuteResult.builder()
            .isSelect(true)
            .sql(sql)
            .resultSummary("Time: " + seconds)
            .resultHeader(columns)
            .resultData(list)
            .build();
      }
    } else {
      int updateCount = statement.getUpdateCount();
      return ScriptExecuteResult.builder()
          .isSelect(false)
          .sql(sql)
          .resultSummary("affected : " + updateCount + ", Time: " + seconds)
          .resultHeader(Collections.emptyList())
          .resultData(Collections.emptyList())
          .build();
    }
  }

  private boolean isMySqlConnection(Connection connection) {
    try {
      String productName = connection.getMetaData().getDatabaseProductName();
      return productName.contains("MySQL") || productName.contains("MariaDB");
    } catch (Exception e) {
      return false;
    }
  }
}
