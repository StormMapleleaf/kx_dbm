package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.internal.database.base.SchemaObject;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class SybaseASETable extends Table<SybaseASEDatabase, SybaseASESchema> {
        SybaseASETable(JdbcTemplate jdbcTemplate, SybaseASEDatabase database, SybaseASESchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForString("SELECT object_id('" + name + "')") != null;
    }

    @Override
    protected void doLock() throws SQLException {
        if (database.supportsMultiStatementTransactions()) {
            jdbcTemplate.execute("LOCK TABLE " + this + " IN EXCLUSIVE MODE");
        }
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + getName());
    }

        @Override
    public String toString() {
        return name;
    }
}