package org.flywaydb.core.api.logging;

import org.flywaydb.core.internal.logging.LogCreatorFactory;

public class LogFactory {
        private static LogCreator logCreator;

        private static LogCreator fallbackLogCreator;

        private LogFactory() {
    }

        public static void setLogCreator(LogCreator logCreator) {
        LogFactory.logCreator = logCreator;
    }

        public static void setFallbackLogCreator(LogCreator fallbackLogCreator) {
        LogFactory.fallbackLogCreator = fallbackLogCreator;
    }

        public static Log getLog(Class<?> clazz) {
        if (logCreator == null) {
            logCreator = LogCreatorFactory.getLogCreator(LogFactory.class.getClassLoader(), fallbackLogCreator);
        }

        return logCreator.createLogger(clazz);
    }
}