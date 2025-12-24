package org.dromara.dbswitch.data.domain;

import org.dromara.dbswitch.common.entity.PrintablePerfStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WriterPerfStat extends PrintablePerfStat {

  private long duration;

  @Override
  public String getPrintableString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Total Writer Duration: \t" + (duration / 1000.0) + " s \n");
    return sb.toString();
  }
}
