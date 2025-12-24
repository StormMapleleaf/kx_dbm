package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class CockroachDBDatabase extends Database<CockroachDBConnection> {
        public CockroachDBDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected CockroachDBConnection doGetConnection(Connection connection) {
        return new CockroachDBConnection(this, connection);
    }












    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("1.1");
        recommendFlywayUpgradeIfNecessary("20.1");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL PRIMARY KEY,\n" +
                "    \"version\" VARCHAR(50),\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INTEGER,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n" +
                "    \"execution_time\" INTEGER NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ");\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX \"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");";
    }

    @Override
    protected MigrationVersion determineVersion() {
        String version;
        try {
            version = getMainConnection().getJdbcTemplate().queryForString("SELECT value FROM crdb_internal.node_build_info where field='Version'");
            if (version == null) {
                version = getMainConnection().getJdbcTemplate().queryForString("SELECT value FROM crdb_internal.node_build_info where field='Tag'");
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine CockroachDB version", e);
        }
        int firstDot = version.indexOf(".");
        int majorVersion = Integer.parseInt(version.substring(1, firstDot));
        String minorPatch = version.substring(firstDot + 1);
        int minorVersion = Integer.parseInt(minorPatch.substring(0, minorPatch.indexOf(".")));
        return MigrationVersion.fromVersion(majorVersion + "." + minorVersion);
    }

    public String getDbName() {
        return "cockroachdb";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT * FROM [SHOW SESSION_USER]");
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }








    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}