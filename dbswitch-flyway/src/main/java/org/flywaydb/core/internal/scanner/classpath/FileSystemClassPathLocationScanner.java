package org.flywaydb.core.internal.scanner.classpath;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.UrlUtils;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

public class FileSystemClassPathLocationScanner implements ClassPathLocationScanner {
    private static final Log LOG = LogFactory.getLog(FileSystemClassPathLocationScanner.class);

    public Set<String> findResourceNames(String location, URL locationUrl) {
        String filePath = UrlUtils.toFilePath(locationUrl);
        File folder = new File(filePath);
        if (!folder.isDirectory()) {
            LOG.debug("Skipping path as it is not a directory: " + filePath);
            return new TreeSet<>();
        }

        String classPathRootOnDisk = filePath.substring(0, filePath.length() - location.length());
        if (!classPathRootOnDisk.endsWith(File.separator)) {
            classPathRootOnDisk = classPathRootOnDisk + File.separator;
        }
        LOG.debug("Scanning starting at classpath root in filesystem: " + classPathRootOnDisk);
        return findResourceNamesFromFileSystem(classPathRootOnDisk, location, folder);
    }

            @SuppressWarnings("ConstantConditions")
    Set<String> findResourceNamesFromFileSystem(String classPathRootOnDisk, String scanRootLocation, File folder) {
        LOG.debug("Scanning for resources in path: " + folder.getPath() + " (" + scanRootLocation + ")");

        Set<String> resourceNames = new TreeSet<>();

        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.canRead()) {
                if (file.isDirectory()) {
                    resourceNames.addAll(findResourceNamesFromFileSystem(classPathRootOnDisk, scanRootLocation, file));
                } else {
                    resourceNames.add(toResourceNameOnClasspath(classPathRootOnDisk, file));
                }
            }
        }

        return resourceNames;
    }

        private String toResourceNameOnClasspath(String classPathRootOnDisk, File file) {
        String fileName = file.getAbsolutePath().replace("\\", "/");

        return fileName.substring(classPathRootOnDisk.length());
    }
}