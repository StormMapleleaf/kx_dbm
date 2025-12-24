package org.dromara.dbswitch.admin.model.request;

import org.dromara.dbswitch.common.entity.PatternMapper;
import org.dromara.dbswitch.common.type.CaseConvertEnum;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PreviewColumnNameMapperRequest {

  private Long id;
  private String schemaName;
  private String tableName;
  private List<PatternMapper> nameMapper;
  private CaseConvertEnum columnNameCase;
}
