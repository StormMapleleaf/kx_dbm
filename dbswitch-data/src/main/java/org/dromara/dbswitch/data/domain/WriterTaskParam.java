package org.dromara.dbswitch.data.domain;

import org.dromara.dbswitch.core.basic.exchange.MemChannel;
import org.dromara.dbswitch.core.basic.robot.RobotReader;
import org.dromara.dbswitch.core.basic.task.TaskParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriterTaskParam implements TaskParam {

  private MemChannel memChannel;
  private RobotReader robotReader;
  private boolean concurrentWrite;
}
