package org.dromara.dbswitch.admin.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreviewNameMapperResponse {

  private String originalName;
  private String targetName;
  private String typeName;
  private int fieldType;
  private boolean canIncrement;
}
