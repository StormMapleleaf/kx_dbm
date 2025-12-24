package org.dromara.dbswitch.common.event;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class TaskEventHub {

  private String identifier;
  private ExecutorService executor;
  private List<EventSubscriber> eventSubscribers;
  private ExceptionHandler exceptionHandler;

  public TaskEventHub(String identifier, int poolSize, ExceptionHandler handler) {
    this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
    this.executor = new ThreadPoolExecutor(
        poolSize,
        poolSize,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(),
        new BasicThreadFactory.Builder()
            .namingPattern(this.identifier + "-%d")
            .build()
    );
    this.eventSubscribers = new CopyOnWriteArrayList<>();
    this.exceptionHandler = handler;
  }

  public void registerSubscriber(EventSubscriber subscriber) {
    this.eventSubscribers.add(subscriber);
  }

  public void notifyEvent(@Nullable Object... args) {
    ListenedEvent event = new ListenedEvent(this, identifier, args);
    for (EventSubscriber subscriber : Lists.newArrayList(eventSubscribers)) {
      this.executor.submit(() -> {
        try {
          subscriber.handleEvent(event);
        } catch (Throwable e) {
          exceptionHandler.handleException(event, e);
        }
      });
    }
  }
}
