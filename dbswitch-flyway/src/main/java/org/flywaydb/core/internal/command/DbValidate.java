package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

import java.util.concurrent.Callable;

public class DbValidate {
    private static final Log LOG = LogFactory.getLog(DbValidate.class);

        private final SchemaHistory schemaHistory;

        private final Schema schema;

        private final MigrationResolver migrationResolver;

        private final Connection connection;

        private final Configuration configuration;

        private final boolean pending;

        private final CallbackExecutor callbackExecutor;

    private final Database database;

        public DbValidate(Database database, SchemaHistory schemaHistory, Schema schema, MigrationResolver migrationResolver,
                      Configuration configuration, boolean pending, CallbackExecutor callbackExecutor) {
        this.database = database;
        this.connection = database.getMainConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.pending = pending;
        this.callbackExecutor = callbackExecutor;
    }

        public String validate() {
        if (!schema.exists()) {
            if (!migrationResolver.resolveMigrations(new Context() {
                @Override
                public Configuration getConfiguration() {
                    return configuration;
                }
            }).isEmpty() && !pending) {
                return "Schema " + schema + " doesn't exist yet";
            }
            return null;
        }

        callbackExecutor.onEvent(Event.BEFORE_VALIDATE);

        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Pair<Integer, String> result = ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                database).execute(new Callable<Pair<Integer, String>>() {
            @Override
            public Pair<Integer, String> call() {
                MigrationInfoServiceImpl migrationInfoService =
                        new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                                configuration.getTarget(),
                                configuration.isOutOfOrder(),
                                pending,
                                configuration.isIgnoreMissingMigrations(),
                                configuration.isIgnoreIgnoredMigrations(),
                                configuration.isIgnoreFutureMigrations());

                migrationInfoService.refresh();

                int count = migrationInfoService.all().length;
                String validationError = migrationInfoService.validate();
                return Pair.of(count, validationError);
            }
        });

        stopWatch.stop();

        String error = result.getRight();
        if (error == null) {
            int count = result.getLeft();
            if (count == 1) {
                LOG.info(String.format("Successfully validated 1 migration (execution time %s)",
                        TimeFormat.format(stopWatch.getTotalTimeMillis())));
            } else {
                LOG.info(String.format("Successfully validated %d migrations (execution time %s)",
                        count, TimeFormat.format(stopWatch.getTotalTimeMillis())));

                if (count == 0) {
                    LOG.warn("No migrations found. Are your locations set up correctly?");
                }
            }
            callbackExecutor.onEvent(Event.AFTER_VALIDATE);
        } else {
            callbackExecutor.onEvent(Event.AFTER_VALIDATE_ERROR);
        }


        return error;
    }
}