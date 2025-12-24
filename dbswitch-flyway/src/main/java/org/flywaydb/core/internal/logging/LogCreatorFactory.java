package org.flywaydb.core.internal.logging;

import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.logging.android.AndroidLogCreator;
import org.flywaydb.core.internal.logging.apachecommons.ApacheCommonsLogCreator;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator;

public class LogCreatorFactory {
        private LogCreatorFactory() {
    }

    public static LogCreator getLogCreator(ClassLoader classLoader, LogCreator fallbackLogCreator) {
        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isAndroidAvailable()) {
            return ClassUtils.instantiate(AndroidLogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isSlf4jAvailable()) {
            return ClassUtils.instantiate(Slf4jLogCreator.class.getName(), classLoader);
        }
        if (featureDetector.isApacheCommonsLoggingAvailable()) {
            return ClassUtils.instantiate(ApacheCommonsLogCreator.class.getName(), classLoader);
        }
        if (fallbackLogCreator == null) {
            return new JavaUtilLogCreator();
        }
        return fallbackLogCreator;
    }
}