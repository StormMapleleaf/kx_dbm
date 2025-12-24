package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class PostgreSQLType extends Type<PostgreSQLDatabase, PostgreSQLSchema> {
        public PostgreSQLType(JdbcTemplate jdbcTemplate, PostgreSQLDatabase database, PostgreSQLSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}