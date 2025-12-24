package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceProvider;

public interface SqlScriptFactory {
        SqlScript createSqlScript(LoadableResource resource, boolean mixed, ResourceProvider resourceProvider);
}