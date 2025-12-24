package org.flywaydb.core.internal.resolver.java;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBRetryingStrategy;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.Connection;
import java.sql.SQLException;

public class JavaMigrationExecutor implements MigrationExecutor {
        private final JavaMigration javaMigration;

        JavaMigrationExecutor(JavaMigration javaMigration) {
        this.javaMigration = javaMigration;
    }

    @Override
    public void execute(final Context context) throws SQLException {
        DatabaseExecutionStrategy strategy = DatabaseFactory.createExecutionStrategy(context.getConnection());
        strategy.execute(new SqlCallable<Boolean>() {
                @Override
                public Boolean call() throws SQLException {
                    executeOnce(context);
                    return true;
                }
            });
    }

    private void executeOnce(final Context context) throws SQLException {
        try {
            javaMigration.migrate(new org.flywaydb.core.api.migration.Context() {
                @Override
                public Configuration getConfiguration() {
                    return context.getConfiguration();
                }

                @Override
                public Connection getConnection() {
                    return context.getConnection();
                }
            });
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    @Override
    public boolean canExecuteInTransaction() {
        return javaMigration.canExecuteInTransaction();
    }
}