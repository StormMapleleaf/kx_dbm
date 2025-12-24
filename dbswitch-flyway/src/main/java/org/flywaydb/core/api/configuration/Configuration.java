package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

public interface Configuration {

    ClassLoader getClassLoader();

    DataSource getDataSource();

    int getConnectRetries();

    String getInitSql();

    MigrationVersion getBaselineVersion();

    String getBaselineDescription();

    MigrationResolver[] getResolvers();

    boolean isSkipDefaultResolvers();

    Callback[] getCallbacks();

    boolean isSkipDefaultCallbacks();

    String getSqlMigrationPrefix();

    String getUndoSqlMigrationPrefix();


    String getRepeatableSqlMigrationPrefix();

 
    String getSqlMigrationSeparator();


    String[] getSqlMigrationSuffixes();


    JavaMigration[] getJavaMigrations();

        boolean isPlaceholderReplacement();

        String getPlaceholderSuffix();

        String getPlaceholderPrefix();

        Map<String, String> getPlaceholders();

        MigrationVersion getTarget();

        String getTable();

        String getTablespace();

        String getDefaultSchema();

        String[] getSchemas();

        Charset getEncoding();

        Location[] getLocations();

        boolean isBaselineOnMigrate();

        boolean isOutOfOrder();

        boolean isIgnoreMissingMigrations();

        boolean isIgnoreIgnoredMigrations();

        boolean isIgnorePendingMigrations();

        boolean isIgnoreFutureMigrations();

        boolean isValidateMigrationNaming();

        boolean isValidateOnMigrate();

        boolean isCleanOnValidationError();

        boolean isCleanDisabled();

        boolean isMixed();

        boolean isGroup();

        String getInstalledBy();

        String[] getErrorOverrides();

        OutputStream getDryRunOutput();

        boolean isStream();

        boolean isBatch();

        boolean isOracleSqlplus();

        boolean isOracleSqlplusWarn();

        String getLicenseKey();

        boolean outputQueryResults();
}