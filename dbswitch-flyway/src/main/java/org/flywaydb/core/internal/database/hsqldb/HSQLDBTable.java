package org.flywaydb.core.internal.database.hsqldb;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class HSQLDBTable extends Table<HSQLDBDatabase, HSQLDBSchema> {
    private static final Log LOG = LogFactory.getLog(HSQLDBTable.class);








        HSQLDBTable(JdbcTemplate jdbcTemplate, HSQLDBDatabase database, HSQLDBSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);




    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return exists(null, schema, name);
    }

    @Override
    protected void doLock() throws SQLException {






        jdbcTemplate.execute("LOCK TABLE " + this + " WRITE");
    }
}