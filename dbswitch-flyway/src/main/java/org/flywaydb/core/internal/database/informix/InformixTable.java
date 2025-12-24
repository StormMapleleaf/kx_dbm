package org.flywaydb.core.internal.database.informix;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class InformixTable extends Table<InformixDatabase, InformixSchema> {
        InformixTable(JdbcTemplate jdbcTemplate, InformixDatabase database, InformixSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.update("lock table " + this + " in exclusive mode");
    }

    @Override
    public String toString() {
        return name;
    }
}