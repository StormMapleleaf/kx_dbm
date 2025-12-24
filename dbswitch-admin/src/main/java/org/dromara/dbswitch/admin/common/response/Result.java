package org.dromara.dbswitch.admin.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@ApiModel(description = "响应结果")
@AllArgsConstructor
@Data
public class Result<T> {

  private static final String SUCCESS = "success";

  @ApiModelProperty("状态码")
  private Integer code;

  @ApiModelProperty("状态描述")
  private String message;

  @ApiModelProperty("数据")
  private T data;

  public static <T> Result success() {
    return new Result(0, SUCCESS, null);
  }

  public static <T> Result success(T data) {
    return new Result<>(0, SUCCESS, data);
  }

  public static Result failed(ResultCode code) {
    return new Result(code.getCode(), code.getMessage(), null);
  }

  public static Result failed(ResultCode code, String message) {
    return new Result(code.getCode(), code.getMessage() + ":" + message, null);
  }

}
