package org.flywaydb.core.api.executor;

import java.sql.SQLException;

public interface MigrationExecutor {
        void execute(Context context) throws SQLException;

        boolean canExecuteInTransaction();
}