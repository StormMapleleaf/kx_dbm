package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.Results;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SybaseASEDatabase extends Database<SybaseASEConnection> {
    private static final Log LOG = LogFactory.getLog(SybaseASEDatabase.class);

    private String databaseName = null;
    private boolean supportsMultiStatementTransactions = false;

        public SybaseASEDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected SybaseASEConnection doGetConnection(Connection connection) {
        return new SybaseASEConnection(this, connection);
    }









    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("15.7");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("16.0", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessary("16.2");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table.getName() + " (\n" +
                "    installed_rank INT NOT NULL,\n" +
                "    version VARCHAR(50) NULL,\n" +
                "    description VARCHAR(200) NOT NULL,\n" +
                "    type VARCHAR(20) NOT NULL,\n" +
                "    script VARCHAR(1000) NOT NULL,\n" +
                "    checksum INT NULL,\n" +
                "    installed_by VARCHAR(100) NOT NULL,\n" +
                "    installed_on datetime DEFAULT getDate() NOT NULL,\n" +
                "    execution_time INT NOT NULL,\n" +
                "    success decimal NOT NULL,\n" +
                "    PRIMARY KEY (installed_rank)\n" +
                ")\n" +
                "lock datarows on 'default'\n" +
                (baseline ? getBaselineStatement(table) + "\n" : "") +
                "go\n" +
                "CREATE INDEX " + table.getName() + "_s_idx ON " + table.getName() + " (success)\n" +
                "go\n";
    }

    @Override
    public boolean supportsEmptyMigrationDescription() {
        return false;
    }

    @Override
    public Delimiter getDefaultDelimiter() {
        return Delimiter.GO;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT user_name()");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
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
        return identifier;
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
        public boolean supportsMultiStatementTransactions() {
        if (supportsMultiStatementTransactions) {
            LOG.debug("ddl in tran was found to be true at some point during execution." +
                    "Therefore multi statement transaction support is assumed.");
            return true;
        }

        boolean ddlInTran = getDdlInTranOption();

        if (ddlInTran) {
            LOG.debug("ddl in tran is true. Multi statement transaction support is now assumed.");
            supportsMultiStatementTransactions = true;
        }

        return supportsMultiStatementTransactions;
    }

    boolean getDdlInTranOption() {
        try {
            String databaseName = getDatabaseName();
            String getDatabaseMetadataQuery = "sp_helpdb " + databaseName + " -- ";
            Results results = getMainConnection().getJdbcTemplate().executeStatement(getDatabaseMetadataQuery);
            for (int resultsIndex = 0; resultsIndex < results.getResults().size(); resultsIndex++) {
                List<String> columns = results.getResults().get(resultsIndex).getColumns();
                if (columns != null) {
                    int statusIndex = getStatusIndex(columns);
                    if (statusIndex > -1) {
                        String options = results.getResults().get(resultsIndex).getData().get(0).get(statusIndex);
                        return (options.contains("ddl in tran"));
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    private int getStatusIndex(List<String> columns) {
        for (int statusIndex = 0; statusIndex < columns.size(); statusIndex++) {
            if ("status".equals(columns.get(statusIndex))) {
                return statusIndex;
            }
        }
        return -1;
    }

    String getDatabaseName() throws SQLException {
        if (databaseName == null) {
            databaseName = getMainConnection().getJdbcTemplate().queryForString("select db_name()");
        }
        return databaseName;
    }
}