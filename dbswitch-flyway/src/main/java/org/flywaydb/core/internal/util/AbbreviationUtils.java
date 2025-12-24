package org.flywaydb.core.internal.util;

public class AbbreviationUtils {
        private AbbreviationUtils() {
    }

        public static String abbreviateDescription(String description) {
        if (description == null) {
            return null;
        }

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
    }

        public static String abbreviateScript(String script) {
        if (script == null) {
            return null;
        }

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
    }
}