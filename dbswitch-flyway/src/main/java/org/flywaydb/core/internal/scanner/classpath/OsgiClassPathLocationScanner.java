package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.api.FlywayException;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OsgiClassPathLocationScanner implements ClassPathLocationScanner {
    private static final Pattern EQUINOX_BUNDLE_ID_PATTERN = Pattern.compile("^\\d+");

    private static final Pattern FELIX_BUNDLE_ID_PATTERN = Pattern.compile("^[0-9a-f\\-]{36}_(\\d+)\\.\\d+");

    public Set<String> findResourceNames(String location, URL locationUrl) {
        Set<String> resourceNames = new TreeSet<>();

        Bundle bundle = getTargetBundleFromContextOrCurrent(FrameworkUtil.getBundle(getClass()), locationUrl);
        Enumeration<URL> entries = bundle.findEntries(locationUrl.getPath(), "*", true);

        if (entries != null) {
            while (entries.hasMoreElements()) {
                URL entry = entries.nextElement();
                String resourceName = getPathWithoutLeadingSlash(entry);

                resourceNames.add(resourceName);
            }
        }

        return resourceNames;
    }

    private Bundle getTargetBundleFromContextOrCurrent(Bundle current, URL locationUrl) {
        Bundle target;
        try {
            target = current.getBundleContext().getBundle(hostToBundleId(locationUrl.getHost()));
        } catch (RuntimeException e) {
            return current;
        }
        return target != null ? target : current;
    }

    static long hostToBundleId(String host) {
        Matcher m = FELIX_BUNDLE_ID_PATTERN.matcher(host);
        if (m.find()) {
            return Double.valueOf(m.group(1)).longValue();
        } else {
            m = EQUINOX_BUNDLE_ID_PATTERN.matcher(host);
            if (m.find()) {
                return Double.valueOf(m.group()).longValue();
            }
        }
        throw new FlywayException("There's no bundleId in passed URL");
    }

    private String getPathWithoutLeadingSlash(URL entry) {
        final String path = entry.getPath();

        return path.startsWith("/") ? path.substring(1) : path;
    }
}