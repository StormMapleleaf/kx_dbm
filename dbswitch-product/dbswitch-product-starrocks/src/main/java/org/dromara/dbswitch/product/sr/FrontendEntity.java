package org.dromara.dbswitch.product.sr;

import lombok.Data;

@Data
public class FrontendEntity {

  private String ip;
  private String httpport;
  private Boolean alive;
  private Boolean join;
  private String role;
}
