package org.flywaydb.core.internal.clazz;

import java.util.Collection;
import java.util.Collections;

public enum NoopClassProvider implements ClassProvider {
    INSTANCE;

    @Override
    public Collection<Class<?>> getClasses() {
        return Collections.emptyList();
    }
}