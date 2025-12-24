package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.util.Pair;

import java.util.*;

public class ResourceNameParser {
    private final Configuration configuration;
    private final List<Pair<String, ResourceType>> prefixes;

    public ResourceNameParser(Configuration configuration) {
        this.configuration = configuration;
        prefixes = populatePrefixes(configuration);
    }

    public ResourceName parse(String resourceName) {
        Pair<String, String> suffixResult = stripSuffix(resourceName, configuration.getSqlMigrationSuffixes());

        Pair<String, ResourceType> prefix = findPrefix(suffixResult.getLeft(), prefixes);
        if (prefix != null) {

            Pair<String, String> prefixResult = stripPrefix(suffixResult.getLeft(), prefix.getLeft());
            String name = prefixResult.getRight();
            Pair<String, String> splitName = splitAtSeparator(name, configuration.getSqlMigrationSeparator());
            boolean isValid = true;
            String validationMessage = "";
            String exampleDescription = ("".equals(splitName.getRight())) ? "description" : splitName.getRight();

            if (!ResourceType.isVersioned(prefix.getRight())) {
                if (!"".equals(splitName.getLeft())) {
                    isValid = false;
                    validationMessage = "Invalid repeatable migration / callback name format: " + resourceName
                            + " (It cannot contain a version and should look like this: "
                            + prefixResult.getLeft() + configuration.getSqlMigrationSeparator() + exampleDescription + suffixResult.getRight() + ")";
                }
            } else {
                if ("".equals(splitName.getLeft())) {
                    isValid = false;
                    validationMessage = "Invalid versioned migration name format: " + resourceName
                            + " (It must contain a version and should look like this: "
                            + prefixResult.getLeft() + "1.2" + configuration.getSqlMigrationSeparator() + exampleDescription + suffixResult.getRight() + ")";
                } else {
                    try {
                        MigrationVersion.fromVersion(splitName.getLeft());
                    } catch (Exception e) {
                        isValid = false;
                        validationMessage = "Invalid versioned migration name format: " + resourceName
                                + " (could not recognise version number " + splitName.getLeft() + ")";
                    }
                }
            }

            String description = splitName.getRight().replace("_", " ");
            return new ResourceName(prefixResult.getLeft(), splitName.getLeft(),
                    configuration.getSqlMigrationSeparator(), description, suffixResult.getRight(),
                    isValid, validationMessage);
        }

        return ResourceName.invalid("Unrecognised migration name format: " + resourceName);
    }

    private Pair<String, ResourceType> findPrefix(String nameWithoutSuffix, List<Pair<String, ResourceType>> prefixes) {
        for (Pair<String, ResourceType> prefix: prefixes) {
            if (nameWithoutSuffix.startsWith(prefix.getLeft())) {
                return prefix;
            }
        }
        return null;
    }

    private Pair<String, String> stripSuffix(String name, String[] suffixes) {
        for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
                return Pair.of(name.substring(0, name.length() - suffix.length()), suffix);
            }
        }
        return Pair.of(name, "");
    }

    private Pair<String, String> stripPrefix(String fileName, String prefix) {
        if (fileName.startsWith(prefix)) {
            return Pair.of(prefix, fileName.substring(prefix.length()));
        }
        return null;
    }

    private Pair<String, String> splitAtSeparator(String name, String separator) {
        int separatorIndex = name.indexOf(separator);
        if (separatorIndex >= 0) {
            return Pair.of(name.substring(0, separatorIndex),
                    name.substring(separatorIndex + separator.length()));
        } else {
            return Pair.of(name, "");
        }
    }

    private List<Pair<String, ResourceType>> populatePrefixes(Configuration configuration) {
        List<Pair<String, ResourceType>> prefixes = new ArrayList<>();

        List<String> versionedPrefixes = new ArrayList<>();
        prefixes.add(Pair.of(configuration.getSqlMigrationPrefix(), ResourceType.MIGRATION));



        prefixes.add(Pair.of(configuration.getRepeatableSqlMigrationPrefix(), ResourceType.REPEATABLE_MIGRATION));
        for (Event event : Event.values()) {
            prefixes.add(Pair.of(event.getId(), ResourceType.CALLBACK));
        }

        Comparator<Pair<String, ResourceType>> prefixComparator
                = new Comparator<Pair<String, ResourceType>>() {
            public int compare(Pair<String, ResourceType> p1, Pair<String, ResourceType> p2) {
                return p2.getLeft().length() - p1.getLeft().length();
            }
        };

        Collections.sort(prefixes, prefixComparator);
        return prefixes;
    }
}