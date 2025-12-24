package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class TransactionalExecutionTemplate implements ExecutionTemplate {
    private static final Log LOG = LogFactory.getLog(TransactionalExecutionTemplate.class);

        private final Connection connection;

        private final boolean rollbackOnException;

        TransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        this.connection = connection;
        this.rollbackOnException = rollbackOnException;
    }

        @Override
    public <T> T execute(Callable<T> callback) {
        boolean oldAutocommit = true;
        try {
            oldAutocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            T result = callback.call();
            connection.commit();
            return result;
        } catch (Exception e) {
            RuntimeException rethrow;
            if (e instanceof SQLException) {
                rethrow = new FlywaySqlException("Unable to commit transaction", (SQLException) e);
            } else if (e instanceof RuntimeException) {
                rethrow = (RuntimeException) e;
            } else {
                rethrow = new FlywayException(e);
            }

            if (rollbackOnException) {
                try {
                    LOG.debug("Rolling back transaction...");
                    connection.rollback();
                    LOG.debug("Transaction rolled back");
                } catch (SQLException se) {
                    LOG.error("Unable to rollback transaction", se);
                }
            } else {
                try {
                    connection.commit();
                } catch (SQLException se) {
                    LOG.error("Unable to commit transaction", se);
                }
            }
            throw rethrow;
        } finally {
            try {
                connection.setAutoCommit(oldAutocommit);
            } catch (SQLException e) {
                LOG.error("Unable to restore autocommit to original value for connection", e);
            }
        }
    }
}