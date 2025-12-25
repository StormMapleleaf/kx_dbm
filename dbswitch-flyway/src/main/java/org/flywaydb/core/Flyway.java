package org.flywaydb.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.callback.DefaultCallbackExecutor;
import org.flywaydb.core.internal.callback.SqlScriptCallbackFactory;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbInfo;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resource.NoopResourceProvider;
import org.flywaydb.core.internal.resource.ResourceNameValidator;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public class Flyway {

  private static final Log LOG = LogFactory.getLog(Flyway.class);

  private final ClassicConfiguration configuration;

    private boolean dbConnectionInfoPrinted;

    private ConfigurationValidator configurationValidator = new ConfigurationValidator();

    private ResourceNameValidator resourceNameValidator = new ResourceNameValidator();

    public static FluentConfiguration configure() {
    return new FluentConfiguration();
  }

    public static FluentConfiguration configure(ClassLoader classLoader) {
    return new FluentConfiguration(classLoader);
  }

    public Flyway(Configuration configuration) {
    this.configuration = new ClassicConfiguration(configuration);
  }

    public Configuration getConfiguration() {
    return new ClassicConfiguration(configuration);
  }

    private ResourceNameCache resourceNameCache = new ResourceNameCache();

    private final LocationScannerCache locationScannerCache = new LocationScannerCache();

    public int migrate() throws FlywayException {
    return execute(new Command<Integer>() {
      @Override
      public Integer execute(MigrationResolver migrationResolver,
          SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        if (configuration.isValidateOnMigrate()) {
          doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
              true
          );
        }

        if (!schemaHistory.exists()) {
          List<Schema> nonEmptySchemas = new ArrayList<>();
          for (Schema schema : schemas) {
            if (schema.exists() && !schema.empty()) {
              nonEmptySchemas.add(schema);
            }
          }

          if (!nonEmptySchemas.isEmpty()) {
            if (configuration.isBaselineOnMigrate()) {
              doBaseline(schemaHistory, callbackExecutor);
            } else {
              if (!schemaHistory.exists()) {
                throw new FlywayException("Found non-empty schema(s) "
                    + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                    + " but no schema history table. Use baseline()"
                    + " or set baselineOnMigrate to true to initialize the schema history table.");
              }
            }
          } else {
            new DbSchemas(database, schemas, schemaHistory).create(false);
            schemaHistory.create(false);
          }
        }

        return new DbMigrate(database, schemaHistory, schemas[0], migrationResolver, configuration,
            callbackExecutor).migrate();
      }
    }, true);
  }

  private void doBaseline(SchemaHistory schemaHistory, CallbackExecutor callbackExecutor) {
    new DbBaseline(schemaHistory, configuration.getBaselineVersion(), configuration.getBaselineDescription(),
        callbackExecutor).baseline();
  }

    public int undo() throws FlywayException {
    throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("undo");
  }

    public void validate() throws FlywayException {
    execute(new Command<Void>() {
      @Override
      public Void execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
          Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
            configuration.isIgnorePendingMigrations());
        return null;
      }
    }, true);
  }

    private void doValidate(Database database, MigrationResolver migrationResolver, SchemaHistory schemaHistory,
      Schema[] schemas, CallbackExecutor callbackExecutor, boolean ignorePending) {
    String validationError =
        new DbValidate(database, schemaHistory, schemas[0], migrationResolver,
            configuration, ignorePending, callbackExecutor).validate();

    if (validationError != null) {
      if (configuration.isCleanOnValidationError()) {
        doClean(database, schemaHistory, schemas, callbackExecutor);
      } else {
        throw new FlywayException("Validate failed: " + validationError);
      }
    }
  }

  private void doClean(Database database, SchemaHistory schemaHistory, Schema[] schemas,
      CallbackExecutor callbackExecutor) {
    new DbClean(database, schemaHistory, schemas, callbackExecutor, configuration.isCleanDisabled()).clean();
  }

    public void clean() {
    execute(new Command<Void>() {
      @Override
      public Void execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
          Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        doClean(database, schemaHistory, schemas, callbackExecutor);
        return null;
      }
    }, false);
  }

    public MigrationInfoService info() {
    return execute(new Command<MigrationInfoService>() {
      @Override
      public MigrationInfoService execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
          final Database database, final Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        return new DbInfo(migrationResolver, schemaHistory, configuration, callbackExecutor).info();
      }
    }, true);
  }

    public void baseline() throws FlywayException {
    execute(new Command<Void>() {
      @Override
      public Void execute(MigrationResolver migrationResolver,
          SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        new DbSchemas(database, schemas, schemaHistory).create(true);
        doBaseline(schemaHistory, callbackExecutor);
        return null;
      }
    }, false);
  }

    public void repair() throws FlywayException {
    execute(new Command<Void>() {
      @Override
      public Void execute(MigrationResolver migrationResolver,
          SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor
      ) {
        new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor, configuration).repair();
        return null;
      }
    }, true);
  }

    private MigrationResolver createMigrationResolver(ResourceProvider resourceProvider,
      ClassProvider<JavaMigration> classProvider,
      SqlScriptExecutorFactory sqlScriptExecutorFactory,
      SqlScriptFactory sqlScriptFactory,
      ParsingContext parsingContext) {
    return new CompositeMigrationResolver(resourceProvider, classProvider, configuration,
        sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, configuration.getResolvers());
  }

    <T> T execute(Command<T> command, boolean scannerRequired) {
    T result;
    VersionPrinter.printVersion();

    configurationValidator.validate(configuration);

    final ResourceProvider resourceProvider;
    ClassProvider<JavaMigration> classProvider;
    if (!scannerRequired && configuration.isSkipDefaultResolvers() && configuration.isSkipDefaultCallbacks()) {
      resourceProvider = NoopResourceProvider.INSTANCE;
      classProvider = NoopClassProvider.INSTANCE;
    } else {
      Scanner<JavaMigration> scanner = new Scanner<>(
          JavaMigration.class,
          Arrays.asList(configuration.getLocations()),
          configuration.getClassLoader(),
          configuration.getEncoding()

          , resourceNameCache
          , locationScannerCache
      );
      resourceProvider = scanner;
      classProvider = scanner;
    }

    if (configuration.isValidateMigrationNaming()) {
      resourceNameValidator.validateSQLMigrationNaming(resourceProvider, configuration);
    }

    JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(configuration.getDataSource(),
        configuration.getConnectRetries()

    );

    final ParsingContext parsingContext = new ParsingContext();
    final SqlScriptFactory sqlScriptFactory =
        DatabaseFactory.createSqlScriptFactory(jdbcConnectionFactory, configuration, parsingContext);

    final SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = DatabaseFactory
        .createSqlScriptExecutorFactory(jdbcConnectionFactory);

    jdbcConnectionFactory.setConnectionInitializer(new JdbcConnectionFactory.ConnectionInitializer() {
      @Override
      public void initialize(JdbcConnectionFactory jdbcConnectionFactory, Connection connection) {
        if (configuration.getInitSql() == null) {
          return;
        }
        StringResource resource = new StringResource(configuration.getInitSql());

        SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, true, resourceProvider);
        noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection

        ).execute(sqlScript);
      }
    });

    Database database = null;
    try {
      database = DatabaseFactory.createDatabase(configuration, !dbConnectionInfoPrinted, jdbcConnectionFactory);

      dbConnectionInfoPrinted = true;
      LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

      Pair<Schema, List<Schema>> schemas = prepareSchemas(database);
      Schema defaultSchema = schemas.getLeft();

      parsingContext.populate(database, configuration);

      database.ensureSupported();

      DefaultCallbackExecutor callbackExecutor = new DefaultCallbackExecutor(configuration, database, defaultSchema,
          prepareCallbacks(database, resourceProvider, jdbcConnectionFactory, sqlScriptFactory

          ));

      SqlScriptExecutorFactory sqlScriptExecutorFactory = DatabaseFactory
          .createSqlScriptExecutorFactory(jdbcConnectionFactory

          );

      result = command.execute(
          createMigrationResolver(resourceProvider, classProvider, sqlScriptExecutorFactory, sqlScriptFactory,
              parsingContext),
          SchemaHistoryFactory.getSchemaHistory(configuration, noCallbackSqlScriptExecutorFactory, sqlScriptFactory,
              database, defaultSchema

          ),
          database,
          schemas.getRight().toArray(new Schema[0]),
          callbackExecutor

      );
    } finally {
      IOUtils.close(database);

      showMemoryUsage();
    }
    return result;
  }

  private void showMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long free = runtime.freeMemory();
    long total = runtime.totalMemory();
    long used = total - free;

    long totalMB = total / (1024 * 1024);
    long usedMB = used / (1024 * 1024);
    LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
  }

  private Pair<Schema, List<Schema>> prepareSchemas(Database database) {
    String defaultSchemaName = configuration.getDefaultSchema();
    String[] schemaNames = configuration.getSchemas();

    if (!isDefaultSchemaValid(defaultSchemaName, schemaNames)) {
      throw new FlywayException("The defaultSchema property is specified but is not a member of the schemas property");
    }

    LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
    LOG.debug("Default schema: " + defaultSchemaName);

    List<Schema> schemas = new ArrayList<>();

    if (schemaNames.length == 0) {
      Schema currentSchema = database.getMainConnection().getCurrentSchema();
      if (currentSchema == null) {
        throw new FlywayException("Unable to determine schema for the schema history table." +
            " Set a default schema for the connection or specify one using the defaultSchema property!");
      }
      schemas.add(currentSchema);
    } else {
      for (String schemaName : schemaNames) {
        schemas.add(database.getMainConnection().getSchema(schemaName));
      }
    }

    if (defaultSchemaName == null && schemaNames.length > 0) {
      defaultSchemaName = schemaNames[0];
    }

    Schema defaultSchema = (defaultSchemaName != null)
        ? database.getMainConnection().getSchema(defaultSchemaName)
        : database.getMainConnection().getCurrentSchema();

    return Pair.of(defaultSchema, schemas);
  }

  private boolean isDefaultSchemaValid(String defaultSchema, String[] schemas) {
    if (defaultSchema == null) {
      return true;
    }
    for (String schema : schemas) {
      if (defaultSchema.equals(schema)) {
        return true;
      }
    }
    return false;
  }

  private List<Callback> prepareCallbacks(Database database, ResourceProvider resourceProvider,
      JdbcConnectionFactory jdbcConnectionFactory,
      SqlScriptFactory sqlScriptFactory

  ) {
    List<Callback> effectiveCallbacks = new ArrayList<>();

    effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));

    if (!configuration.isSkipDefaultCallbacks()) {
      SqlScriptExecutorFactory sqlScriptExecutorFactory =
          DatabaseFactory.createSqlScriptExecutorFactory(jdbcConnectionFactory

          );

      effectiveCallbacks.addAll(
          new SqlScriptCallbackFactory(
              resourceProvider,
              sqlScriptExecutorFactory,
              sqlScriptFactory,
              configuration
          ).getCallbacks());
    }

    return effectiveCallbacks;
  }

    interface Command<T> {

        T execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
        Database database, Schema[] schemas, CallbackExecutor callbackExecutor);
  }
}