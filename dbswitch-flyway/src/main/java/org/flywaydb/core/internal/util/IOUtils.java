package org.flywaydb.core.internal.util;

public class IOUtils {
    private IOUtils() {
    }

        public static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
        }
    }
}