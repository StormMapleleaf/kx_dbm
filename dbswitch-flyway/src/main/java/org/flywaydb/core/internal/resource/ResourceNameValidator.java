package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourceNameValidator {
    private static final Log LOG = LogFactory.getLog(ResourceNameValidator.class);

        public void validateSQLMigrationNaming(ResourceProvider provider, Configuration configuration) {

        List<String> errorsFound = new ArrayList<>();
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (Resource resource : getAllSqlResources(provider, configuration)) {
            LOG.debug("Validating " + resource.getFilename());

            ResourceName result = resourceNameParser.parse(resource.getFilename());
            if (!result.isValid()) {
                errorsFound.add(result.getValidityMessage());
            }
        }

        if (!errorsFound.isEmpty()) {
            throw new FlywayException("Invalid SQL filenames found:\r\n" + StringUtils.collectionToDelimitedString(errorsFound, "\r\n"));
        }
    }

    private Collection<LoadableResource> getAllSqlResources(ResourceProvider provider, Configuration configuration) {
        return provider.getResources("", configuration.getSqlMigrationSuffixes());
    }
}