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

          T execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
        Database database, Schema[] schemas, CallbackExecutor callbackExecutor);
  }
}