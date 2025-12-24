package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.BomFilter;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public abstract class LoadableResource implements Resource, Comparable<LoadableResource> {

    private Integer checksum;

        public abstract Reader read();












    @Override
    public int compareTo(LoadableResource o) {
        return getRelativePath().compareTo(o.getRelativePath());
    }
}