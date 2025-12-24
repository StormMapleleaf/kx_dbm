package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class RedshiftType extends Type<RedshiftDatabase, RedshiftSchema> {
        public RedshiftType(JdbcTemplate jdbcTemplate, RedshiftDatabase database, RedshiftSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TYPE " + database.quote(schema.getName(), name));
    }
}