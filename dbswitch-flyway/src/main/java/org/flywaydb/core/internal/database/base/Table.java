package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Table<D extends Database, S extends Schema> extends SchemaObject<D, S> {
    private static final Log LOG = LogFactory.getLog(Table.class);

        protected int lockDepth = 0;

        public Table(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

        public boolean exists() {
        try {
            return doExists();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check whether table " + this + " exists", e);
        }
    }

        protected abstract boolean doExists() throws SQLException;

        protected boolean exists(Schema catalog, Schema schema, String table, String... tableTypes) throws SQLException {
        String[] types = tableTypes;
        if (types.length == 0) {
            types = null;
        }

        ResultSet resultSet = null;
        boolean found;
        try {
            resultSet = database.jdbcMetaData.getTables(
                    catalog == null ? null : catalog.getName(),
                    schema == null ? null : schema.getName(),
                    table,
                    types);
            found = resultSet.next();
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return found;
    }

        public void lock() {
        if (!exists()) {
            return;
        }
        try {
            doLock();
            lockDepth++;
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to lock table " + this, e);
        }
    }

        protected abstract void doLock() throws SQLException;

        public void unlock() {
        if (!exists() || lockDepth == 0) {
            return;
        }
        try {
            doUnlock();
            lockDepth--;
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to unlock table " + this, e);
        }
    }

        protected void doUnlock() throws SQLException {
    };
}