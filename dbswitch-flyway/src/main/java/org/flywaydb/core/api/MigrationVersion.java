package org.flywaydb.core.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MigrationVersion implements Comparable<MigrationVersion> {
        public static final MigrationVersion EMPTY = new MigrationVersion(null, "<< Empty Schema >>");

        public static final MigrationVersion LATEST = new MigrationVersion(BigInteger.valueOf(-1), "<< Latest Version >>");

        public static final MigrationVersion CURRENT = new MigrationVersion(BigInteger.valueOf(-2), "<< Current Version >>");

        private static final Pattern SPLIT_REGEX = Pattern.compile("\\.(?=\\d)");

        private final List<BigInteger> versionParts;

        private final String displayText;

        @SuppressWarnings("ConstantConditions")
    public static MigrationVersion fromVersion(String version) {
        if ("current".equalsIgnoreCase(version)) return CURRENT;
        if ("latest".equalsIgnoreCase(version) || LATEST.getVersion().equals(version)) return LATEST;
        if (version == null) return EMPTY;
        return new MigrationVersion(version);
    }

        private MigrationVersion(String version) {
        String normalizedVersion = version.replace('_', '.');
        this.versionParts = tokenize(normalizedVersion);
        this.displayText = normalizedVersion;
    }

        private MigrationVersion(BigInteger version, String displayText) {
        this.versionParts = new ArrayList<>();
        this.versionParts.add(version);
        this.displayText = displayText;
    }

        @Override
    public String toString() {
        return displayText;
    }

        public String getVersion() {
        if (this.equals(EMPTY)) return null;
        if (this.equals(LATEST)) return Long.toString(Long.MAX_VALUE);
        return displayText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationVersion version1 = (MigrationVersion) o;

        return compareTo(version1) == 0;
    }

    @Override
    public int hashCode() {
        return versionParts == null ? 0 : versionParts.hashCode();
    }

        public boolean isAtLeast(String otherVersion) {
        return compareTo(MigrationVersion.fromVersion(otherVersion)) >= 0;
    }

        public boolean isNewerThan(String otherVersion) {
        return compareTo(MigrationVersion.fromVersion(otherVersion)) > 0;
    }

        public boolean isMajorNewerThan(String otherVersion) {
        return getMajor().compareTo(MigrationVersion.fromVersion(otherVersion).getMajor()) > 0;
    }

        public BigInteger getMajor() {
        return versionParts.get(0);
    }

        public String getMajorAsString() {
        return versionParts.get(0).toString();
    }

        public String getMinorAsString() {
        if (versionParts.size() == 1) {
            return "0";
        }
        return versionParts.get(1).toString();
    }

    @Override
    public int compareTo(MigrationVersion o) {
        if (o == null) {
            return 1;
        }

        if (this == EMPTY) {
            if (o == EMPTY) return 0;
            else return -1;
        }

        if (this == CURRENT) {
            return o == CURRENT ? 0 : -1;
        }

        if (this == LATEST) {
            if (o == LATEST) return 0;
            else return 1;
        }

        if (o == EMPTY) {
            return 1;
        }

        if (o == CURRENT) {
            return 1;
        }

        if (o == LATEST) {
            return -1;
        }
        final List<BigInteger> parts1 = versionParts;
        final List<BigInteger> parts2 = o.versionParts;
        int largestNumberOfParts = Math.max(parts1.size(), parts2.size());
        for (int i = 0; i < largestNumberOfParts; i++) {
            final int compared = getOrZero(parts1, i).compareTo(getOrZero(parts2, i));
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }

    private BigInteger getOrZero(List<BigInteger> elements, int i) {
        return i < elements.size() ? elements.get(i) : BigInteger.ZERO;
    }

        private List<BigInteger> tokenize(String versionStr) {
        List<BigInteger> parts = new ArrayList<>();
        for (String part : SPLIT_REGEX.split(versionStr)) {
            parts.add(toBigInteger(versionStr, part));
        }

        for (int i = parts.size() - 1; i > 0; i--) {
            if (!parts.get(i).equals(BigInteger.ZERO)) {
                break;
            }
            parts.remove(i);
        }

        return parts;
    }

    private BigInteger toBigInteger(String versionStr, String part) {
        try {
            return new BigInteger(part);
        } catch (NumberFormatException e) {
            throw new FlywayException("Version may only contain 0..9 and . (dot). Invalid version: " + versionStr);
        }
    }
}