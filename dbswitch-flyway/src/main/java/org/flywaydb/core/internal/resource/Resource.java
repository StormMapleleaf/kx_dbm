package org.flywaydb.core.internal.resource;

public interface Resource {
        String getAbsolutePath();

        String getAbsolutePathOnDisk();

        String getFilename();

        String getRelativePath();
}