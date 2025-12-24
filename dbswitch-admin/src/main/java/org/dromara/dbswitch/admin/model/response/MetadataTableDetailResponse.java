package org.dromara.dbswitch.admin.model.response;

import org.dromara.dbswitch.core.schema.IndexDescription;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel("表元数据详情")
public class MetadataTableDetailResponse {

  private String tableName;
  private String schemaName;
  private String remarks;
  private String type;
  private String createSql;
  private List<String> primaryKeys;
  private List<MetadataColumnDetailResponse> columns;
  private List<IndexDescription> indexes;
}
