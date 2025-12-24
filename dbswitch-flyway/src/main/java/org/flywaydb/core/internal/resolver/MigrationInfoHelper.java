package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public class MigrationInfoHelper {
        private MigrationInfoHelper() {
    }

        public static Pair<MigrationVersion, String> extractVersionAndDescription(String migrationName,
                                                                              String prefix, String separator,
                                                                              String[] suffixes, boolean repeatable) {
        String cleanMigrationName = cleanMigrationName(migrationName, prefix, suffixes);

        int separatorPos = cleanMigrationName.indexOf(separator);

        String version;
        String description;
        if (separatorPos < 0) {
            version = cleanMigrationName;
            description = "";
        } else {
            version = cleanMigrationName.substring(0, separatorPos);
            description = cleanMigrationName.substring(separatorPos + separator.length()).replace("_", " ");
        }

        if (StringUtils.hasText(version)) {
            if (repeatable) {
                throw new FlywayException("Wrong repeatable migration name format: " + migrationName
                        + " (It cannot contain a version and should look like this: "
                        + prefix + separator + description + suffixes[0] + ")");
            }
            try {
                return Pair.of(MigrationVersion.fromVersion(version), description);
            } catch (Exception e) {
                throw new FlywayException("Wrong versioned migration name format: " + migrationName
                        + " (could not recognise version number " + version + ")", e);
            }
        }

        if (!repeatable) {
            throw new FlywayException("Wrong versioned migration name format: " + migrationName
                    + " (It must contain a version and should look like this: "
                    + prefix + "1.2" + separator + description + suffixes[0] + ")");
        }
        return Pair.of(null, description);
    }

    private static String cleanMigrationName(String migrationName, String prefix, String[] suffixes) {
        for (String suffix : suffixes) {
            if (migrationName.endsWith(suffix)) {
                return migrationName.substring(
                        StringUtils.hasLength(prefix) ? prefix.length() : 0,
                        migrationName.length() - suffix.length());
            }
        }
        return migrationName;
    }
}