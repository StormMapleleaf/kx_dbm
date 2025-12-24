package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

class JdbcTableSchemaHistory extends SchemaHistory {
    private static final Log LOG = LogFactory.getLog(JdbcTableSchemaHistory.class);

    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
    private final SqlScriptFactory sqlScriptFactory;

        private final Database database;

        private final Connection<?> connection;

    private final JdbcTemplate jdbcTemplate;

        private final LinkedList<AppliedMigration> cache = new LinkedList<>();

        JdbcTableSchemaHistory(SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory,
                           Database database, Table table) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.sqlScriptFactory = sqlScriptFactory;
        this.table = table;
        this.database = database;
        this.connection = database.getMainConnection();
        this.jdbcTemplate = connection.getJdbcTemplate();
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public boolean exists() {
        connection.restoreOriginalState();

        return table.exists();
    }

    @Override
    public void create(final boolean baseline) {
        connection.lock(table, new Callable<Object>() {
            @Override
            public Object call() {
                int retries = 0;
                while (!exists()) {
                    if (retries == 0) {
                        LOG.info("Creating Schema History table " + table + (baseline ? " with baseline" : "") + " ...");
                    }
                    try {
                        ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(),
                                database).execute(new Callable<Object>() {
                            @Override
                            public Object call() {
                                sqlScriptExecutorFactory.createSqlScriptExecutor(connection.getJdbcConnection()



                                ).execute(database.getCreateScript(sqlScriptFactory, table, baseline));
                                LOG.debug("Created Schema History table " + table + (baseline ? " with baseline" : ""));
                                return null;
                            }
                        });
                    } catch (FlywayException e) {
                        if (++retries >= 10) {
                            throw e;
                        }
                        try {
                            LOG.debug("Schema History table creation failed. Retrying in 1 sec ...");
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public <T> T lock(Callable<T> callable) {
        connection.restoreOriginalState();

        return connection.lock(table, callable);
    }

    @Override
    protected void doAddAppliedMigration(int installedRank, MigrationVersion version, String description,
                                         MigrationType type, String script, Integer checksum,
                                         int executionTime, boolean success) {
        boolean tableIsLocked = false;
        connection.restoreOriginalState();

        if (!database.supportsDdlTransactions()) {
            table.lock();
            tableIsLocked = true;
        }

        try {
            String versionStr = version == null ? null : version.toString();

            if (!database.supportsEmptyMigrationDescription() && "".equals(description)) {
                description = NO_DESCRIPTION_MARKER;
            }

            jdbcTemplate.update(database.getInsertStatement(table),
                    installedRank, versionStr, description, type.name(), script, checksum, database.getInstalledBy(),
                    executionTime, success);

            LOG.debug("Schema History table " + table + " successfully updated to reflect changes");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to insert row for version '" + version + "' in Schema History table " + table, e);
        } finally {
            if (tableIsLocked) {
                table.unlock();
            }
        }
    }

    @Override
    public List<AppliedMigration> allAppliedMigrations() {
        if (!exists()) {
            return new ArrayList<>();
        }

        refreshCache();
        return cache;
    }

    private void refreshCache() {
        int maxCachedInstalledRank = cache.isEmpty() ? -1 : cache.getLast().getInstalledRank();

        String query = database.getSelectStatement(table);

        try {
            cache.addAll(jdbcTemplate.query(query, new RowMapper<AppliedMigration>() {
                public AppliedMigration mapRow(final ResultSet rs) throws SQLException {
                    Integer checksum = rs.getInt("checksum");
                    if (rs.wasNull()) {
                        checksum = null;
                    }

                    String type = rs.getString("type");
                    if ("SPRING_JDBC".equals(type)) {
                        type = "JDBC";
                    }
                    if ("UNDO_SPRING_JDBC".equals(type)) {
                        type = "UNDO_JDBC";
                    }

                    return new AppliedMigration(
                            rs.getInt("installed_rank"),
                            rs.getString("version") != null ? MigrationVersion.fromVersion(rs.getString("version")) : null,
                            rs.getString("description"),
                            MigrationType.valueOf(type),
                            rs.getString("script"),
                            checksum,
                            rs.getTimestamp("installed_on"),
                            rs.getString("installed_by"),
                            rs.getInt("execution_time"),
                            rs.getBoolean("success")
                    );
                }
            }, maxCachedInstalledRank));
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while retrieving the list of applied migrations from Schema History table "
                    + table, e);
        }
    }

    @Override
    public void removeFailedMigrations() {
        if (!exists()) {
            LOG.info("Repair of failed migration in Schema History table " + table + " not necessary as table doesn't exist.");
            return;
        }

        boolean failed = false;
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        for (AppliedMigration appliedMigration : appliedMigrations) {
            if (!appliedMigration.isSuccess()) {
                failed = true;
            }
        }
        if (!failed) {
            LOG.info("Repair of failed migration in Schema History table " + table + " not necessary. No failed migration detected.");
            return;
        }

        try {
            clearCache();
            jdbcTemplate.execute("DELETE FROM " + table
                    + " WHERE " + database.quote("success") + " = " + database.getBooleanFalse());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table " + table, e);
        }
    }

    @Override
    public void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration) {
        connection.restoreOriginalState();

        clearCache();

        MigrationVersion version = appliedMigration.getVersion();

        String description = resolvedMigration.getDescription();
        Integer checksum = resolvedMigration.getChecksum();
        MigrationType type = appliedMigration.getType().isSynthetic()
                ? appliedMigration.getType()
                : resolvedMigration.getType();

        LOG.info("Repairing Schema History table for version " + version
                + " (Description: " + description + ", Type: " + type + ", Checksum: " + checksum + ")  ...");

        try {
            jdbcTemplate.update("UPDATE " + table
                                + " SET "
                                + database.quote("description") + "=? , "
                                + database.quote("type") + "=? , "
                                + database.quote("checksum") + "=?"
                                + " WHERE " + database.quote("installed_rank") + "=?",
                        description, type, checksum, appliedMigration.getInstalledRank());
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to repair Schema History table " + table
                    + " for version " + version, e);
        }
    }
}