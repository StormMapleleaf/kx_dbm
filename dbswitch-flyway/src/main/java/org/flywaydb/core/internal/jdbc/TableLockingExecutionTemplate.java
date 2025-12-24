package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.internal.database.base.Table;

import java.util.concurrent.Callable;

public class TableLockingExecutionTemplate implements ExecutionTemplate {
    private final Table table;
    private final ExecutionTemplate executionTemplate;

    TableLockingExecutionTemplate(Table table, ExecutionTemplate executionTemplate) {
        this.table = table;
        this.executionTemplate = executionTemplate;
    }

    @Override
    public <T> T execute(final Callable<T> callback) {
        return executionTemplate.execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    table.lock();
                    return callback.call();
                } finally {
                    table.unlock();
                }
            }
        });
    }
}