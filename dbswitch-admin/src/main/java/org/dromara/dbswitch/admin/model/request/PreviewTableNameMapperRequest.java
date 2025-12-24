package org.dromara.dbswitch.admin.model.request;

import org.dromara.dbswitch.common.entity.PatternMapper;
import org.dromara.dbswitch.common.type.CaseConvertEnum;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PreviewTableNameMapperRequest {

  private Long id;
  private String schemaName;
  private Boolean isInclude;
  private List<String> tableNames;
  private List<PatternMapper> nameMapper;
  private CaseConvertEnum tableNameCase;
}
