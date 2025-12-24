package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class DB2Type extends Type<DB2Database, DB2Schema> {
        DB2Type(JdbcTemplate jdbcTemplate, DB2Database database, DB2Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}