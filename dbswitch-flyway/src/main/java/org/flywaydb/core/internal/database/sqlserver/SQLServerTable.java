package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class SQLServerTable extends Table<SQLServerDatabase, SQLServerSchema> {
    private final String databaseName;

        SQLServerTable(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, SQLServerSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        this.databaseName = databaseName;
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + this);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean(
                "SELECT CAST(" +
                        "CASE WHEN EXISTS(" +
                        "  SELECT 1 FROM [" + databaseName + "].INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?" +
                        ") " +
                        "THEN 1 ELSE 0 " +
                        "END " +
                        "AS BIT)",
                schema.getName(), name);
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("select * from " + this + " WITH (TABLOCKX)");
    }

        void dropSystemVersioningIfPresent() throws SQLException {
                if (database.supportsTemporalTables()) {
            if (jdbcTemplate.queryForInt("SELECT temporal_type FROM sys.tables WHERE object_id = OBJECT_ID('" + this + "', 'U')") == 2) {
                jdbcTemplate.execute("ALTER TABLE " + this + " SET (SYSTEM_VERSIONING = OFF)");
            }
        }
    }

    @Override
    public String toString() {
        return database.quote(databaseName, schema.getName(), name);
    }
}