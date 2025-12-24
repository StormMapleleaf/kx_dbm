package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Schema<D extends Database, T extends Table> {
    private static final Log LOG = LogFactory.getLog(Schema.class);

        protected final JdbcTemplate jdbcTemplate;

        protected final D database;

        protected final String name;

        public Schema(JdbcTemplate jdbcTemplate, D database, String name) {
        this.jdbcTemplate = jdbcTemplate;
        this.database = database;
        this.name = name;
    }

        public String getName() {
        return name;
    }

        public boolean exists() {
        try {
            return doExists();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check whether schema " + this + " exists", e);
        }
    }

        protected abstract boolean doExists() throws SQLException;

        public boolean empty() {
        try {
            return doEmpty();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to check whether schema " + this + " is empty", e);
        }
    }

        protected abstract boolean doEmpty() throws SQLException;

        public void create() {
        try {
            LOG.info("Creating schema " + this + " ...");
            doCreate();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to create schema " + this, e);
        }
    }

        protected abstract void doCreate() throws SQLException;

        public void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to drop schema " + this, e);
        }
    }

        protected abstract void doDrop() throws SQLException;

        public void clean() {
        try {
            doClean();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean schema " + this, e);
        }
    }

        protected abstract void doClean() throws SQLException;

        public T[] allTables() {
        try {
            return doAllTables();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve all tables in schema " + this, e);
        }
    }

        protected abstract T[] doAllTables() throws SQLException;

        protected final Type[] allTypes() {
        ResultSet resultSet = null;
        try {
            resultSet = database.jdbcMetaData.getUDTs(null, name, null, null);

            List<Type> types = new ArrayList<>();
            while (resultSet.next()) {
                types.add(getType(resultSet.getString("TYPE_NAME")));
            }

            return types.toArray(new Type[0]);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve all types in schema " + this, e);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }
    }

        protected Type getType(String typeName) {
        return null;
    }

        public abstract Table getTable(String tableName);

        public Function getFunction(String functionName, String... args) {
        throw new UnsupportedOperationException("getFunction()");
    }

        protected final Function[] allFunctions() {
        try {
            return doAllFunctions();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve all functions in schema " + this, e);
        }
    }

        protected Function[] doAllFunctions() throws SQLException {
        return new Function[0];
    }

        @Override
    public String toString() {
        return database.quote(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;
        return name.equals(schema.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}