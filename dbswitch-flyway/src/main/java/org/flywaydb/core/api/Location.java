package org.flywaydb.core.api;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Location implements Comparable<Location> {
    private static final Log LOG = LogFactory.getLog(Location.class);

        private static final String CLASSPATH_PREFIX = "classpath:";

        public static final String FILESYSTEM_PREFIX = "filesystem:";

        private final String prefix;

        private String rawPath;

        private String rootPath;

    private Pattern pathRegex = null;

        public Location(String descriptor) {
        String normalizedDescriptor = descriptor.trim();

        if (normalizedDescriptor.contains(":")) {
            prefix = normalizedDescriptor.substring(0, normalizedDescriptor.indexOf(":") + 1);
            rawPath = normalizedDescriptor.substring(normalizedDescriptor.indexOf(":") + 1);
        } else {
            prefix = CLASSPATH_PREFIX;
            rawPath = normalizedDescriptor;
        }

        if (isClassPath()) {
            if (rawPath.contains(".")) {
                LOG.warn("Use of dots (.) as path separators will be deprecated in Flyway 7. Path: " + rawPath);
            }
            rawPath = rawPath.replace(".", "/");
            if (rawPath.startsWith("/")) {
                rawPath = rawPath.substring(1);
            }
            if (rawPath.endsWith("/")) {
                rawPath = rawPath.substring(0, rawPath.length() - 1);
            }
            processRawPath();
        } else if (isFileSystem()) {
            processRawPath();
            rootPath = new File(rootPath).getPath();

            if (pathRegex == null) {
                rawPath = new File(rawPath).getPath();
            }
        } else {
            throw new FlywayException("Unknown prefix for location (should be either filesystem: or classpath:): "
                    + normalizedDescriptor);
        }

        if (rawPath.endsWith(File.separator)) {
            rawPath = rawPath.substring(0, rawPath.length() - 1);
        }
    }

        private void processRawPath() {
        if (rawPath.contains("*") || rawPath.contains("?")) {

            String seperator = isFileSystem() ? File.separator : "/";
            String escapedSeperator = seperator.replace("\\", "\\\\").replace("/", "\\/");

            String[] pathSplit = rawPath.split("[\\\\/]");

            StringBuilder rootPart = new StringBuilder();
            StringBuilder patternPart = new StringBuilder();

            boolean endsInFile = false;
            boolean skipSeperator = false;
            boolean inPattern = false;
            for (String pathPart : pathSplit) {
                endsInFile = false;

                if (pathPart.contains("*") || pathPart.contains("?")) {
                    inPattern = true;
                }

                if (inPattern) {
                    if (skipSeperator) {
                        skipSeperator = false;
                    } else {
                        patternPart.append("/");
                    }

                    String regex;
                    if ("**".equals(pathPart)) {
                        regex = "([^/]+/)*?";

                        skipSeperator = true;
                    } else {
                        endsInFile = pathPart.contains(".");

                        regex = pathPart;
                        regex = regex.replace(".", "\\.");
                        regex = regex.replace("?", "[^/]");
                        regex = regex.replace("*", "[^/]+?");
                    }

                    patternPart.append(regex);
                } else {
                    rootPart.append(seperator).append(pathPart);
                }
            }

            rootPath = rootPart.length() > 0 ? rootPart.toString().substring(1) : "";

            String pattern = patternPart.toString().substring(1);

            pattern = pattern.replace("/", escapedSeperator);

            if (rootPart.length() > 0) {
                pattern = rootPath.replace(seperator, escapedSeperator) + escapedSeperator + pattern;
            }

            if (!endsInFile) {
                pattern = pattern + escapedSeperator + "(?<relpath>.*)";
            }

            pathRegex = Pattern.compile(pattern);
        } else {
            rootPath = rawPath;
        }
    }

        public boolean matchesPath(String path) {
        if (pathRegex == null) {
            return true;
        }

        return pathRegex.matcher(path).matches();
    }

        public String getPathRelativeToThis(String path) {
        if (pathRegex != null && pathRegex.pattern().contains("?<relpath>")) {
            Matcher matcher = pathRegex.matcher(path);
            if (matcher.matches()) {
                String relPath = matcher.group("relpath");
                if (relPath != null && relPath.length() > 0) {
                    return relPath;
                }
            }
        }

        return rootPath.length() > 0 ? path.substring(rootPath.length() + 1) : path;
    }

        public boolean isClassPath() {
        return CLASSPATH_PREFIX.equals(prefix);
    }

        public boolean isFileSystem() {
        return FILESYSTEM_PREFIX.equals(prefix);
    }

        @SuppressWarnings("SimplifiableIfStatement")
    public boolean isParentOf(Location other) {
        if (pathRegex != null || other.pathRegex != null) {
            return false;
        }

        if (isClassPath() && other.isClassPath()) {
            return (other.getDescriptor() + "/").startsWith(getDescriptor() + "/");
        }
        if (isFileSystem() && other.isFileSystem()) {
            return (other.getDescriptor() + File.separator).startsWith(getDescriptor() + File.separator);
        }
        return false;
    }

        public String getPrefix() {
        return prefix;
    }

        public String getRootPath() {
        return rootPath;
    }

        public String getPath() {
        return rawPath;
    }

        public Pattern getPathRegex() {
        return pathRegex;
    }

        public String getDescriptor() {
        return prefix + rawPath;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(Location o) {
        return getDescriptor().compareTo(o.getDescriptor());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return getDescriptor().equals(location.getDescriptor());
    }

    @Override
    public int hashCode() {
        return getDescriptor().hashCode();
    }

        @Override
    public String toString() {
        return getDescriptor();
    }
}