package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public abstract class SchemaObject<D extends Database, S extends Schema> {
        protected final JdbcTemplate jdbcTemplate;

        protected final D database;

        protected final S schema;

        protected final String name;

        SchemaObject(JdbcTemplate jdbcTemplate, D database, S schema, String name) {
        this.name = name;
        this.jdbcTemplate = jdbcTemplate;
        this.database = database;
        this.schema = schema;
    }

        public final S getSchema() {
        return schema;
    }

        public final String getName() {
        return name;
    }

        public final void drop() {
        try {
            doDrop();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to drop " + this, e);
        }
    }

        protected abstract void doDrop() throws SQLException;

    @Override
    public String toString() {
        return database.quote(schema.getName(), name);
    }
}