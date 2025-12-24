package org.dromara.dbswitch.admin.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AssignmentSearchRequest {

  private String searchText;
  private Integer page;
  private Integer size;
}
