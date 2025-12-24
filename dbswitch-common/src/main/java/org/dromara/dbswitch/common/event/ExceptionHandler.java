package org.dromara.dbswitch.common.event;

public interface ExceptionHandler {

  void handleException(ListenedEvent event, Throwable throwable);
}
