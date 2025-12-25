package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.license.Edition;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;
import static org.flywaydb.core.internal.configuration.ConfigUtils.removeInteger;

public class ClassicConfiguration implements Configuration {
    private static final Log LOG = LogFactory.getLog(ClassicConfiguration.class);

    private String driver;
    private String url;
    private String user;
    private String password;


    private DataSource dataSource;

    private int connectRetries;


    private String initSql;


    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();


    private Locations locations = new Locations("db/migration");


    private Charset encoding = StandardCharsets.UTF_8;


    private String defaultSchemaName = null;


    private String[] schemaNames = {};


    private String table = "flyway_schema_history";


    private String tablespace;


    private MigrationVersion target;

    private boolean placeholderReplacement = true;

    private Map<String, String> placeholders = new HashMap<>();

    private String placeholderPrefix = "${";

    private String placeholderSuffix = "}";

    private String sqlMigrationPrefix = "V";












        private String repeatableSqlMigrationPrefix = "R";

        private String sqlMigrationSeparator = "__";

        private String[] sqlMigrationSuffixes = {".sql"};

        private JavaMigration[] javaMigrations = {};

        private boolean ignoreMissingMigrations;

        private boolean ignoreIgnoredMigrations;

        private boolean ignorePendingMigrations;

        private boolean ignoreFutureMigrations = true;

        private boolean validateMigrationNaming = false;

        private boolean validateOnMigrate = true;

        private boolean cleanOnValidationError;

        private boolean cleanDisabled;

        private MigrationVersion baselineVersion = MigrationVersion.fromVersion("1");

        private String baselineDescription = "<< Flyway Baseline >>";

        private boolean baselineOnMigrate;

        private boolean outOfOrder;

        private final List<Callback> callbacks = new ArrayList<>();

        private boolean skipDefaultCallbacks;

        private MigrationResolver[] resolvers = new MigrationResolver[0];

        private boolean skipDefaultResolvers;

        private boolean mixed;

        private boolean group;

        private String installedBy;












































































        public ClassicConfiguration() {
    }

        public ClassicConfiguration(ClassLoader classLoader) {
        if (classLoader != null) {
            this.classLoader = classLoader;
        }
    }

        public ClassicConfiguration(Configuration configuration) {
        this(configuration.getClassLoader());
        configure(configuration);
    }

    @Override
    public Location[] getLocations() {
        return locations.getLocations().toArray(new Location[0]);
    }

    @Override
    public Charset getEncoding() {
        return encoding;
    }

    @Override
    public String getDefaultSchema() { return defaultSchemaName; }

    @Override
    public String[] getSchemas() { return schemaNames; }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public String getTablespace() {
        return tablespace;
    }

    @Override
    public MigrationVersion getTarget() {
        return target;
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return placeholderReplacement;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    @Override
    public String getPlaceholderSuffix() {
        return placeholderSuffix;
    }

    @Override
    public String getSqlMigrationPrefix() {
        return sqlMigrationPrefix;
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return repeatableSqlMigrationPrefix;
    }

    @Override
    public String getSqlMigrationSeparator() {
        return sqlMigrationSeparator;
    }

    @Override
    public String[] getSqlMigrationSuffixes() {
        return sqlMigrationSuffixes;
    }

    @Override
    public JavaMigration[] getJavaMigrations() {
        return javaMigrations;
    }

    @Override
    public boolean isIgnoreMissingMigrations() {
        return ignoreMissingMigrations;
    }

    @Override
    public boolean isIgnoreIgnoredMigrations() {
        return ignoreIgnoredMigrations;
    }

    @Override
    public boolean isIgnorePendingMigrations() {
        return ignorePendingMigrations;
    }

    @Override
    public boolean isIgnoreFutureMigrations() {
        return ignoreFutureMigrations;
    }

    @Override
    public boolean isValidateMigrationNaming() {
        return validateMigrationNaming;
    }

    @Override
    public boolean isValidateOnMigrate() {
        return validateOnMigrate;
    }

    @Override
    public boolean isCleanOnValidationError() {
        return cleanOnValidationError;
    }

    @Override
    public boolean isCleanDisabled() {
        return cleanDisabled;
    }

    @Override
    public MigrationVersion getBaselineVersion() {
        return baselineVersion;
    }

    @Override
    public String getBaselineDescription() {
        return baselineDescription;
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return baselineOnMigrate;
    }

    @Override
    public boolean isOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return resolvers;
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return skipDefaultResolvers;
    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null &&
                (StringUtils.hasLength(driver) || StringUtils.hasLength(user) || StringUtils.hasLength(password))) {
            LOG.warn("Discarding INCOMPLETE dataSource configuration! " + ConfigUtils.URL + " must be set.");
        }
        return dataSource;
    }

    @Override
    public int getConnectRetries() {
        return connectRetries;
    }

    @Override
    public String getInitSql() {
        return initSql;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean isMixed() {
        return mixed;
    }

    @Override
    public String getInstalledBy() {
        return installedBy;
    }

    @Override
    public boolean isGroup() {
        return group;
    }

    @Override
    public String[] getErrorOverrides() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("errorOverrides");




    }

    @Override
    public OutputStream getDryRunOutput() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("dryRunOutput");




    }

    @Override
    public String getLicenseKey() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("licenseKey");




    }


    @Override
    public boolean outputQueryResults() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("outputQueryResults");




    }


    public void setDryRunOutput(OutputStream dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("dryRunOutput");




    }

    public void setDryRunOutputAsFile(File dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("dryRunOutput");











































    }


    public void setDryRunOutputAsFileName(String dryRunOutputFileName) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("dryRunOutput");




    }

    
    public void setErrorOverrides(String... errorOverrides) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("errorOverrides");




    }


    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setInstalledBy(String installedBy) {
        if ("".equals(installedBy)) {
            installedBy = null;
        }
        this.installedBy = installedBy;
    }


    public void setMixed(boolean mixed) {
        this.mixed = mixed;
    }


    public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
        this.ignoreMissingMigrations = ignoreMissingMigrations;
    }


    public void setIgnoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
        this.ignoreIgnoredMigrations = ignoreIgnoredMigrations;
    }

        public void setIgnorePendingMigrations(boolean ignorePendingMigrations) {
        this.ignorePendingMigrations = ignorePendingMigrations;
    }

        public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
        this.ignoreFutureMigrations = ignoreFutureMigrations;
    }

        public void setValidateMigrationNaming(boolean validateMigrationNaming) {
        this.validateMigrationNaming = validateMigrationNaming;
    }

        public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

        public void setCleanOnValidationError(boolean cleanOnValidationError) {
        this.cleanOnValidationError = cleanOnValidationError;
    }

        public void setCleanDisabled(boolean cleanDisabled) {
        this.cleanDisabled = cleanDisabled;
    }

        public void setLocationsAsStrings(String... locations) {
        this.locations = new Locations(locations);
    }

        public void setLocations(Location... locations) {
        this.locations = new Locations(Arrays.asList(locations));
    }

        public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

        public void setEncodingAsString(String encoding) {
        this.encoding = Charset.forName(encoding);
    }

        public void setDefaultSchema(String schema) {
        this.defaultSchemaName = schema;
    }

        public void setSchemas(String... schemas) {
        this.schemaNames = schemas;
    }

        public void setTable(String table) {
        this.table = table;
    }

        public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

        public void setTarget(MigrationVersion target) {
        this.target = target;
    }

        public void setTargetAsString(String target) {
        this.target = MigrationVersion.fromVersion(target);
    }

        public void setPlaceholderReplacement(boolean placeholderReplacement) {
        this.placeholderReplacement = placeholderReplacement;
    }

        public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

        public void setPlaceholderPrefix(String placeholderPrefix) {
        if (!StringUtils.hasLength(placeholderPrefix)) {
            throw new FlywayException("placeholderPrefix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        this.placeholderPrefix = placeholderPrefix;
    }

        public void setPlaceholderSuffix(String placeholderSuffix) {
        if (!StringUtils.hasLength(placeholderSuffix)) {
            throw new FlywayException("placeholderSuffix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        this.placeholderSuffix = placeholderSuffix;
    }

        public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    @Override
    public String getUndoSqlMigrationPrefix() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("undoSqlMigrationPrefix");




    }

        public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("undoSqlMigrationPrefix");




    }

        public void setJavaMigrations(JavaMigration... javaMigrations) {
        if (javaMigrations == null) {
            throw new FlywayException("javaMigrations cannot be null", ErrorCode.CONFIGURATION);
        }
        this.javaMigrations = javaMigrations;
    }

    @Override
    public boolean isStream() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("stream");




    }

        public void setStream(boolean stream) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("stream");




    }

    @Override
    public boolean isBatch() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("batch");




    }

        public void setBatch(boolean batch) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("batch");




    }

        public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
    }

        public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        if (!StringUtils.hasLength(sqlMigrationSeparator)) {
            throw new FlywayException("sqlMigrationSeparator cannot be empty!", ErrorCode.CONFIGURATION);
        }

        this.sqlMigrationSeparator = sqlMigrationSeparator;
    }

        public void setSqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        this.sqlMigrationSuffixes = sqlMigrationSuffixes;
    }

        public void setDataSource(DataSource dataSource) {
        driver = null;
        url = null;
        user = null;
        password = null;
        this.dataSource = dataSource;
    }

        public void setDataSource(String url, String user, String password) {
        this.dataSource = new DriverDataSource(classLoader, null, url, user, password);
    }

        public void setConnectRetries(int connectRetries) {
        if (connectRetries < 0) {
            throw new FlywayException("Invalid number of connectRetries (must be 0 or greater): " + connectRetries, ErrorCode.CONFIGURATION);
        }
        this.connectRetries = connectRetries;
    }

        public void setInitSql(String initSql) {
        this.initSql = initSql;
    }

        public void setBaselineVersion(MigrationVersion baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

        public void setBaselineVersionAsString(String baselineVersion) {
        this.baselineVersion = MigrationVersion.fromVersion(baselineVersion);
    }

        public void setBaselineDescription(String baselineDescription) {
        this.baselineDescription = baselineDescription;
    }

        public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
    }

        public void setOutOfOrder(boolean outOfOrder) {
        this.outOfOrder = outOfOrder;
    }

        @Override
    public Callback[] getCallbacks() {
        return callbacks.toArray(new Callback[0]);
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return skipDefaultCallbacks;
    }

        public void setCallbacks(Callback... callbacks) {
        this.callbacks.clear();
        this.callbacks.addAll(Arrays.asList(callbacks));
    }

        public void setCallbacksAsClassNames(String... callbacks) {
        this.callbacks.clear();
        for (String callback : callbacks) {
            Object o = ClassUtils.instantiate(callback, classLoader);
            if (o instanceof Callback) {
                this.callbacks.add((Callback) o);
            } else {
                throw new FlywayException("Invalid callback: " + callback + " (must implement org.flywaydb.core.api.callback.Callback)", ErrorCode.CONFIGURATION);
            }
        }
    }

        public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
        this.skipDefaultCallbacks = skipDefaultCallbacks;
    }

        public void setResolvers(MigrationResolver... resolvers) {
        this.resolvers = resolvers;
    }

        public void setResolversAsClassNames(String... resolvers) {
        List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolvers, classLoader);
        setResolvers(resolverList.toArray(new MigrationResolver[resolvers.length]));
    }

        public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
        this.skipDefaultResolvers = skipDefaultResolvers;
    }


















    @Override
    public boolean isOracleSqlplus() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("oracle.sqlplus");




    }

        public void setOracleSqlplus(boolean oracleSqlplus) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("oracle.sqlplus");




    }

    @Override
    public boolean isOracleSqlplusWarn() {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("oracle.sqlplusWarn");




    }

        public void setOracleSqlplusWarn(boolean oracleSqlplusWarn) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("oracle.sqlplusWarn");




    }

        public void setLicenseKey(String licenseKey) {

          LOG.warn(Edition.PRO + " or " + Edition.ENTERPRISE + " upgrade required: " + licenseKey
            + " is not supported by " + Edition.COMMUNITY + ".");




    }

        public void configure(Configuration configuration) {
        setBaselineDescription(configuration.getBaselineDescription());
        setBaselineOnMigrate(configuration.isBaselineOnMigrate());
        setBaselineVersion(configuration.getBaselineVersion());
        setCallbacks(configuration.getCallbacks());
        setCleanDisabled(configuration.isCleanDisabled());
        setCleanOnValidationError(configuration.isCleanOnValidationError());
        setDataSource(configuration.getDataSource());
        setConnectRetries(configuration.getConnectRetries());
        setInitSql(configuration.getInitSql());











        setEncoding(configuration.getEncoding());
        setGroup(configuration.isGroup());
        setValidateMigrationNaming(configuration.isValidateMigrationNaming());
        setIgnoreFutureMigrations(configuration.isIgnoreFutureMigrations());
        setIgnoreMissingMigrations(configuration.isIgnoreMissingMigrations());
        setIgnoreIgnoredMigrations(configuration.isIgnoreIgnoredMigrations());
        setIgnorePendingMigrations(configuration.isIgnorePendingMigrations());
        setInstalledBy(configuration.getInstalledBy());
        setJavaMigrations(configuration.getJavaMigrations());
        setLocations(configuration.getLocations());
        setMixed(configuration.isMixed());
        setOutOfOrder(configuration.isOutOfOrder());
        setPlaceholderPrefix(configuration.getPlaceholderPrefix());
        setPlaceholderReplacement(configuration.isPlaceholderReplacement());
        setPlaceholders(configuration.getPlaceholders());
        setPlaceholderSuffix(configuration.getPlaceholderSuffix());
        setRepeatableSqlMigrationPrefix(configuration.getRepeatableSqlMigrationPrefix());
        setResolvers(configuration.getResolvers());
        setDefaultSchema(configuration.getDefaultSchema());
        setSchemas(configuration.getSchemas());
        setSkipDefaultCallbacks(configuration.isSkipDefaultCallbacks());
        setSkipDefaultResolvers(configuration.isSkipDefaultResolvers());
        setSqlMigrationPrefix(configuration.getSqlMigrationPrefix());
        setSqlMigrationSeparator(configuration.getSqlMigrationSeparator());
        setSqlMigrationSuffixes(configuration.getSqlMigrationSuffixes());
        setTable(configuration.getTable());
        setTablespace(configuration.getTablespace());
        setTarget(configuration.getTarget());
        setValidateOnMigrate(configuration.isValidateOnMigrate());
    }

        private void setOutputQueryResults(boolean outputQueryResults) {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("outputQueryResults");




    }

        public void configure(Properties properties) {
        configure(ConfigUtils.propertiesToMap(properties));
    }

        public void configure(Map<String, String> props) {
        props = new HashMap<>(props);

        String driverProp = props.remove(ConfigUtils.DRIVER);
        if (driverProp != null) {
            dataSource = null;
            driver = driverProp;
        }
        String urlProp = props.remove(ConfigUtils.URL);
        if (urlProp != null) {
            dataSource = null;
            url = urlProp;
        }
        String userProp = props.remove(ConfigUtils.USER);
        if (userProp != null) {
            dataSource = null;
            user = userProp;
        }
        String passwordProp = props.remove(ConfigUtils.PASSWORD);
        if (passwordProp != null) {
            dataSource = null;
            password = passwordProp;
        }
        if (StringUtils.hasText(url) && (StringUtils.hasText(urlProp) ||
                StringUtils.hasText(driverProp) || StringUtils.hasText(userProp) || StringUtils.hasText(passwordProp))) {
            setDataSource(new DriverDataSource(classLoader, driver, url, user, password));
        }
        Integer connectRetriesProp = removeInteger(props, ConfigUtils.CONNECT_RETRIES);
        if (connectRetriesProp != null) {
            setConnectRetries(connectRetriesProp);
        }
        String initSqlProp = props.remove(ConfigUtils.INIT_SQL);
        if (initSqlProp != null) {
            setInitSql(initSqlProp);
        }
        String locationsProp = props.remove(ConfigUtils.LOCATIONS);
        if (locationsProp != null) {
            setLocationsAsStrings(StringUtils.tokenizeToStringArray(locationsProp, ","));
        }
        Boolean placeholderReplacementProp = removeBoolean(props, ConfigUtils.PLACEHOLDER_REPLACEMENT);
        if (placeholderReplacementProp != null) {
            setPlaceholderReplacement(placeholderReplacementProp);
        }
        String placeholderPrefixProp = props.remove(ConfigUtils.PLACEHOLDER_PREFIX);
        if (placeholderPrefixProp != null) {
            setPlaceholderPrefix(placeholderPrefixProp);
        }
        String placeholderSuffixProp = props.remove(ConfigUtils.PLACEHOLDER_SUFFIX);
        if (placeholderSuffixProp != null) {
            setPlaceholderSuffix(placeholderSuffixProp);
        }
        String sqlMigrationPrefixProp = props.remove(ConfigUtils.SQL_MIGRATION_PREFIX);
        if (sqlMigrationPrefixProp != null) {
            setSqlMigrationPrefix(sqlMigrationPrefixProp);
        }
        String undoSqlMigrationPrefixProp = props.remove(ConfigUtils.UNDO_SQL_MIGRATION_PREFIX);
        if (undoSqlMigrationPrefixProp != null) {
            setUndoSqlMigrationPrefix(undoSqlMigrationPrefixProp);
        }
        String repeatableSqlMigrationPrefixProp = props.remove(ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX);
        if (repeatableSqlMigrationPrefixProp != null) {
            setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
        }
        String sqlMigrationSeparatorProp = props.remove(ConfigUtils.SQL_MIGRATION_SEPARATOR);
        if (sqlMigrationSeparatorProp != null) {
            setSqlMigrationSeparator(sqlMigrationSeparatorProp);
        }
        String sqlMigrationSuffixesProp = props.remove(ConfigUtils.SQL_MIGRATION_SUFFIXES);
        if (sqlMigrationSuffixesProp != null) {
            setSqlMigrationSuffixes(StringUtils.tokenizeToStringArray(sqlMigrationSuffixesProp, ","));
        }
        String encodingProp = props.remove(ConfigUtils.ENCODING);
        if (encodingProp != null) {
            setEncodingAsString(encodingProp);
        }
        String defaultSchemaProp = props.remove(ConfigUtils.DEFAULT_SCHEMA);
        if (defaultSchemaProp != null) {
            setDefaultSchema(defaultSchemaProp);
        }
        String schemasProp = props.remove(ConfigUtils.SCHEMAS);
        if (schemasProp != null) {
            setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
        }
        String tableProp = props.remove(ConfigUtils.TABLE);
        if (tableProp != null) {
            setTable(tableProp);
        }
        String tablespaceProp = props.remove(ConfigUtils.TABLESPACE);
        if (tablespaceProp != null) {
            setTablespace(tablespaceProp);
        }
        Boolean cleanOnValidationErrorProp = removeBoolean(props, ConfigUtils.CLEAN_ON_VALIDATION_ERROR);
        if (cleanOnValidationErrorProp != null) {
            setCleanOnValidationError(cleanOnValidationErrorProp);
        }
        Boolean cleanDisabledProp = removeBoolean(props, ConfigUtils.CLEAN_DISABLED);
        if (cleanDisabledProp != null) {
            setCleanDisabled(cleanDisabledProp);
        }
        Boolean validateOnMigrateProp = removeBoolean(props, ConfigUtils.VALIDATE_ON_MIGRATE);
        if (validateOnMigrateProp != null) {
            setValidateOnMigrate(validateOnMigrateProp);
        }
        String baselineVersionProp = props.remove(ConfigUtils.BASELINE_VERSION);
        if (baselineVersionProp != null) {
            setBaselineVersion(MigrationVersion.fromVersion(baselineVersionProp));
        }
        String baselineDescriptionProp = props.remove(ConfigUtils.BASELINE_DESCRIPTION);
        if (baselineDescriptionProp != null) {
            setBaselineDescription(baselineDescriptionProp);
        }
        Boolean baselineOnMigrateProp = removeBoolean(props, ConfigUtils.BASELINE_ON_MIGRATE);
        if (baselineOnMigrateProp != null) {
            setBaselineOnMigrate(baselineOnMigrateProp);
        }
        Boolean ignoreMissingMigrationsProp = removeBoolean(props, ConfigUtils.IGNORE_MISSING_MIGRATIONS);
        if (ignoreMissingMigrationsProp != null) {
            setIgnoreMissingMigrations(ignoreMissingMigrationsProp);
        }
        Boolean ignoreIgnoredMigrationsProp = removeBoolean(props, ConfigUtils.IGNORE_IGNORED_MIGRATIONS);
        if (ignoreIgnoredMigrationsProp != null) {
            setIgnoreIgnoredMigrations(ignoreIgnoredMigrationsProp);
        }
        Boolean ignorePendingMigrationsProp = removeBoolean(props, ConfigUtils.IGNORE_PENDING_MIGRATIONS);
        if (ignorePendingMigrationsProp != null) {
            setIgnorePendingMigrations(ignorePendingMigrationsProp);
        }
        Boolean ignoreFutureMigrationsProp = removeBoolean(props, ConfigUtils.IGNORE_FUTURE_MIGRATIONS);
        if (ignoreFutureMigrationsProp != null) {
            setIgnoreFutureMigrations(ignoreFutureMigrationsProp);
        }
        Boolean validateMigrationNamingProp = removeBoolean(props, ConfigUtils.VALIDATE_MIGRATION_NAMING);
        if (validateMigrationNamingProp != null) {
            setValidateMigrationNaming(validateMigrationNamingProp);
        }
        String targetProp = props.remove(ConfigUtils.TARGET);
        if (targetProp != null) {
            setTarget(MigrationVersion.fromVersion(targetProp));
        }
        Boolean outOfOrderProp = removeBoolean(props, ConfigUtils.OUT_OF_ORDER);
        if (outOfOrderProp != null) {
            setOutOfOrder(outOfOrderProp);
        }
        Boolean outputQueryResultsProp = removeBoolean(props, ConfigUtils.OUTPUT_QUERY_RESULTS);
        if (outputQueryResultsProp != null) {
            setOutputQueryResults(outputQueryResultsProp);
        }
        String resolversProp = props.remove(ConfigUtils.RESOLVERS);
        if (StringUtils.hasLength(resolversProp)) {
            setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
        }
        Boolean skipDefaultResolversProp = removeBoolean(props, ConfigUtils.SKIP_DEFAULT_RESOLVERS);
        if (skipDefaultResolversProp != null) {
            setSkipDefaultResolvers(skipDefaultResolversProp);
        }
        String callbacksProp = props.remove(ConfigUtils.CALLBACKS);
        if (StringUtils.hasLength(callbacksProp)) {
            setCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
        }
        Boolean skipDefaultCallbacksProp = removeBoolean(props, ConfigUtils.SKIP_DEFAULT_CALLBACKS);
        if (skipDefaultCallbacksProp != null) {
            setSkipDefaultCallbacks(skipDefaultCallbacksProp);
        }

        Map<String, String> placeholdersFromProps = new HashMap<>(getPlaceholders());
        Iterator<Map.Entry<String, String>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String propertyName = entry.getKey();

            if (propertyName.startsWith(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = entry.getValue();
                placeholdersFromProps.put(placeholderName, placeholderValue);
                iterator.remove();
            }
        }
        setPlaceholders(placeholdersFromProps);

        Boolean mixedProp = removeBoolean(props, ConfigUtils.MIXED);
        if (mixedProp != null) {
            setMixed(mixedProp);
        }

        Boolean groupProp = removeBoolean(props, ConfigUtils.GROUP);
        if (groupProp != null) {
            setGroup(groupProp);
        }

        String installedByProp = props.remove(ConfigUtils.INSTALLED_BY);
        if (installedByProp != null) {
            setInstalledBy(installedByProp);
        }

        String dryRunOutputProp = props.remove(ConfigUtils.DRYRUN_OUTPUT);
        if (dryRunOutputProp != null) {
            setDryRunOutputAsFileName(dryRunOutputProp);
        }

        String errorOverridesProp = props.remove(ConfigUtils.ERROR_OVERRIDES);
        if (errorOverridesProp != null) {
            setErrorOverrides(StringUtils.tokenizeToStringArray(errorOverridesProp, ","));
        }

        Boolean streamProp = removeBoolean(props, ConfigUtils.STREAM);
        if (streamProp != null) {
            setStream(streamProp);
        }

        Boolean batchProp = removeBoolean(props, ConfigUtils.BATCH);
        if (batchProp != null) {
            setBatch(batchProp);
        }

        Boolean oracleSqlplusProp = removeBoolean(props, ConfigUtils.ORACLE_SQLPLUS);
        if (oracleSqlplusProp != null) {
            setOracleSqlplus(oracleSqlplusProp);
        }

        Boolean oracleSqlplusWarnProp = removeBoolean(props, ConfigUtils.ORACLE_SQLPLUS_WARN);
        if (oracleSqlplusWarnProp != null) {
            setOracleSqlplusWarn(oracleSqlplusWarnProp);
        }

        String licenseKeyProp = props.remove(ConfigUtils.LICENSE_KEY);
        if (licenseKeyProp != null) {
            setLicenseKey(licenseKeyProp);
        }

        ConfigUtils.checkConfigurationForUnrecognisedProperties(props, "flyway.");
    }

        public void configureUsingEnvVars() {
        configure(ConfigUtils.environmentVariablesToPropertyMap());
    }
}