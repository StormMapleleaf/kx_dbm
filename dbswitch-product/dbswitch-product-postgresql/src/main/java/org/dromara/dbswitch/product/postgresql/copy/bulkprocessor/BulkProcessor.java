package org.dromara.dbswitch.product.postgresql.copy.bulkprocessor;

import org.dromara.dbswitch.product.postgresql.copy.bulkprocessor.handler.IBulkWriteHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BulkProcessor<TEntity> implements AutoCloseable {

  private final ScheduledThreadPoolExecutor scheduler;

  private final ScheduledFuture<?> scheduledFuture;

  private volatile boolean closed = false;

  private final IBulkWriteHandler<TEntity> handler;

  private final int bulkSize;

  private List<TEntity> batchedEntities;

  public BulkProcessor(IBulkWriteHandler<TEntity> handler, int bulkSize) {
    this(handler, bulkSize, null);
  }

  public BulkProcessor(IBulkWriteHandler<TEntity> handler, int bulkSize, Duration flushInterval) {

    this.handler = handler;
    this.bulkSize = bulkSize;

        this.batchedEntities = new ArrayList<>();

    if (flushInterval != null) {
            this.scheduler = new ScheduledThreadPoolExecutor(1);
      this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
      this.scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
      this.scheduledFuture = this.scheduler
          .scheduleWithFixedDelay(new Flush(), flushInterval.toMillis(), flushInterval.toMillis(),
              TimeUnit.MILLISECONDS);
    } else {
      this.scheduler = null;
      this.scheduledFuture = null;
    }
  }

  public synchronized BulkProcessor<TEntity> add(TEntity entity) {
    batchedEntities.add(entity);
    executeIfNeccessary();
    return this;
  }

  @Override
  public void close() throws Exception {
        if (closed) {
      return;
    }
    closed = true;

        Optional.ofNullable(this.scheduledFuture).ifPresent(future -> future.cancel(false));
    Optional.ofNullable(this.scheduler).ifPresent(ScheduledThreadPoolExecutor::shutdown);

        if (batchedEntities.size() > 0) {
      execute();
    }
  }

  private void executeIfNeccessary() {
    if (batchedEntities.size() >= bulkSize) {
      execute();
    }
  }

    private void execute() {
        final List<TEntity> entities = batchedEntities;
        batchedEntities = new ArrayList<>();
        write(entities);
  }

  private void write(List<TEntity> entities) {
    try {
      handler.write(entities);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  class Flush implements Runnable {

    @Override
    public void run() {
      synchronized (BulkProcessor.this) {
        if (closed) {
          return;
        }
        if (batchedEntities.size() == 0) {
          return;
        }
        execute();
      }

    }
  }
}