package org.flywaydb.core.internal.scanner.filesystem;

import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.filesystem.FileSystemResource;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class FileSystemScanner {
    private static final Log LOG = LogFactory.getLog(FileSystemScanner.class);
    private final Charset encoding;





        public FileSystemScanner(Charset encoding



    ) {
        this.encoding = encoding;



    }

        public Collection<LoadableResource> scanForResources(Location location) {
        String path = location.getRootPath();
        LOG.debug("Scanning for filesystem resources at '" + path + "'");

        File dir = new File(path);
        if (!dir.exists()) {
            LOG.warn("Skipping filesystem location:" + path + " (not found). Note this warning will become an error in Flyway 7.");
            return Collections.emptyList();
        }
        if (!dir.canRead()) {
            LOG.warn("Skipping filesystem location:" + path + " (not readable). Note this warning will become an error in Flyway 7.");
            return Collections.emptyList();
        }
        if (!dir.isDirectory()) {
            LOG.warn("Skipping filesystem location:" + path + " (not a directory). Note this warning will become an error in Flyway 7.");
            return Collections.emptyList();
        }

        Set<LoadableResource> resources = new TreeSet<>();

        for (String resourceName : findResourceNamesFromFileSystem(path, new File(path))) {

            if (location.matchesPath(resourceName)) {
                resources.add(new FileSystemResource(location, resourceName, encoding



                ));
                LOG.debug("Found filesystem resource: " + resourceName);
            }
        }

        return resources;
    }

        @SuppressWarnings("ConstantConditions")
    private Set<String> findResourceNamesFromFileSystem(String scanRootLocation, File folder) {
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        Set<String> resourceNames = new TreeSet<>();

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.canRead()) {
                if (file.isDirectory()) {
                    if (file.isHidden()) {
                        LOG.debug("Skipping hidden directory: " + file.getAbsolutePath());
                    } else {
                        resourceNames.addAll(findResourceNamesFromFileSystem(scanRootLocation, file));
                    }
                } else {
                    resourceNames.add(file.getPath());
                }
            }
        }

        return resourceNames;
    }
}