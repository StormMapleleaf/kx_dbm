package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.IOUtils;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileClassPathLocationScanner implements ClassPathLocationScanner {
    private static final Log LOG = LogFactory.getLog(JarFileClassPathLocationScanner.class);

        private final String separator;

        JarFileClassPathLocationScanner(String separator) { this.separator = separator; }

    public Set<String> findResourceNames(String location, URL locationUrl) {
        JarFile jarFile;
        try {
            jarFile = getJarFromUrl(locationUrl);
        } catch (IOException e) {
            LOG.warn("Unable to determine jar from url (" + locationUrl + "): " + e.getMessage());
            return Collections.emptySet();
        }

        try {
            String prefix = jarFile.getName().toLowerCase().endsWith(".war") ? "WEB-INF/classes/" : "";
            return findResourceNamesFromJarFile(jarFile, prefix, location);
        } finally {
            try {
                jarFile.close();
            } catch (IOException e) {
            }
        }
    }

        private JarFile getJarFromUrl(URL locationUrl) throws IOException {
        URLConnection con = locationUrl.openConnection();
        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(false);
            return jarCon.getJarFile();
        }

        String urlFile = locationUrl.getFile();

        int separatorIndex = urlFile.indexOf(separator);
        if (separatorIndex != -1) {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            if (jarFileUrl.startsWith("file:")) {
                try {
                    return new JarFile(new URL(jarFileUrl).toURI().getSchemeSpecificPart());
                } catch (URISyntaxException ex) {
                    return new JarFile(jarFileUrl.substring("file:".length()));
                }
            }
            return new JarFile(jarFileUrl);
        }

        return new JarFile(urlFile);
    }

        private Set<String> findResourceNamesFromJarFile(JarFile jarFile, String prefix, String location) {
        String toScan = prefix + location + (location.endsWith("/") ? "" : "/");
        Set<String> resourceNames = new TreeSet<>();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement().getName();
            if (entryName.startsWith(toScan)) {
                resourceNames.add(entryName.substring(prefix.length()));
            }
        }

        return resourceNames;
    }
}