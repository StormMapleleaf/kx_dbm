package org.dromara.dbswitch.data.entity;

import lombok.Data;


@Data
public class GlobalParamConfigProperties {

  private int channelQueueSize;

  private int writeThreadNum;

  public GlobalParamConfigProperties() {
    this.channelQueueSize = 100;
    this.writeThreadNum = getDefaultWriteThreadNum();
  }

  private int getDefaultWriteThreadNum() {
    int availableProcessorCount = Runtime.getRuntime().availableProcessors();
    return Math.min(Math.max(4, availableProcessorCount), 8);
  }

}
