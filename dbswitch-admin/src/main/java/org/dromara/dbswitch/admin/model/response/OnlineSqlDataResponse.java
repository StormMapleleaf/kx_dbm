package org.dromara.dbswitch.admin.model.response;

import io.swagger.annotations.ApiModel;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel("SQL脚本结果数据内容")
public class OnlineSqlDataResponse {

  private List<SqlInput> summaries;

  private List<SqlResult> results;

  @Data
  @Builder
  public static class SqlInput {

    private String sql;

    private String summary;
  }

  @Data
  @Builder
  public static class SqlResult {

    private List<ColumnItem> columns;

    private List<Map<String, Object>> rows;
  }

  @Data
  @Builder
  public static class ColumnItem {

    private String columnName;

    private String columnType;
  }
}
