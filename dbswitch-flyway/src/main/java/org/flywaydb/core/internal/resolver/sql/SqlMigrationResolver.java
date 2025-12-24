package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.io.Reader;
import java.util.*;

public class SqlMigrationResolver implements MigrationResolver {
        private final SqlScriptExecutorFactory sqlScriptExecutorFactory;

        private final ResourceProvider resourceProvider;

    private final SqlScriptFactory sqlScriptFactory;

        private final Configuration configuration;

    private final ParsingContext parsingContext;

        public SqlMigrationResolver(ResourceProvider resourceProvider,
                                SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory,
                                Configuration configuration, ParsingContext parsingContext) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.resourceProvider = resourceProvider;
        this.sqlScriptFactory = sqlScriptFactory;
        this.configuration = configuration;
        this.parsingContext = parsingContext;
    }

    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        String separator = configuration.getSqlMigrationSeparator();
        String[] suffixes = configuration.getSqlMigrationSuffixes();
        addMigrations(migrations, configuration.getSqlMigrationPrefix(), separator, suffixes,
                false



        );




        addMigrations(migrations, configuration.getRepeatableSqlMigrationPrefix(), separator, suffixes,
                true



        );

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources) {
        List<LoadableResource> list = new ArrayList<>();

        for (final LoadableResource loadableResource : loadableResources) {
            LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
                @Override
                public Reader read() {
                    return PlaceholderReplacingReader.create(
                            configuration,
                            parsingContext,
                            loadableResource.read());
                }

                @Override
                public String getAbsolutePath() { return loadableResource.getAbsolutePath(); }
                @Override
                public String getAbsolutePathOnDisk() { return loadableResource.getAbsolutePathOnDisk(); }
                @Override
                public String getFilename() { return loadableResource.getFilename(); }
                @Override
                public String getRelativePath() { return loadableResource.getRelativePath(); }
            };

            list.add(placeholderReplacingLoadableResource);
        }

        return list.toArray(new LoadableResource[0]);
    }

    private Integer getChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable && configuration.isPlaceholderReplacement()) {
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources));
        }

        return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
    }

    private Integer getEquivalentChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable) {
            return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
        }

        return null;
    }

    private void addMigrations(List<ResolvedMigration> migrations, String prefix,
                               String separator, String[] suffixes, boolean repeatable



    ){
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resourceProvider.getResources(prefix, suffixes)) {
            String filename = resource.getFilename();
            ResourceName result = resourceNameParser.parse(filename);
            if (!result.isValid() || isSqlCallback(result) || !prefix.equals(result.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, configuration.isMixed(), resourceProvider);

            List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);








            Integer checksum = getChecksumForLoadableResource(repeatable, resources);
            Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

            migrations.add(new ResolvedMigrationImpl(
                    result.getVersion(),
                    result.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    equivalentChecksum,



                            MigrationType.SQL,
                    resource.getAbsolutePathOnDisk(),
                    new SqlMigrationExecutor(sqlScriptExecutorFactory, sqlScript



                    )) {
                @Override
                public void validate() {
                }
            });
        }
    }



            static boolean isSqlCallback(ResourceName result) {
        if (Event.fromId(result.getPrefix()) != null) {
            return true;
        }
        return false;
    }
}