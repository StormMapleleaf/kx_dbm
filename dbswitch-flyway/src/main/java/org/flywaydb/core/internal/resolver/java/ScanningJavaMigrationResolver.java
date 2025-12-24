package org.flywaydb.core.internal.resolver.java;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanningJavaMigrationResolver implements MigrationResolver {
        private final ClassProvider<JavaMigration> classProvider;

        private final Configuration configuration;

        public ScanningJavaMigrationResolver(ClassProvider<JavaMigration> classProvider, Configuration configuration) {
        this.classProvider = classProvider;
        this.configuration = configuration;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (Class<?> clazz : classProvider.getClasses()) {
            JavaMigration javaMigration = ClassUtils.instantiate(clazz.getName(), configuration.getClassLoader());
            migrations.add(new ResolvedJavaMigration(javaMigration));
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }
}