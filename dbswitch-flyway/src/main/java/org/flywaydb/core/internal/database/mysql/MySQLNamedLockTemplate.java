package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class MySQLNamedLockTemplate {
    private static final Log LOG = LogFactory.getLog(MySQLNamedLockTemplate.class);

        private final JdbcTemplate jdbcTemplate;

    private final String lockName;

        MySQLNamedLockTemplate(JdbcTemplate jdbcTemplate, int discriminator) {
        this.jdbcTemplate = jdbcTemplate;
        lockName = "Flyway-" + discriminator;
    }

        public <T> T execute(Callable<T> callable) {
        try {
            lock();
            return callable.call();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to acquire MySQL named lock: " + lockName, e);
        } catch (Exception e) {
            RuntimeException rethrow;
            if (e instanceof RuntimeException) {
                rethrow = (RuntimeException) e;
            } else {
                rethrow = new FlywayException(e);
            }
            throw rethrow;
        } finally {
            try {
                jdbcTemplate.execute("SELECT RELEASE_LOCK('" + lockName + "')");
            } catch (SQLException e) {
                LOG.error("Unable to release MySQL named lock: " + lockName, e);
            }
        }
    }

    private void lock() throws SQLException {
        while (!tryLock()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new FlywayException("Interrupted while attempting to acquire MySQL named lock: " + lockName, e);
            }
        }
    }

    private boolean tryLock() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT GET_LOCK(?,10)", lockName) == 1;
    }
}