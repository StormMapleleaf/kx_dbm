package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class RedshiftTable extends Table<RedshiftDatabase, RedshiftSchema> {
        RedshiftTable(JdbcTemplate jdbcTemplate, RedshiftDatabase database, RedshiftSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                        "  SELECT 1\n" +
                        "  FROM   pg_catalog.pg_class c\n" +
                        "  JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n" +
                        "  WHERE  n.nspname = ?\n" +
                        "  AND    c.relname = ?\n" +
                        "  AND    c.relkind = 'r'\n" + 
                        ")", schema.getName(),
                name.toLowerCase() 
        );
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("DELETE FROM " + this + " WHERE FALSE");
    }
}