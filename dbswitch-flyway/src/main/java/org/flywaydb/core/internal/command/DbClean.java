package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

import java.util.concurrent.Callable;

public class DbClean {
    private static final Log LOG = LogFactory.getLog(DbClean.class);

        private final Connection connection;

        private final SchemaHistory schemaHistory;

        private final Schema[] schemas;

        private final CallbackExecutor callbackExecutor;

        private boolean cleanDisabled;

        private Database database;

        public DbClean(Database database, SchemaHistory schemaHistory, Schema[] schemas,
                   CallbackExecutor callbackExecutor, boolean cleanDisabled) {
        this.database = database;
        this.connection = database.getMainConnection();
        this.schemaHistory = schemaHistory;
        this.schemas = schemas;
        this.callbackExecutor = callbackExecutor;
        this.cleanDisabled = cleanDisabled;
    }

        public void clean() throws FlywayException {
        if (cleanDisabled) {
            throw new FlywayException("Unable to execute clean as it has been disabled with the \"flyway.cleanDisabled\" property.");
        }
        callbackExecutor.onEvent(Event.BEFORE_CLEAN);

        try {
            connection.changeCurrentSchemaTo(schemas[0]);
            boolean dropSchemas = false;
            try {
                dropSchemas = schemaHistory.hasSchemasMarker();
            } catch (Exception e) {
                LOG.error("Error while checking whether the schemas should be dropped", e);
            }

            dropDatabaseObjectsPreSchemas();

            for (Schema schema : schemas) {
                if (!schema.exists()) {
                    LOG.warn("Unable to clean unknown schema: " + schema);
                    continue;
                }

                if (dropSchemas) {
                    dropSchema(schema);
                } else {
                    cleanSchema(schema);
                }
            }

            dropDatabaseObjectsPostSchemas();

        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_CLEAN_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_CLEAN);
        schemaHistory.clearCache();
    }

        private void dropDatabaseObjectsPreSchemas() {
        LOG.debug("Dropping pre-schema database level objects...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                    database).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    database.cleanPreSchemas();
                    return null;
                }
            });
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop pre-schema database level objects");
        }
        stopWatch.stop();
        LOG.info(String.format("Successfully dropped pre-schema database level objects (execution time %s)",
                TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }

        private void dropDatabaseObjectsPostSchemas() {
        LOG.debug("Dropping post-schema database level objects...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                    database).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    database.cleanPostSchemas();
                    return null;
                }
            });
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop post-schema database level objects");
        }
        stopWatch.stop();
        LOG.info(String.format("Successfully dropped post-schema database level objects (execution time %s)",
                TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }

        private void dropSchema(final Schema schema) {
        LOG.debug("Dropping schema " + schema + " ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                    database).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    schema.drop();
                    return null;
                }
            });
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop schema " + schema + ". Attempting clean instead...");
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                    database).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    schema.clean();
                    return null;
                }
            });
        }
        stopWatch.stop();
        LOG.info(String.format("Successfully dropped schema %s (execution time %s)",
                schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }

        private void cleanSchema(final Schema schema) {
        LOG.debug("Cleaning schema " + schema + " ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                database).execute(new Callable<Object>() {
            @Override
            public Void call() {
                schema.clean();
                return null;
            }
        });
        stopWatch.stop();
        LOG.info(String.format("Successfully cleaned schema %s (execution time %s)",
                schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }
}