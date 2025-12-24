package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;

import java.util.HashMap;
import java.util.Map;

import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;

public class SqlScriptMetadata {
    private static final Log LOG = LogFactory.getLog(SqlScriptMetadata.class);
    private static final String EXECUTE_IN_TRANSACTION = "executeInTransaction";

    private final Boolean executeInTransaction;

    private SqlScriptMetadata(Map<String, String> metadata) {
        metadata = new HashMap<>(metadata);
        this.executeInTransaction = removeBoolean(metadata, EXECUTE_IN_TRANSACTION);

        ConfigUtils.checkConfigurationForUnrecognisedProperties(metadata, null);
    }

    public Boolean executeInTransaction() {
        return executeInTransaction;
    }

    public static SqlScriptMetadata fromResource(LoadableResource resource) {
        if (resource != null) {
            LOG.debug("Found script configuration: " + resource.getFilename());
            return new SqlScriptMetadata(ConfigUtils.loadConfigurationFromReader(resource.read()));
        }
        return new SqlScriptMetadata(new HashMap<>());
    }

    public static LoadableResource getMetadataResource(ResourceProvider resourceProvider, LoadableResource resource) {
        if (resourceProvider == null) {
            return null;
        }
        return resourceProvider.getResource(resource.getRelativePath() + ".conf");
    }
}