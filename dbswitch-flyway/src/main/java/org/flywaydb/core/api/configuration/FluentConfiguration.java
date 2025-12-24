package org.flywaydb.core.api.configuration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FluentConfiguration implements Configuration {
    private final ClassicConfiguration config;

        public FluentConfiguration() {
        config = new ClassicConfiguration();
    }

        public FluentConfiguration(ClassLoader classLoader) {
        config = new ClassicConfiguration(classLoader);
    }

        public Flyway load() {
        return new Flyway(this);
    }

        public FluentConfiguration configuration(Configuration configuration) {
        config.configure(configuration);
        return this;
    }

    @Override
    public Location[] getLocations() {
        return config.getLocations();
    }

    @Override
    public Charset getEncoding() {
        return config.getEncoding();
    }

    @Override
    public String getDefaultSchema() { return config.getDefaultSchema(); }

    @Override
    public String[] getSchemas() { return config.getSchemas(); }

    @Override
    public String getTable() {
        return config.getTable();
    }

    @Override
    public String getTablespace() {
        return config.getTablespace();
    }

    @Override
    public MigrationVersion getTarget() {
        return config.getTarget();
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return config.isPlaceholderReplacement();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return config.getPlaceholders();
    }

    @Override
    public String getPlaceholderPrefix() {
        return config.getPlaceholderPrefix();
    }

    @Override
    public String getPlaceholderSuffix() {
        return config.getPlaceholderSuffix();
    }

    @Override
    public String getSqlMigrationPrefix() {
        return config.getSqlMigrationPrefix();
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return config.getRepeatableSqlMigrationPrefix();
    }

    @Override
    public String getSqlMigrationSeparator() {
        return config.getSqlMigrationSeparator();
    }

    @Override
    public String[] getSqlMigrationSuffixes() {
        return config.getSqlMigrationSuffixes();
    }

    @Override
    public JavaMigration[] getJavaMigrations() {
        return config.getJavaMigrations();
    }

    @Override
    public boolean isIgnoreMissingMigrations() {
        return config.isIgnoreMissingMigrations();
    }

    @Override
    public boolean isIgnoreIgnoredMigrations() {
        return config.isIgnoreIgnoredMigrations();
    }

    @Override
    public boolean isIgnorePendingMigrations() {
        return config.isIgnorePendingMigrations();
    }

    @Override
    public boolean isIgnoreFutureMigrations() {
        return config.isIgnoreFutureMigrations();
    }

    @Override
    public boolean isValidateMigrationNaming() { return config.isValidateMigrationNaming(); }

    @Override
    public boolean isValidateOnMigrate() {
        return config.isValidateOnMigrate();
    }

    @Override
    public boolean isCleanOnValidationError() {
        return config.isCleanOnValidationError();
    }

    @Override
    public boolean isCleanDisabled() {
        return config.isCleanDisabled();
    }

    @Override
    public MigrationVersion getBaselineVersion() {
        return config.getBaselineVersion();
    }

    @Override
    public String getBaselineDescription() {
        return config.getBaselineDescription();
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return config.isBaselineOnMigrate();
    }

    @Override
    public boolean isOutOfOrder() {
        return config.isOutOfOrder();
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return config.getResolvers();
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return config.isSkipDefaultResolvers();
    }

    @Override
    public DataSource getDataSource() {
        return config.getDataSource();
    }

    @Override
    public int getConnectRetries() {
        return config.getConnectRetries();
    }

    @Override
    public String getInitSql() {
        return config.getInitSql();
    }

    @Override
    public ClassLoader getClassLoader() {
        return config.getClassLoader();
    }

    @Override
    public boolean isMixed() {
        return config.isMixed();
    }

    @Override
    public String getInstalledBy() {
        return config.getInstalledBy();
    }

    @Override
    public boolean isGroup() {
        return config.isGroup();
    }

    @Override
    public String[] getErrorOverrides() {
        return config.getErrorOverrides();
    }

    @Override
    public OutputStream getDryRunOutput() {
        return config.getDryRunOutput();
    }

    @Override
    public boolean isStream() {
        return config.isStream();
    }

    @Override
    public boolean isBatch() {
        return config.isBatch();
    }

    @Override
    public boolean isOracleSqlplus() {
        return config.isOracleSqlplus();
    }

    @Override
    public boolean isOracleSqlplusWarn() {
        return config.isOracleSqlplusWarn();
    }

    @Override
    public String getLicenseKey() {
        return config.getLicenseKey();
    }

    @Override
    public boolean outputQueryResults() {
        return config.outputQueryResults();
    }

        public FluentConfiguration dryRunOutput(OutputStream dryRunOutput) {
        config.setDryRunOutput(dryRunOutput);
        return this;
    }

        public FluentConfiguration dryRunOutput(File dryRunOutput) {
        config.setDryRunOutputAsFile(dryRunOutput);
        return this;
    }

        public FluentConfiguration dryRunOutput(String dryRunOutputFileName) {
        config.setDryRunOutputAsFileName(dryRunOutputFileName);
        return this;
    }

        public FluentConfiguration errorOverrides(String... errorOverrides) {
        config.setErrorOverrides(errorOverrides);
        return this;
    }

        public FluentConfiguration group(boolean group) {
        config.setGroup(group);
        return this;
    }

        public FluentConfiguration installedBy(String installedBy) {
        config.setInstalledBy(installedBy);
        return this;
    }

        public FluentConfiguration mixed(boolean mixed) {
        config.setMixed(mixed);
        return this;
    }

        public FluentConfiguration ignoreMissingMigrations(boolean ignoreMissingMigrations) {
        config.setIgnoreMissingMigrations(ignoreMissingMigrations);
        return this;
    }

        public FluentConfiguration ignoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
        config.setIgnoreIgnoredMigrations(ignoreIgnoredMigrations);
        return this;
    }

        public FluentConfiguration ignorePendingMigrations(boolean ignorePendingMigrations) {
        config.setIgnorePendingMigrations(ignorePendingMigrations);
        return this;
    }

        public FluentConfiguration ignoreFutureMigrations(boolean ignoreFutureMigrations) {
        config.setIgnoreFutureMigrations(ignoreFutureMigrations);
        return this;
    }

        public FluentConfiguration validateMigrationNaming(boolean validateMigrationNaming){
        config.setValidateMigrationNaming(validateMigrationNaming);
        return this;
    }

        public FluentConfiguration validateOnMigrate(boolean validateOnMigrate) {
        config.setValidateOnMigrate(validateOnMigrate);
        return this;
    }

        public FluentConfiguration cleanOnValidationError(boolean cleanOnValidationError) {
        config.setCleanOnValidationError(cleanOnValidationError);
        return this;
    }

        public FluentConfiguration cleanDisabled(boolean cleanDisabled) {
        config.setCleanDisabled(cleanDisabled);
        return this;
    }

        public FluentConfiguration locations(String... locations) {
        config.setLocationsAsStrings(locations);
        return this;
    }

        public FluentConfiguration locations(Location... locations) {
        config.setLocations(locations);
        return this;
    }

        public FluentConfiguration encoding(String encoding) {
        config.setEncodingAsString(encoding);
        return this;
    }

        public FluentConfiguration encoding(Charset encoding) {
        config.setEncoding(encoding);
        return this;
    }

        public FluentConfiguration defaultSchema(String schema) {
        config.setDefaultSchema(schema);
        return this;
    }

        public FluentConfiguration schemas(String... schemas) {
        config.setSchemas(schemas);
        return this;
    }

        public FluentConfiguration table(String table) {
        config.setTable(table);
        return this;
    }

        public FluentConfiguration tablespace(String tablespace) {
        config.setTablespace(tablespace);
        return this;
    }

        public FluentConfiguration target(MigrationVersion target) {
        config.setTarget(target);
        return this;
    }

        public FluentConfiguration target(String target) {
        config.setTargetAsString(target);
        return this;
    }

        public FluentConfiguration placeholderReplacement(boolean placeholderReplacement) {
        config.setPlaceholderReplacement(placeholderReplacement);
        return this;
    }

        public FluentConfiguration placeholders(Map<String, String> placeholders) {
        config.setPlaceholders(placeholders);
        return this;
    }

        public FluentConfiguration placeholderPrefix(String placeholderPrefix) {
        config.setPlaceholderPrefix(placeholderPrefix);
        return this;
    }

        public FluentConfiguration placeholderSuffix(String placeholderSuffix) {
        config.setPlaceholderSuffix(placeholderSuffix);
        return this;
    }

        public FluentConfiguration sqlMigrationPrefix(String sqlMigrationPrefix) {
        config.setSqlMigrationPrefix(sqlMigrationPrefix);
        return this;
    }

    @Override
    public String getUndoSqlMigrationPrefix() {
        return config.getUndoSqlMigrationPrefix();
    }

        public FluentConfiguration undoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
        config.setUndoSqlMigrationPrefix(undoSqlMigrationPrefix);
        return this;
    }

        public FluentConfiguration repeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        config.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
        return this;
    }

        public FluentConfiguration sqlMigrationSeparator(String sqlMigrationSeparator) {
        config.setSqlMigrationSeparator(sqlMigrationSeparator);
        return this;
    }

        public FluentConfiguration sqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        config.setSqlMigrationSuffixes(sqlMigrationSuffixes);
        return this;
    }

        public FluentConfiguration javaMigrations(JavaMigration... javaMigrations) {
        config.setJavaMigrations(javaMigrations);
        return this;
    }

        public FluentConfiguration dataSource(DataSource dataSource) {
        config.setDataSource(dataSource);
        return this;
    }

        public FluentConfiguration dataSource(String url, String user, String password) {
        config.setDataSource(url, user, password);
        return this;
    }

        public FluentConfiguration connectRetries(int connectRetries) {
        config.setConnectRetries(connectRetries);
        return this;
    }

        public FluentConfiguration initSql(String initSql) {
        config.setInitSql(initSql);
        return this;
    }

        public FluentConfiguration baselineVersion(MigrationVersion baselineVersion) {
        config.setBaselineVersion(baselineVersion);
        return this;
    }

        public FluentConfiguration baselineVersion(String baselineVersion) {
        config.setBaselineVersion(MigrationVersion.fromVersion(baselineVersion));
        return this;
    }

        public FluentConfiguration baselineDescription(String baselineDescription) {
        config.setBaselineDescription(baselineDescription);
        return this;
    }

        public FluentConfiguration baselineOnMigrate(boolean baselineOnMigrate) {
        config.setBaselineOnMigrate(baselineOnMigrate);
        return this;
    }

        public FluentConfiguration outOfOrder(boolean outOfOrder) {
        config.setOutOfOrder(outOfOrder);
        return this;
    }

        @Override
    public Callback[] getCallbacks() {
        return config.getCallbacks();
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return config.isSkipDefaultCallbacks();
    }

        public FluentConfiguration callbacks(Callback... callbacks) {
        config.setCallbacks(callbacks);
        return this;
    }

        public FluentConfiguration callbacks(String... callbacks) {
        config.setCallbacksAsClassNames(callbacks);
        return this;
    }

        public FluentConfiguration skipDefaultCallbacks(boolean skipDefaultCallbacks) {
        config.setSkipDefaultCallbacks(skipDefaultCallbacks);
        return this;
    }

        public FluentConfiguration resolvers(MigrationResolver... resolvers) {
        config.setResolvers(resolvers);
        return this;
    }

        public FluentConfiguration resolvers(String... resolvers) {
        config.setResolversAsClassNames(resolvers);
        return this;
    }

        public FluentConfiguration skipDefaultResolvers(boolean skipDefaultResolvers) {
        config.setSkipDefaultResolvers(skipDefaultResolvers);
        return this;
    }

        public FluentConfiguration stream(boolean stream) {
        config.setStream(stream);
        return this;
    }

        public FluentConfiguration batch(boolean batch) {
        config.setBatch(batch);
        return this;
    }

        public FluentConfiguration oracleSqlplus(boolean oracleSqlplus) {
        config.setOracleSqlplus(oracleSqlplus);
        return this;
    }

        public FluentConfiguration oracleSqlplusWarn(boolean oracleSqlplusWarn) {
        config.setOracleSqlplusWarn(oracleSqlplusWarn);
        return this;
    }

        public FluentConfiguration licenseKey(String licenseKey) {
        config.setLicenseKey(licenseKey);
        return this;
    }

        public FluentConfiguration configuration(Properties properties) {
        config.configure(properties);
        return this;
    }

        public FluentConfiguration configuration(Map<String, String> props) {
        config.configure(props);
        return this;
    }

        public FluentConfiguration loadDefaultConfigurationFiles() {
        return loadDefaultConfigurationFiles("UTF-8");
    }

        public FluentConfiguration loadDefaultConfigurationFiles(String encoding) {
        String installationPath = ClassUtils.getLocationOnDisk(FluentConfiguration.class);
        File installationDir = new File(installationPath).getParentFile();

        Map<String, String> configMap = ConfigUtils.loadDefaultConfigurationFiles(installationDir, encoding);

        config.configure(configMap);
        return this;
    }

        public FluentConfiguration envVars() {
        config.configureUsingEnvVars();
        return this;
    }
}