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
