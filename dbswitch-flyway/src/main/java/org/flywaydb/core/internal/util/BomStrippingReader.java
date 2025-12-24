package org.flywaydb.core.internal.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class BomStrippingReader extends FilterReader {
    private static final int EMPTY_STREAM = -1;

        public BomStrippingReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != EMPTY_STREAM && BomFilter.isBom((char)c)) {
            return super.read();
        }
        return c;
    }
}