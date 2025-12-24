package org.flywaydb.core.internal.resource;

import java.util.Collection;
import java.util.Collections;

public enum NoopResourceProvider implements ResourceProvider {
    INSTANCE;

    @Override
    public LoadableResource getResource(String name) {
        return null;
    }

        public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
        return Collections.emptyList();
    }
}