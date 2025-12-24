package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.mysql.MySQLDatabase;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SnowflakeDatabase extends Database<SnowflakeConnection> {
    private static final Log LOG = LogFactory.getLog(SnowflakeDatabase.class);

        private final boolean quotedIdentifiersIgnoreCase;

        public SnowflakeDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );

        quotedIdentifiersIgnoreCase = getQuotedIdentifiersIgnoreCase(jdbcTemplate);
        if (quotedIdentifiersIgnoreCase) {
            LOG.warn("Current Flyway history table can't be used with QUOTED_IDENTIFIERS_IGNORE_CASE option on");
        }
    }

    private static boolean getQuotedIdentifiersIgnoreCase(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, String>> result = jdbcTemplate.queryForList("SHOW PARAMETERS LIKE 'QUOTED_IDENTIFIERS_IGNORE_CASE'");
            Map<String, String> row = result.get(0);
            return "TRUE".equals(row.get("value").toUpperCase());
        } catch (SQLException e) {
            LOG.warn("Could not query for parameter QUOTED_IDENTIFIERS_IGNORE_CASE.");
            return false;
        }
    }

    @Override
    protected SnowflakeConnection doGetConnection(Connection connection) {
        return new SnowflakeConnection(this, connection);
    }









    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("3.0");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("3", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessaryForMajorVersion("4.2");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        return "CREATE TABLE " + table + " (\n" +
                quote("installed_rank") + " NUMBER(38,0) NOT NULL,\n" +
                quote("version") + " VARCHAR(50),\n" +
                quote("description") + " VARCHAR(200),\n" +
                quote("type") + " VARCHAR(20) NOT NULL,\n" +
                quote("script") + " VARCHAR(1000) NOT NULL,\n" +
                quote("checksum") + " NUMBER(38,0),\n" +
                quote("installed_by") + " VARCHAR(100) NOT NULL,\n" +
                quote("installed_on") + " TIMESTAMP_LTZ(9) NOT NULL DEFAULT CURRENT_TIMESTAMP(),\n" +
                quote("execution_time") + " NUMBER(38,0) NOT NULL,\n" +
                quote("success") + " BOOLEAN NOT NULL,\n" +
                "primary key (" + quote("installed_rank") + "));\n" +

                (baseline ? getBaselineStatement(table) + ";\n" : "");
    }

    @Override
    public String getSelectStatement(Table table) {
        return "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                + " WHERE " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
    }

    @Override
    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        return "true";
    }

    @Override
    public String getBooleanFalse() {
        return "false";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}