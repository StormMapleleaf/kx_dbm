package org.flywaydb.core.internal.util;

public class TimeFormat {
        private TimeFormat() {
    }

        public static String format(long millis) {
        return String.format("%02d:%02d.%03ds", millis / 60000, (millis % 60000) / 1000, (millis % 1000));
    }
}