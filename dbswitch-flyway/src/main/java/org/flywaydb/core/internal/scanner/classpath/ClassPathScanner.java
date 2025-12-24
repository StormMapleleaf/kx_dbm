package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv2UrlResolver;
import org.flywaydb.core.internal.scanner.classpath.jboss.JBossVFSv3ClassPathLocationScanner;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.UrlUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class ClassPathScanner<I> implements ResourceAndClassScanner<I> {
    private static final Log LOG = LogFactory.getLog(ClassPathScanner.class);

    private final Class<I> implementedInterface;
        private final ClassLoader classLoader;
    private final Location location;

    private final Set<LoadableResource> resources = new TreeSet<>();

        private final Map<Location, List<URL>> locationUrlCache = new HashMap<>();

        private final LocationScannerCache locationScannerCache;

        private final ResourceNameCache resourceNameCache;

        public ClassPathScanner(Class<I> implementedInterface, ClassLoader classLoader, Charset encoding, Location location,
                            ResourceNameCache resourceNameCache,
                            LocationScannerCache locationScannerCache) {
        this.implementedInterface = implementedInterface;
        this.classLoader = classLoader;
        this.location = location;
        this.resourceNameCache = resourceNameCache;
        this.locationScannerCache = locationScannerCache;

        LOG.debug("Scanning for classpath resources at '" + location + "' ...");
        for (String resourceName : findResourceNames()) {
            resources.add(new ClassPathResource(location, resourceName, classLoader, encoding));
            LOG.debug("Found resource: " + resourceName);
        }
    }

    @Override
    public Collection<LoadableResource> scanForResources() {
        return resources;
    }

    @Override
    public Collection<Class<? extends I>> scanForClasses() {
        LOG.debug("Scanning for classes at " + location);

        List<Class<? extends I>> classes = new ArrayList<>();

        for (LoadableResource resource : resources) {
            if (resource.getAbsolutePath().endsWith(".class")) {
                Class<? extends I> clazz = ClassUtils.loadClass(
                        implementedInterface,
                        toClassName(resource.getAbsolutePath()),
                        classLoader);
                if (clazz != null) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }

        private String toClassName(String resourceName) {
        String nameWithDots = resourceName.replace("/", ".");
        return nameWithDots.substring(0, (nameWithDots.length() - ".class".length()));
    }

        private Set<String> findResourceNames() {
        Set<String> resourceNames = new TreeSet<>();

        List<URL> locationUrls = getLocationUrlsForPath(location);
        for (URL locationUrl : locationUrls) {
            LOG.debug("Scanning URL: " + locationUrl.toExternalForm());

            UrlResolver urlResolver = createUrlResolver(locationUrl.getProtocol());
            URL resolvedUrl = urlResolver.toStandardJavaUrl(locationUrl);

            String protocol = resolvedUrl.getProtocol();
            ClassPathLocationScanner classPathLocationScanner = createLocationScanner(protocol);
            if (classPathLocationScanner == null) {
                String scanRoot = UrlUtils.toFilePath(resolvedUrl);
                LOG.warn("Unable to scan location: " + scanRoot + " (unsupported protocol: " + protocol + ")");
            } else {
                Set<String> names = resourceNameCache.get(classPathLocationScanner, resolvedUrl);
                if (names == null) {
                    names = classPathLocationScanner.findResourceNames(location.getRootPath(), resolvedUrl);
                    resourceNameCache.put(classPathLocationScanner, resolvedUrl, names);
                }
                Set<String> filteredNames = new HashSet<>();
                for (String name : names) {
                    if (location.matchesPath(name)) {
                        filteredNames.add(name);
                    }
                }

                resourceNames.addAll(filteredNames);
            }
        }

        boolean locationResolved = !locationUrls.isEmpty();

        boolean isClassPathRoot = location.isClassPath() && "".equals(location.getRootPath());

        if (!locationResolved || isClassPathRoot) {
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    if ("file".equals(url.getProtocol())
                            && url.getPath().endsWith(".jar")
                            && !url.getPath().matches(".*" + Pattern.quote("/jre/lib/") + ".*")) {
                        JarFile jarFile;
                        try {
                            try {
                                jarFile = new JarFile(url.toURI().getSchemeSpecificPart());
                            } catch (URISyntaxException ex) {
                                jarFile = new JarFile(url.getPath().substring("file:".length()));
                            }
                        } catch (IOException | SecurityException e) {
                            LOG.warn("Skipping unloadable jar file: " + url + " (" + e.getMessage() + ")");
                            continue;
                        }

                        try {
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                String entryName = entries.nextElement().getName();
                                if (entryName.startsWith(location.getRootPath())) {
                                    locationResolved = true;
                                    resourceNames.add(entryName);
                                }
                            }
                        } finally {
                            try {
                                jarFile.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }

        if (!locationResolved) {
            LOG.warn("Unable to resolve location " + location + ". Note this warning will become an error in Flyway 7.");
        }

        return resourceNames;
    }

        private List<URL> getLocationUrlsForPath(Location location) {
        if (locationUrlCache.containsKey(location)) {
            return locationUrlCache.get(location);
        }

        LOG.debug("Determining location urls for " + location + " using ClassLoader " + classLoader + " ...");

        List<URL> locationUrls = new ArrayList<>();

        if (classLoader.getClass().getName().startsWith("com.ibm")) {
            Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getRootPath() + "/flyway.location");
                if (!urls.hasMoreElements()) {
                    LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + ")"
                            + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!\nNote this warning will become an error in Flyway 7.");
                }
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    locationUrls.add(new URL(UrlUtils.decodeURL(url.toExternalForm()).replace("/flyway.location", "")));
                }
            } catch (IOException e) {
                LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + ")"
                        + " On WebSphere an empty file named flyway.location must be present on the classpath location for WebSphere to find it!\nNote this warning will become an error in Flyway 7.");
            }
        } else {
            Enumeration<URL> urls;
            try {
                urls = classLoader.getResources(location.getRootPath());
                while (urls.hasMoreElements()) {
                    locationUrls.add(urls.nextElement());
                }
            } catch (IOException e) {
                LOG.warn("Unable to resolve location " + location + " (ClassLoader: " + classLoader + "): " + e.getMessage() + "\nNote this warning will become an error in Flyway 7.");
            }
        }

        locationUrlCache.put(location, locationUrls);

        return locationUrls;
    }

        private UrlResolver createUrlResolver(String protocol) {
        if (new FeatureDetector(classLoader).isJBossVFSv2Available() && protocol.startsWith("vfs")) {
            return new JBossVFSv2UrlResolver();
        }

        return new DefaultUrlResolver();
    }

        private ClassPathLocationScanner createLocationScanner(String protocol) {
        if (locationScannerCache.containsKey(protocol)) {
            return locationScannerCache.get(protocol);
        }

        if ("file".equals(protocol)) {
            FileSystemClassPathLocationScanner locationScanner = new FileSystemClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        if ("jar".equals(protocol) || isTomcat(protocol) || isWebLogic(protocol) || isWebSphere(protocol)) {
            String separator = isTomcat(protocol) ? "*/" : "!/";
            ClassPathLocationScanner locationScanner = new JarFileClassPathLocationScanner(separator);
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        FeatureDetector featureDetector = new FeatureDetector(classLoader);
        if (featureDetector.isJBossVFSv3Available() && "vfs".equals(protocol)) {
            JBossVFSv3ClassPathLocationScanner locationScanner = new JBossVFSv3ClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }
        if (featureDetector.isOsgiFrameworkAvailable() && (isFelix(protocol) || isEquinox(protocol))) {
            OsgiClassPathLocationScanner locationScanner = new OsgiClassPathLocationScanner();
            locationScannerCache.put(protocol, locationScanner);
            resourceNameCache.put(locationScanner, new HashMap<>());
            return locationScanner;
        }

        return null;
    }

    private boolean isEquinox(String protocol) {
        return "bundleresource".equals(protocol);
    }

    private boolean isFelix(String protocol) {
        return "bundle".equals(protocol);
    }

    private boolean isWebSphere(String protocol) {
        return "wsjar".equals(protocol);
    }

    private boolean isWebLogic(String protocol) {
        return "zip".equals(protocol);
    }

    private boolean isTomcat(String protocol) {
        return "war".equals(protocol);
    }
}