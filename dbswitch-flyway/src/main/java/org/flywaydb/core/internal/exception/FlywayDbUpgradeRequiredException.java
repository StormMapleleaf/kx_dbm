package org.flywaydb.core.internal.exception;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.DatabaseType;

public class FlywayDbUpgradeRequiredException extends FlywayException {
    public FlywayDbUpgradeRequiredException(DatabaseType databaseType, String version, String minimumVersion) {
        super(databaseType + " upgrade required: " + databaseType + " " + version
                + " is outdated and no longer supported by Flyway. Flyway currently supports " + databaseType + " "
                + minimumVersion + " and newer.");
    }
}