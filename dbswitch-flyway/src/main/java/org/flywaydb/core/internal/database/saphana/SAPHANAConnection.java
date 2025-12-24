package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

public class SAPHANAConnection extends Connection<SAPHANADatabase> {
    SAPHANAConnection(SAPHANADatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUMMY");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.doQuote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new SAPHANASchema(jdbcTemplate, database, name);
    }
}