package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;

import java.sql.Connection;

public class ExecutionTemplateFactory {
        public static ExecutionTemplate createExecutionTemplate(Connection connection) {
        return createTransactionalExecutionTemplate(connection, true);
    }

        public static ExecutionTemplate createExecutionTemplate(Connection connection, Database database) {
        if (database.supportsMultiStatementTransactions()) {
            return createTransactionalExecutionTemplate(connection, true);
        }

        return new PlainExecutionTemplate();
    }

        public static ExecutionTemplate createTableExclusiveExecutionTemplate(Connection connection, Table table, Database database) {
        if (database.supportsMultiStatementTransactions()) {
            return new TableLockingExecutionTemplate(table, createTransactionalExecutionTemplate(connection, database.supportsDdlTransactions()));
        }

        return new TableLockingExecutionTemplate(table, new PlainExecutionTemplate());
    }

        private static ExecutionTemplate createTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        DatabaseType databaseType = DatabaseType.fromJdbcConnection(connection);

        if (DatabaseType.COCKROACHDB.equals(databaseType)) {
            return new CockroachRetryingTransactionalExecutionTemplate(connection, rollbackOnException);
        }

        return new TransactionalExecutionTemplate(connection, rollbackOnException);
    }
}