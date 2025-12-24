package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultCallbackExecutor implements CallbackExecutor {
    private final Configuration configuration;
    private final Database database;
    private final Schema schema;
    private final Collection<Callback> callbacks;
    private MigrationInfo migrationInfo;

        public DefaultCallbackExecutor(Configuration configuration, Database database, Schema schema, Collection<Callback> callbacks) {
        this.configuration = configuration;
        this.database = database;
        this.schema = schema;
        this.callbacks = callbacks;
    }

    @Override
    public void onEvent(final Event event) {
        execute(event, database.getMainConnection());
    }

    @Override
    public void onMigrateOrUndoEvent(final Event event) {
        execute(event, database.getMigrationConnection());
    }

    @Override
    public void setMigrationInfo(MigrationInfo migrationInfo) {
        this.migrationInfo = migrationInfo;
    }

    @Override
    public void onEachMigrateOrUndoEvent(Event event) {
        final Context context = new SimpleContext(configuration, database.getMigrationConnection(), migrationInfo);
        for (Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                callback.handle(event, context);
            }
        }
    }














    private void execute(final Event event, final Connection connection) {
        final Context context = new SimpleContext(configuration, connection, null);

        for (final Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                if (callback.canHandleInTransaction(event, context)) {
                    ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                            database).execute(new Callable<Void>() {
                        @Override
                        public Void call() {
                            DefaultCallbackExecutor.this.execute(connection, callback, event, context);
                            return null;
                        }
                    });
                } else {
                    execute(connection, callback, event, context);
                }
            }
        }
    }

    private void execute(Connection connection, Callback callback, Event event, Context context) {
        connection.restoreOriginalState();
        connection.changeCurrentSchemaTo(schema);
        handleEvent(callback, event, context);
    }

    private void handleEvent(Callback callback, Event event, Context context) {
        try {
            callback.handle(event, context);
        } catch (RuntimeException e) {
            throw new FlywayException("Error while executing " + event.getId() + " callback: " + e.getMessage(), e);
        }
    }
}