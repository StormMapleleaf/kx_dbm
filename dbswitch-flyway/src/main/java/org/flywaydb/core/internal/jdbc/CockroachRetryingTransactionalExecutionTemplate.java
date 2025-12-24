package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class CockroachRetryingTransactionalExecutionTemplate extends TransactionalExecutionTemplate {
    private static final Log LOG = LogFactory.getLog(CockroachRetryingTransactionalExecutionTemplate.class);

    private static final String DEADLOCK_OR_TIMEOUT_ERROR_CODE = "40001";
    private static final int MAX_RETRIES = 50;

        CockroachRetryingTransactionalExecutionTemplate(Connection connection, boolean rollbackOnException) {
        super(connection, rollbackOnException);
    }

        @Override
    public <T> T execute(Callable<T> transactionCallback) {
        int retryCount = 0;
        while (true) {
            try {
                return transactionCallback.call();
            } catch (SQLException e) {
                if (!DEADLOCK_OR_TIMEOUT_ERROR_CODE.equals(e.getSQLState()) || retryCount >= MAX_RETRIES) {
                    LOG.info("error: " + e);
                    throw new FlywayException(e);
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new FlywayException(e);
            }
            retryCount++;
        }
    }
}