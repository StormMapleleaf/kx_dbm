package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;

import java.util.Date;
import java.util.Objects;

public class AppliedMigration implements Comparable<AppliedMigration> {
        private final int installedRank;

        private final MigrationVersion version;

        private final String description;

        private final MigrationType type;

        private final String script;

        private final Integer checksum;

        private final Date installedOn;

        private final String installedBy;

        private final int executionTime;

        private final boolean success;

        public AppliedMigration(int installedRank, MigrationVersion version, String description,
                     MigrationType type, String script, Integer checksum, Date installedOn,
                     String installedBy, int executionTime, boolean success) {
        this.installedRank = installedRank;
        this.version = version;
        this.description = description;
        this.type = type;
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

        public int getInstalledRank() {
        return installedRank;
    }

        public MigrationVersion getVersion() {
        return version;
    }

        public String getDescription() {
        return description;
    }

        public MigrationType getType() {
        return type;
    }

        public String getScript() {
        return script;
    }

        public Integer getChecksum() {
        return checksum;
    }

        public Date getInstalledOn() {
        return installedOn;
    }

        public String getInstalledBy() {
        return installedBy;
    }

        public int getExecutionTime() {
        return executionTime;
    }

        public boolean isSuccess() {
        return success;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppliedMigration that = (AppliedMigration) o;

        if (executionTime != that.executionTime) return false;
        if (installedRank != that.installedRank) return false;
        if (success != that.success) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;
        if (!description.equals(that.description)) return false;
        if (installedBy != null ? !installedBy.equals(that.installedBy) : that.installedBy != null) return false;
        if (installedOn != null ? !installedOn.equals(that.installedOn) : that.installedOn != null) return false;
        if (!script.equals(that.script)) return false;
        if (type != that.type) return false;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = installedRank;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + description.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + script.hashCode();
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (installedOn != null ? installedOn.hashCode() : 0);
        result = 31 * result + (installedBy != null ? installedBy.hashCode() : 0);
        result = 31 * result + executionTime;
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(AppliedMigration o) {
        return installedRank - o.installedRank;
    }
}