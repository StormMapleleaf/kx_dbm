package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;

public class ResourceName {
    private String prefix;
    private String version;
    private String separator;
    private String description;
    private String suffix;
    private boolean isValid;
    private String validityMessage;

    public ResourceName(String prefix, String version, String separator, String description, String suffix,
                        boolean isValid, String validityMessage){
        this.prefix = prefix;
        this.version = version;
        this.separator = separator;
        this.description = description;
        this.suffix = suffix;
        this.isValid = isValid;
        this.validityMessage = validityMessage;
    }

        public static ResourceName invalid(String message) {
        return new ResourceName(null, null, null, null,
                null, false, message);
    }

        public String getPrefix() {
        if (!isValid) {
            throw new FlywayException("Cannot access prefix of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return prefix;
    }

    private boolean isVersioned() {
        return (!"".equals(version));
    }

        public MigrationVersion getVersion() {
        if (isVersioned()) {
            return MigrationVersion.fromVersion(version);
        } else {
            return null;
        }
    }

        public String getDescription() {
        if (!isValid) {
            throw new FlywayException("Cannot access description of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return description;
    }

        public String getSuffix() {
        if (!isValid) {
            throw new FlywayException("Cannot access suffix of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return suffix;
    }

        public String getFilenameWithoutSuffix() {
        if (!isValid) {
            throw new FlywayException("Cannot access name of invalid ResourceNameParseResult\r\n" + validityMessage);
        }

        if ("".equals(description)) {
            return prefix + version;
        } else {
            return prefix + version + separator + description;
        }
    }

        public boolean isValid() {
        return isValid;
    }

        public String getValidityMessage() {
        return validityMessage;
    }
}