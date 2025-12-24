package org.flywaydb.core.internal.parser;

import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

public class PeekingReader extends FilterReader {
    private int[] peekBuffer = new int[256];
    private int peekMax = 0;
    private int peekBufferOffset = 0;

    PeekingReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        peekBufferOffset++;
        return super.read();
    }

        public void swallow() throws IOException {
        read();
    }

        public void swallow(int n) throws IOException {
        for (int i = 0; i < n; i++) {
            read();
        }
    }

    private int peek() throws IOException {
        if (peekBufferOffset >= peekMax) {
            refillPeekBuffer();
        }

        return peekBuffer[peekBufferOffset];
    }

    private void refillPeekBuffer() throws IOException {
        mark(peekBuffer.length);
        peekMax = peekBuffer.length;
        peekBufferOffset = 0;
        for (int i = 0; i < peekBuffer.length; i++) {
            int read = super.read();
            peekBuffer[i] = read;
            if (read == '\n') {
                peekMax = i;
                break;
            }
        }
        reset();
    }

        public boolean peek(char c) throws IOException {
        int r = peek();
        return r != -1 && c == (char) r;
    }

        public boolean peek(char c1, char c2) throws IOException {
        int r = peek();
        return r != -1 && (c1 == (char) r || c2 == (char) r);
    }

        public boolean peekNumeric() throws IOException {
        int r = peek();
        return isNumeric(r);
    }

    private boolean isNumeric(int r) {
        return r != -1 && (char) r >= '0' && (char) r <= '9';
    }

        public boolean peekWhitespace() throws IOException {
        int r = peek();
        return isWhitespace(r);
    }

    private boolean isWhitespace(int r) {
        return r != -1 && Character.isWhitespace((char) r);
    }

        public boolean peekKeywordPart(ParserContext context) throws IOException {
        int r = peek();
        return isKeywordPart(r, context);
    }

    private boolean isKeywordPart(int r, ParserContext context) {
        return r != -1 && ((char) r == '_' || (char) r == '$' || Character.isLetterOrDigit((char) r) || context.isLetter((char)r));
    }

        public boolean peek(String str) throws IOException {
        return str.equals(peek(str.length()));
    }

        public String peek(int numChars) throws IOException {
        if (numChars >= peekBuffer.length) {
            resizePeekBuffer(numChars);
        }

        if (peekBufferOffset + numChars >= peekMax) {
            refillPeekBuffer();
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numChars; i++) {
            int r = peekBuffer[peekBufferOffset + i];
            if (r == -1) {
                break;
            } else if (peekBufferOffset + i > peekMax) {
                break;
            }
            result.append((char) r);
        }
        if (result.length() == 0) {
            return null;
        }
        return result.toString();
    }

        public char peekNextNonWhitespace() throws IOException {
        int i = 1;
        String c = peek(i++);
        while (c.trim().isEmpty()) {
            c = peek(i++);
        }

        return c.charAt(c.length()-1);
    }

    private void resizePeekBuffer(int newSize) {
        peekBuffer = Arrays.copyOf(peekBuffer, newSize + peekBufferOffset);
    }

        public void swallowUntilExcluding(char delimiter1, char delimiter2) throws IOException {
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            int r = read();
            if (r == -1) {
                break;
            }
        } while (true);
    }

        public String readUntilExcluding(char delimiter1, char delimiter2) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peek(delimiter1, delimiter2)) {
                break;
            }
            int r = read();
            if (r == -1) {
                break;
            } else {
                result.append((char) r);
            }
        } while (true);
        return result.toString();
    }

        public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
        swallowUntilExcludingWithEscape(delimiter, selfEscape, (char) 0);
    }

        public void swallowUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
            if (escape != 0 && c == escape) {
                swallow();
                continue;
            }
            if (c == delimiter) {
                if (selfEscape && peek(delimiter)) {
                    swallow();
                    continue;
                }
                break;
            }
        } while (true);
    }

        public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape) throws IOException {
        return readUntilExcludingWithEscape(delimiter, selfEscape, (char) 0);
    }

        public String readUntilExcludingWithEscape(char delimiter, boolean selfEscape, char escape) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
            if (escape != 0 && c == escape) {
                int r2 = read();
                if (r2 == -1) {
                    result.append(escape);
                    break;
                }
                char c2 = (char) r2;
                result.append(c2);
                continue;
            }
            if (c == delimiter) {
                if (selfEscape && peek(delimiter)) {
                    result.append(delimiter);
                    continue;
                }
                break;
            }
            result.append(c);
        } while (true);
        return result.toString();
    }

        public void swallowUntilExcluding(String str) throws IOException {
        do {
            if (peek(str)) {
                break;
            }
            int r = read();
            if (r == -1) {
                break;
            }
        } while (true);
    }

        public String readUntilExcluding(String str) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peek(str)) {
                break;
            }
            int r = read();
            if (r == -1) {
                break;
            } else {
                result.append((char) r);
            }
        } while (true);
        return result.toString();
    }

        public String readUntilIncluding(char delimiter) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;
            result.append(c);
            if (c == delimiter) {
                break;
            }
        } while (true);
        return result.toString();
    }

        public String readUntilIncluding(String delimiterSequence) throws IOException {
        StringBuilder result = new StringBuilder();

        do {
            int r = read();
            if (r == -1) {
                break;
            }
            char c = (char) r;

            result.append(c);
            if (result.toString().endsWith(delimiterSequence)) {
                break;
            }
        } while (true);
        return result.toString();
    }

        public String readKeywordPart(Delimiter delimiter, ParserContext context) throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if ((delimiter == null || !peek(delimiter.getDelimiter())) && peekKeywordPart(context)) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }

        public void swallowNumeric() throws IOException {
        do {
            if (!peekNumeric()) {
                return;
            }
            swallow();
        } while (true);
    }

        public String readNumeric() throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peekNumeric()) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }

        public String readWhitespace() throws IOException {
        StringBuilder result = new StringBuilder();
        do {
            if (peekWhitespace()) {
                result.append((char) read());
            } else {
                break;
            }
        } while (true);
        return result.toString();
    }
}