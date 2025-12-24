package org.flywaydb.core.internal.scanner.classpath;

import java.net.URL;

public class DefaultUrlResolver implements UrlResolver {
    public URL toStandardJavaUrl(URL url) {
        return url;
    }
}