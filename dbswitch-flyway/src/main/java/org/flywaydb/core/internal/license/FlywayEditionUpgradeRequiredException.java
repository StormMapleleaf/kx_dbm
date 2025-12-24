package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.jdbc.DatabaseType;

public class FlywayEditionUpgradeRequiredException extends FlywayException {
    public FlywayEditionUpgradeRequiredException(Edition edition, DatabaseType databaseType, String version) {
        super(edition + " or " + databaseType + " upgrade required: " + databaseType + " " + version
                + " is no longer supported by " + VersionPrinter.EDITION + ","
                + " but still supported by " + edition + ".");
    }
}