package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.callback.Error;

import java.util.List;

public interface CallbackExecutor {
        void onEvent(Event event);

        void onMigrateOrUndoEvent(Event event);

        void setMigrationInfo(MigrationInfo migrationInfo);

        void onEachMigrateOrUndoEvent(Event event);












}