package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class SQLiteTable extends Table<SQLiteDatabase, SQLiteSchema> {
    private static final Log LOG = LogFactory.getLog(SQLiteTable.class);

        static final String SQLITE_SEQUENCE = "sqlite_sequence";
    private final boolean undroppable;

        public SQLiteTable(JdbcTemplate jdbcTemplate, SQLiteDatabase database, SQLiteSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
        undroppable = SQLITE_SEQUENCE.equals(name);
    }

    @Override
    protected void doDrop() throws SQLException {
        if (undroppable) {
            LOG.debug("SQLite system table " + this + " cannot be dropped. Ignoring.");
        } else {
            String dropSql = "DROP TABLE " + database.quote(schema.getName(), name);
            if (getSchema().getForeignKeysEnabled()) {
                dropSql = "PRAGMA foreign_keys = OFF; " + dropSql + "; PRAGMA foreign_keys = ON";
            }
            jdbcTemplate.execute(dropSql);
        }
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(tbl_name) FROM "
                + database.quote(schema.getName()) + ".sqlite_master WHERE type='table' AND tbl_name='" + name + "'") > 0;
    }

    @Override
    protected void doLock() {
        LOG.debug("Unable to lock " + this + " as SQLite does not support locking. No concurrent migration supported.");
    }
}