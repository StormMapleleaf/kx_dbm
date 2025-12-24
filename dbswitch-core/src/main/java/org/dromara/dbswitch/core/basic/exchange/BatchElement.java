package org.dromara.dbswitch.core.basic.exchange;

import org.dromara.dbswitch.common.entity.ThreeArgsFunction;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchElement {

  private String tableNameMapString;

  private ThreeArgsFunction<List<String>, List<Object[]>, org.slf4j.Logger, Long> handler;

  private List<String> arg1;

  private List<Object[]> arg2;
}
