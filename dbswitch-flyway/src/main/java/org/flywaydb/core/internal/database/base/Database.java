package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywayDbUpgradeRequiredException;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.DatabaseType;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.sqlscript.Delimiter;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.AbbreviationUtils;

import java.io.Closeable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public abstract class Database<C extends Connection> implements Closeable {
    private static final Log LOG = LogFactory.getLog(Database.class);

        protected final DatabaseType databaseType;

        protected final Configuration configuration;

        protected final DatabaseMetaData jdbcMetaData;

        protected final java.sql.Connection rawMainJdbcConnection;

        private C mainConnection;

        private C migrationConnection;

    protected final JdbcConnectionFactory jdbcConnectionFactory;





        private MigrationVersion version;

        private String installedBy;

    protected JdbcTemplate jdbcTemplate;

        public Database(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        this.databaseType = jdbcConnectionFactory.getDatabaseType();
        this.configuration = configuration;
        this.rawMainJdbcConnection = jdbcConnectionFactory.openConnection();
        try {
            this.jdbcMetaData = rawMainJdbcConnection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to get metadata for connection", e);
        }
        this.jdbcTemplate = new JdbcTemplate(rawMainJdbcConnection, databaseType);
        this.jdbcConnectionFactory = jdbcConnectionFactory;



    }

        private C getConnection(java.sql.Connection connection) {
        return doGetConnection(connection);
    }

        protected abstract C doGetConnection(java.sql.Connection connection);

        public abstract void ensureSupported();

        public final MigrationVersion getVersion() {
        if (version == null) {
            version = determineVersion();
        }
        return version;
    }

    protected final void ensureDatabaseIsRecentEnough(String oldestSupportedVersion) {
        if (!getVersion().isAtLeast(oldestSupportedVersion)) {
            throw new FlywayDbUpgradeRequiredException(databaseType, computeVersionDisplayName(getVersion()),
                    computeVersionDisplayName(MigrationVersion.fromVersion(oldestSupportedVersion)));
        }
    }

        protected final void ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition(String oldestSupportedVersionInThisEdition,
                                                                                            Edition editionWhereStillSupported) {
        if (!getVersion().isAtLeast(oldestSupportedVersionInThisEdition)) {
            throw new FlywayEditionUpgradeRequiredException(
                    editionWhereStillSupported,
                    databaseType,
                    computeVersionDisplayName(getVersion()));
        }
    }

    protected final void recommendFlywayUpgradeIfNecessary(String newestSupportedVersion) {
        if (getVersion().isNewerThan(newestSupportedVersion)) {
            recommendFlywayUpgrade(newestSupportedVersion);
        }
    }

    protected final void recommendFlywayUpgradeIfNecessaryForMajorVersion(String newestSupportedVersion) {
        if (getVersion().isMajorNewerThan(newestSupportedVersion)) {
            recommendFlywayUpgrade(newestSupportedVersion);
        }
    }

    private void recommendFlywayUpgrade(String newestSupportedVersion) {
        String message = "Flyway upgrade recommended: " + databaseType + " " + computeVersionDisplayName(getVersion())
                + " is newer than this version of Flyway and support has not been tested. "
                + "The latest supported version of " + databaseType + " is " + newestSupportedVersion + ".";

        LOG.warn(message);
    }

        protected String computeVersionDisplayName(MigrationVersion version) {
        return version.getVersion();
    }

        public Delimiter getDefaultDelimiter() {
        return Delimiter.SEMICOLON;
    }

        public final String getCurrentUser() {
        try {
            return doGetCurrentUser();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error retrieving the database user", e);
        }
    }

    protected String doGetCurrentUser() throws SQLException {
        return jdbcMetaData.getUserName();
    }

        public abstract boolean supportsDdlTransactions();

        public boolean useDirectBaseline() {
        return false;
    }

        public abstract boolean supportsChangingCurrentSchema();













        public abstract String getBooleanTrue();

        public abstract String getBooleanFalse();

        public final String quote(String... identifiers) {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (String identifier : identifiers) {
            if (!first) {
                result.append(".");
            }
            first = false;
            result.append(doQuote(identifier));
        }

        return result.toString();
    }

        protected abstract String doQuote(String identifier);

        public abstract boolean catalogIsSchema();

        public boolean useSingleConnection() {
        return false;
    }

    public DatabaseMetaData getJdbcMetaData() {
        return jdbcMetaData;
    }

        public final C getMainConnection() {
        if (mainConnection == null) {
            this.mainConnection = getConnection(rawMainJdbcConnection);
        }
        return mainConnection;
    }

        public final C getMigrationConnection() {
        if (migrationConnection == null) {
            if (useSingleConnection()) {
                this.migrationConnection = getMainConnection();
            } else {
                this.migrationConnection = getConnection(jdbcConnectionFactory.openConnection());
            }
        }
        return migrationConnection;
    }

        protected MigrationVersion determineVersion() {
        try {
            return MigrationVersion.fromVersion(jdbcMetaData.getDatabaseMajorVersion() + "." + jdbcMetaData.getDatabaseMinorVersion());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the major version of the database", e);
        }
    }

        public final SqlScript getCreateScript(SqlScriptFactory sqlScriptFactory, Table table, boolean baseline) {
        return sqlScriptFactory.createSqlScript(new StringResource(getRawCreateScript(table, baseline)), false, null);
    }

    public abstract String getRawCreateScript(Table table, boolean baseline);

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

    public final String getBaselineStatement(Table table) {
        return String.format(getInsertStatement(table).replace("?", "%s"),
                1,
                "'" + configuration.getBaselineVersion() + "'",
                "'" + AbbreviationUtils.abbreviateDescription(configuration.getBaselineDescription()) + "'",
                "'" + MigrationType.BASELINE + "'",
                "'" + AbbreviationUtils.abbreviateScript(configuration.getBaselineDescription()) + "'",
                "NULL",
                "'" + installedBy + "'",
                0,
                getBooleanTrue()
        );
    }

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

    public final String getInstalledBy() {
        if (installedBy == null) {
            installedBy = configuration.getInstalledBy() == null ? getCurrentUser() : configuration.getInstalledBy();
        }
        return installedBy;
    }

    public void close() {
        if (!useSingleConnection() && migrationConnection != null) {
            migrationConnection.close();
        }
        if (mainConnection != null) {
            mainConnection.close();
        }
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

        public boolean supportsEmptyMigrationDescription() { return true; }

        public boolean supportsMultiStatementTransactions() { return true; }

        public void cleanPreSchemas() {
        try {
            doCleanPreSchemas();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean database " + this, e);
        }
    }

        protected void doCleanPreSchemas() throws SQLException {
        // Default is to do nothing.
    }

        public void cleanPostSchemas() {
        try {
            doCleanPostSchemas();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to clean schema " + this, e);
        }
    }

        protected void doCleanPostSchemas() throws SQLException {
        // Default is to do nothing
    }
}