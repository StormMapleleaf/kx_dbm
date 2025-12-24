// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.entity;

import java.util.Objects;
import org.slf4j.MDC;

public abstract class AbstractLogging {

  private final MdcKeyValue mdc;

  public AbstractLogging(MdcKeyValue mdc) {
    this.mdc = Objects.requireNonNull(mdc, "mdc is null");
  }

  protected void setupMdc() {
    MDC.put(mdc.getMdcKey(), mdc.getMdcValue());
  }

  protected void cleanMdc() {
    MDC.remove(mdc.getMdcKey());
  }
}
