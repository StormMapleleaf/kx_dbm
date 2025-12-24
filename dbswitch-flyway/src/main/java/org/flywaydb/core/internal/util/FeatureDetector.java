package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

public final class FeatureDetector {
    private static final Log LOG = LogFactory.getLog(FeatureDetector.class);

        private ClassLoader classLoader;

        public FeatureDetector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

        private Boolean apacheCommonsLoggingAvailable;

        private Boolean slf4jAvailable;

        private Boolean jbossVFSv2Available;

        private Boolean jbossVFSv3Available;

        private Boolean osgiFrameworkAvailable;

        private Boolean androidAvailable;

        public boolean isApacheCommonsLoggingAvailable() {
        if (apacheCommonsLoggingAvailable == null) {
            apacheCommonsLoggingAvailable = ClassUtils.isPresent("org.apache.commons.logging.Log", classLoader);
        }

        return apacheCommonsLoggingAvailable;
    }

        public boolean isSlf4jAvailable() {
        if (slf4jAvailable == null) {
            slf4jAvailable = ClassUtils.isPresent("org.slf4j.Logger", classLoader);
        }

        return slf4jAvailable;
    }

        public boolean isJBossVFSv2Available() {
        if (jbossVFSv2Available == null) {
            jbossVFSv2Available = ClassUtils.isPresent("org.jboss.virtual.VFS", classLoader);
            LOG.debug("JBoss VFS v2 available: " + jbossVFSv2Available);
        }

        return jbossVFSv2Available;
    }

        public boolean isJBossVFSv3Available() {
        if (jbossVFSv3Available == null) {
            jbossVFSv3Available = ClassUtils.isPresent("org.jboss.vfs.VFS", classLoader);
            LOG.debug("JBoss VFS v3 available: " + jbossVFSv3Available);
        }

        return jbossVFSv3Available;
    }

        public boolean isOsgiFrameworkAvailable() {
        if (osgiFrameworkAvailable == null) {
            ClassLoader classLoader = FeatureDetector.class.getClassLoader();
            osgiFrameworkAvailable = ClassUtils.isPresent("org.osgi.framework.Bundle", classLoader);
            LOG.debug("OSGi framework available: " + osgiFrameworkAvailable);
        }

        return osgiFrameworkAvailable;
    }

        public boolean isAndroidAvailable() {
        if (androidAvailable == null) {
            androidAvailable = "Android Runtime".equals(System.getProperty("java.runtime.name"));
        }

        return androidAvailable;
    }
}