package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    private static final Log LOG = LogFactory.getLog(ClassUtils.class);

        private ClassUtils() {
    }

        @SuppressWarnings({"unchecked"})
    public static synchronized <T> T instantiate(String className, ClassLoader classLoader) {
        try {
            return (T) Class.forName(className, true, classLoader).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + className + " : " + e.getMessage(), e);
        }
    }

    public static synchronized <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new FlywayException("Unable to instantiate class " + clazz.getName() + " : " + e.getMessage(), e);
        }
    }

        public static <T> List<T> instantiateAll(String[] classes, ClassLoader classLoader) {
        List<T> clazzes = new ArrayList<>();
        for (String clazz : classes) {
            if (StringUtils.hasLength(clazz)) {
                clazzes.add(ClassUtils.<T>instantiate(clazz, classLoader));
            }
        }
        return clazzes;
    }

        public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

        public static <I> Class<? extends I> loadClass(Class<I> implementedInterface, String className, ClassLoader classLoader) {
        try {
            Class<?> clazz = classLoader.loadClass(className);

            if (!implementedInterface.isAssignableFrom(clazz)) {
                return null;
            }

            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnonymousClass()) {
                LOG.debug("Skipping non-instantiable class: " + className);
                return null;
            }

            clazz.getDeclaredConstructor().newInstance();
            LOG.debug("Found class: " + className);
            return (Class<? extends I>) clazz;
        } catch (Throwable e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            LOG.warn("Skipping " + className + ": " + formatThrowable(e) + (
                    rootCause == e
                            ? ""
                            : " caused by " + formatThrowable(rootCause)
                            + " at " + ExceptionUtils.getThrowLocation(rootCause)
            ));
            return null;
        }
    }

    private static String formatThrowable(Throwable e) {
        return "(" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
    }

        public static String getLocationOnDisk(Class<?> aClass) {
        ProtectionDomain protectionDomain = aClass.getProtectionDomain();
        if (protectionDomain == null) {
            return null;
        }
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return null;
        }
        return UrlUtils.decodeURL(codeSource.getLocation().getPath());
    }

        public static ClassLoader addJarsOrDirectoriesToClasspath(ClassLoader classLoader, List<File> jarFiles) {
        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            LOG.debug("Adding location to classpath: " + jarFile.getAbsolutePath());

            try {
                urls.add(jarFile.toURI().toURL());
            } catch (Exception e) {
                throw new FlywayException("Unable to load " + jarFile.getPath(), e);
            }
        }
        return new URLClassLoader(urls.toArray(new URL[0]), classLoader);
    }


        public static String getStaticFieldValue(String className, String fieldName, ClassLoader classLoader) {
        try {
            Class clazz = Class.forName(className, true, classLoader);
            Field field = clazz.getField(fieldName);
            return (String)field.get(null);
        } catch (Exception e) {
            throw new FlywayException("Unable to obtain field value " + className + "." + fieldName + " : " + e.getMessage(), e);
        }
    }
}