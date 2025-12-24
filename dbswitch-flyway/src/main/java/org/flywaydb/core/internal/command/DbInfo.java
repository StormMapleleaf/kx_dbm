package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

public class DbInfo {
    private final MigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Configuration configuration;
    private final CallbackExecutor callbackExecutor;

    public DbInfo(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                  Configuration configuration, CallbackExecutor callbackExecutor) {

        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.configuration = configuration;
        this.callbackExecutor = callbackExecutor;
    }

    public MigrationInfoService info() {
        callbackExecutor.onEvent(Event.BEFORE_INFO);


        MigrationInfoServiceImpl migrationInfoService;
        try {
            migrationInfoService =
                    new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                            configuration.getTarget(), configuration.isOutOfOrder(),
                            true, true, true, true);
            migrationInfoService.refresh();
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_INFO_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_INFO);

        return migrationInfoService;
    }
}