package org.dromara.dbswitch.data.domain;

import org.dromara.dbswitch.core.basic.task.TaskResult;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderTaskResult implements TaskResult {

  @Builder.Default
  private Map<String, Long> perf = new HashMap<>();

  @Builder.Default
  private Map<String, Throwable> except = new HashMap<>();

  private String tableNameMapString;

  private long successCount;

  private long failureCount;

  private long recordCount;

  private long totalBytes;

  private Throwable throwable;

  @Override
  public void padding() {
    if (successCount > 0 && null != tableNameMapString) {
      perf.put(tableNameMapString, recordCount);
    }
    if (null != throwable && null != tableNameMapString) {
      except.putIfAbsent(tableNameMapString, throwable);
    }
  }
}
