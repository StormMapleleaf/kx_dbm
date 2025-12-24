package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

public abstract class SchemaHistory {
    public static final String NO_DESCRIPTION_MARKER = "<< no description >>";

        protected Table table;

        public abstract <T> T lock(Callable<T> callable);

        public abstract boolean exists();

        public abstract void create(boolean baseline);

        public final boolean hasNonSyntheticAppliedMigrations() {
        for (AppliedMigration appliedMigration : allAppliedMigrations()) {
            if (!appliedMigration.getType().isSynthetic()



            ) {
                return true;
            }
        }
        return false;
    }

        public abstract List<AppliedMigration> allAppliedMigrations();

        public final AppliedMigration getBaselineMarker() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        for (int i = 0; i < Math.min(appliedMigrations.size(), 2); i++) {
            AppliedMigration appliedMigration = appliedMigrations.get(i);
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                return appliedMigration;
            }
        }
        return null;
    }

        public abstract void removeFailedMigrations();

        public final void addSchemasMarker(Schema[] schemas) {
        addAppliedMigration(null, "<< Flyway Schema Creation >>",
                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true);
    }

        public final boolean hasSchemasMarker() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        return !appliedMigrations.isEmpty() && appliedMigrations.get(0).getType() == MigrationType.SCHEMA;
    }


        public abstract void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration);

        public void clearCache() {
    }

        public final void addAppliedMigration(MigrationVersion version, String description, MigrationType type,
                                          String script, Integer checksum, int executionTime, boolean success) {
        int installedRank = type == MigrationType.SCHEMA ? 0 : calculateInstalledRank();
        doAddAppliedMigration(
                installedRank,
                version,
                AbbreviationUtils.abbreviateDescription(description),
                type,
                AbbreviationUtils.abbreviateScript(script),
                checksum,
                executionTime,
                success);
    }

        private int calculateInstalledRank() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        if (appliedMigrations.isEmpty()) {
            return 1;
        }
        return appliedMigrations.get(appliedMigrations.size() - 1).getInstalledRank() + 1;
    }

    protected abstract void doAddAppliedMigration(int installedRank, MigrationVersion version, String description,
                                                  MigrationType type, String script, Integer checksum,
                                                  int executionTime, boolean success);

    @Override
    public String toString() {
        return table.toString();
    }
}