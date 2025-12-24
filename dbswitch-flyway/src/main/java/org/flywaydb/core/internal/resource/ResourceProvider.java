package org.flywaydb.core.internal.resource;

import java.util.Collection;

public interface ResourceProvider {
        LoadableResource getResource(String name);

        Collection<LoadableResource> getResources(String prefix, String[] suffixes);
}