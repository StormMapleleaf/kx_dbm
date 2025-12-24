package org.dromara.dbswitch.admin.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OnlineSqlDataRequest {

  private String script;

  private Integer page;

  private Integer size;
}
