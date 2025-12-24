package org.dromara.dbswitch.data.domain;

import org.dromara.dbswitch.common.entity.CloseableDataSource;
import org.dromara.dbswitch.core.basic.exchange.MemChannel;
import org.dromara.dbswitch.core.basic.task.TaskParam;
import org.dromara.dbswitch.data.config.DbswichPropertiesConfiguration;
import org.dromara.dbswitch.core.schema.TableDescription;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReaderTaskParam implements TaskParam {

  private MemChannel memChannel;
  private TableDescription tableDescription;
  private DbswichPropertiesConfiguration configuration;
  private CloseableDataSource sourceDataSource;
  private CloseableDataSource targetDataSource;
  private Set<String> targetExistTables;
  private CountDownLatch countDownLatch;
}
