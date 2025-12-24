package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class UrlUtils {
        private UrlUtils() {
    }

        public static String toFilePath(URL url) {
        String filePath = new File(decodeURL(url.getPath().replace("+", "%2b"))).getAbsolutePath();
        if (filePath.endsWith("/")) {
            return filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

        public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Can never happen", e);
        }
    }
}