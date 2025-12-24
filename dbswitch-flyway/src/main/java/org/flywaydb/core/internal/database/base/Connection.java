package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public abstract class Connection<D extends Database> implements Closeable {
    protected final D database;
    protected final JdbcTemplate jdbcTemplate;
    private final java.sql.Connection jdbcConnection;

        protected final String originalSchemaNameOrSearchPath;

        private final boolean originalAutoCommit;

    protected Connection(D database, java.sql.Connection connection) {
        this.database = database;

        try {
            this.originalAutoCommit = connection.getAutoCommit();
            if (!originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to turn on auto-commit for the connection", e);
        }

        this.jdbcConnection = connection;
        jdbcTemplate = new JdbcTemplate(jdbcConnection, database.getDatabaseType());
        try {
            originalSchemaNameOrSearchPath = getCurrentSchemaNameOrSearchPath();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the original schema for the connection", e);
        }
    }

        protected abstract String getCurrentSchemaNameOrSearchPath() throws SQLException;

        public final Schema getCurrentSchema() {
        try {
            return doGetCurrentSchema();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the current schema for the connection", e);
        }
    }

    protected Schema doGetCurrentSchema() throws SQLException {
        return getSchema(getCurrentSchemaNameOrSearchPath());
    }

        public abstract Schema getSchema(String name);

        public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (!schema.exists()) {
                return;
            }
            doChangeCurrentSchemaOrSearchPathTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

        protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException {
    }

        public <T> T lock(final Table table, final Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
    }

    public final JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public final void close() {
        restoreOriginalState();
        restoreOriginalSchema();
        restoreOriginalAutoCommit();
        JdbcUtils.closeConnection(jdbcConnection);
    }

    private void restoreOriginalSchema() {
        ExecutionTemplateFactory.createExecutionTemplate(jdbcConnection, database).execute(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    doChangeCurrentSchemaOrSearchPathTo(originalSchemaNameOrSearchPath);
                } catch (SQLException e) {
                    throw new FlywaySqlException("Unable to restore original schema", e);
                }
                return null;
            }
        });
    }

        public final void restoreOriginalState() {
        try {
            doRestoreOriginalState();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original state", e);
        }
    }

        private void restoreOriginalAutoCommit() {
        try {
            jdbcConnection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original auto-commit setting", e);
        }
    }

        protected void doRestoreOriginalState() throws SQLException {
    }

    public final java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }
}