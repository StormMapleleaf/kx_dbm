package org.flywaydb.core.internal.resolver.java;

import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedJavaMigrationResolver implements MigrationResolver {
        private final JavaMigration[] javaMigrations;

        public FixedJavaMigrationResolver(JavaMigration... javaMigrations) {
        this.javaMigrations = javaMigrations;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (JavaMigration javaMigration : javaMigrations) {
            migrations.add(new ResolvedJavaMigration(javaMigration));
        }

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }
}