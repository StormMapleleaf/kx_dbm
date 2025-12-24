package org.dromara.dbswitch.admin.model.ops;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpsTaskJobTrend {

  @ApiModelProperty("日期")
  private String dateOfDay;

  @ApiModelProperty("作业总数")
  private Integer countOfJob;

  @ApiModelProperty("任务总数")
  private Integer countOfTask;
}
