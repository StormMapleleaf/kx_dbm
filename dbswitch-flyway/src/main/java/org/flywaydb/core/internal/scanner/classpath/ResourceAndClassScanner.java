package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.internal.resource.LoadableResource;

import java.util.Collection;

public interface ResourceAndClassScanner<I> {
        Collection<LoadableResource> scanForResources();

        Collection<Class<? extends I>> scanForClasses();
}