package org.dromara.dbswitch.admin.common.exception;

import org.dromara.dbswitch.admin.common.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbswitchException extends RuntimeException {

  private ResultCode code;
  private String message;

}
