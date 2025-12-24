package org.flywaydb.core.internal.clazz;

import java.util.Collection;

public interface ClassProvider<I> {
        Collection<Class<? extends I>> getClasses();
}