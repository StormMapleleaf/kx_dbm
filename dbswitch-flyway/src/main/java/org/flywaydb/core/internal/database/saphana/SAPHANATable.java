package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class SAPHANATable extends Table<SAPHANADatabase, SAPHANASchema> {
        SAPHANATable(JdbcTemplate jdbcTemplate, SAPHANADatabase database, SAPHANASchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.update("lock table " + this + " in exclusive mode");
    }
}