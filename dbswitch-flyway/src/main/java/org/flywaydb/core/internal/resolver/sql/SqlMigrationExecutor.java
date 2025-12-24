package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.internal.database.DatabaseExecutionStrategy;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.cockroachdb.CockroachDBRetryingStrategy;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

public class SqlMigrationExecutor implements MigrationExecutor {
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;

        private final SqlScript sqlScript;













        SqlMigrationExecutor(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScript sqlScript



    ) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.sqlScript = sqlScript;




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

    private void executeOnce(Context context) {
        sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection()



        ).execute(sqlScript);
    }

    @Override
    public boolean canExecuteInTransaction() {
        return sqlScript.executeInTransaction();
    }
}