package org.flywaydb.core.internal.license;

import org.flywaydb.core.api.FlywayException;

public class FlywayProUpgradeRequiredException extends FlywayException {
    public FlywayProUpgradeRequiredException(String feature) {
        super(Edition.PRO + " or " + Edition.ENTERPRISE + " upgrade required: " + feature
                + " is not supported by " + Edition.COMMUNITY + ".");
    }
}