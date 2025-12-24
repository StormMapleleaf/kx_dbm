package org.flywaydb.core.internal.util;

public class BomFilter {
    private static final char BOM = '\ufeff';

        public static boolean isBom(char c) {
        return c == BOM;
    }

        public static String FilterBomFromString(String s) {
        if (s.isEmpty()) {
            return s;
        }

       if (isBom(s.charAt(0))) {
           return s.substring(1);
       }

       return s;
    }
}