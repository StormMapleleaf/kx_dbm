package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class FirebirdDatabase extends Database<FirebirdConnection> {
        public FirebirdDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected FirebirdConnection doGetConnection(Connection connection) {
        return new FirebirdConnection( this, connection);
    }











    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("3.0");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    protected String doQuote(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String createScript = "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INTEGER CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INTEGER,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INTEGER NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL\n" +
                ");\n" +
                "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");\n";

        if (baseline) {
            createScript += "COMMIT RETAIN;\n" +
                    getBaselineStatement(table) + ";\n";

        }

        return createScript;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("select CURRENT_USER from RDB$DATABASE");
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }
}