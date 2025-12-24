// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.dbswitch.common.event;

import com.google.common.eventbus.Subscribe;
import java.util.function.Consumer;

public class EventSubscriber {

  private Consumer<ListenedEvent> handler;

  public EventSubscriber(Consumer<ListenedEvent> handler) {
    this.handler = handler;
  }

  @Subscribe
  public void handleEvent(ListenedEvent event) {
    handler.accept(event);
  }

}
