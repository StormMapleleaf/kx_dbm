package org.flywaydb.core.internal.logging.android;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

public class AndroidLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new AndroidLog();
    }
}