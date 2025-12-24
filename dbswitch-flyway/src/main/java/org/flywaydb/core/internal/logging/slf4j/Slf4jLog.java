package org.flywaydb.core.internal.logging.slf4j;

import org.flywaydb.core.api.logging.Log;
import org.slf4j.Logger;

public class Slf4jLog implements Log {
        private final Logger logger;

        public Slf4jLog(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Exception e) {
        logger.error(message, e);
    }
}