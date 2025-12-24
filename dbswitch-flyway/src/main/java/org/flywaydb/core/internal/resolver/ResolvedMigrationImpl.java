package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;

import java.util.Objects;

public class ResolvedMigrationImpl implements ResolvedMigration {
        private final MigrationVersion version;

        private final String description;

        private final String script;

        private final Integer equivalentChecksum;

        private final Integer checksum;

        private final MigrationType type;

        private final String physicalLocation;

        private final MigrationExecutor executor;

        public ResolvedMigrationImpl(MigrationVersion version, String description, String script,
                                 Integer checksum, Integer equivalentChecksum,
                                 MigrationType type, String physicalLocation, MigrationExecutor executor) {
        this.version = version;
        this.description = description;
        this.script = script;
        this.checksum = checksum;
        this.equivalentChecksum = equivalentChecksum;
        this.type = type;
        this.physicalLocation = physicalLocation;
        this.executor = executor;
    }

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Integer getChecksum() {
        return checksum == null ?
                equivalentChecksum :
                checksum;
    }

    @Override
    public MigrationType getType() {
        return type;
    }

    @Override
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return executor;
    }

    public int compareTo(ResolvedMigrationImpl o) {
        return version.compareTo(o.version);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResolvedMigrationImpl migration = (ResolvedMigrationImpl) o;

        if (checksum != null ? !checksum.equals(migration.checksum) : migration.checksum != null) return false;
        if (equivalentChecksum != null ? !equivalentChecksum.equals(migration.equivalentChecksum) : migration.equivalentChecksum != null) return false;
        if (description != null ? !description.equals(migration.description) : migration.description != null)
            return false;
        if (script != null ? !script.equals(migration.script) : migration.script != null) return false;
        if (type != migration.type) return false;
        return Objects.equals(version, migration.version);
    }

    @Override
    public int hashCode() {
        int result = (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (equivalentChecksum != null ? equivalentChecksum.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResolvedMigrationImpl{" +
                "version=" + version +
                ", description='" + description + '\'' +
                ", script='" + script + '\'' +
                ", checksum=" + getChecksum() +
                ", type=" + type +
                ", physicalLocation='" + physicalLocation + '\'' +
                ", executor=" + executor +
                '}';
    }

        public void validate() {
    }

    @Override
    public boolean checksumMatches(Integer checksum) {
        return Objects.equals(checksum, this.checksum) ||
                Objects.equals(checksum, this.equivalentChecksum);
    }

    @Override
    public boolean checksumMatchesWithoutBeingIdentical(Integer checksum) {
        return Objects.equals(checksum, this.equivalentChecksum)
                && !Objects.equals(checksum, this.checksum);
    }
}