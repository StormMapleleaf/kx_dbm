package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final String WHITESPACE_CHARS = " \t\n\f\r";

        private StringUtils() {
    }

        public static String trimOrPad(String str, int length) {
        return trimOrPad(str, length, ' ');
    }

        public static String trimOrPad(String str, int length, char padChar) {
        StringBuilder result;
        if (str == null) {
            result = new StringBuilder();
        } else {
            result = new StringBuilder(str);
        }

        if (result.length() > length) {
            return result.substring(0, length);
        }

        while (result.length() < length) {
            result.append(padChar);
        }
        return result.toString();
    }

        public static String trimOrLeftPad(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() > length) {
            return str.substring(0, length);
        }
        return leftPad(str, length, padChar);
    }

    public static String leftPad(String original, int length, char padChar) {
        StringBuilder result = new StringBuilder(original);
        while (result.length() < length) {
            result.insert(0, padChar);
        }
        return result.toString();
    }

        public static String collapseWhitespace(String str) {
        StringBuilder result = new StringBuilder();
        char previous = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isCharAnyOf(c, WHITESPACE_CHARS)) {
                if (previous != ' ') {
                    result.append(' ');
                }
                previous = ' ';
            } else {
                result.append(c);
                previous = c;
            }
        }
        return result.toString();
    }

        public static String left(String str, int count) {
        if (str == null) {
            return null;
        }

        if (str.length() < count) {
            return str;
        }

        return str.substring(0, count);
    }

        public static String replaceAll(String str, String originalToken, String replacementToken) {
        return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
    }

        public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }

        public static String arrayToCommaDelimitedString(Object[] strings) {
        return arrayToDelimitedString(",", strings);
    }

        public static String arrayToDelimitedString(String delimiter, Object[] strings) {
        if (strings == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(strings[i]);
        }
        return builder.toString();
    }

        public static boolean hasText(String s) {
        return (s != null) && (s.trim().length() > 0);
    }

        public static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        Collection<String> tokens = tokenizeToStringCollection(str, delimiters);
        return tokens.toArray(new String[0]);
    }

        public static List<String> tokenizeToStringCollection(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        List<String> tokens = new ArrayList<>(str.length() / 5);
        char[] delimiterChars = delimiters.toCharArray();
        int start = 0;
        int end = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            boolean delimiter = false;
            for (char d : delimiterChars) {
                if (c == d) {
                    tokens.add(str.substring(start, end));
                    start = i + 1;
                    end = start;
                    delimiter = true;
                    break;
                }
            }
            if (!delimiter) {
                if (i == start && c == ' ') {
                    start++;
                    end++;
                }
                if (i >= start && c != ' ') {
                    end = i + 1;
                }
            }
        }
        if (start < end) {
            tokens.add(str.substring(start, end));
        }
        return tokens;
    }

        public static List<String> tokenizeToStringCollection(String str, char delimiterChar, char groupDelimiterChar) {
        if (str == null) {
            return null;
        }
        List<String> tokens = new ArrayList<>(str.length() / 5);
        int start = 0;
        int end = 0;
        boolean inGroup = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == groupDelimiterChar) {
                inGroup = !inGroup;
                addToken(tokens, str, start, end);
                start = i + 1;
                end = start;
            } else if (!inGroup && c == delimiterChar) {
                addToken(tokens, str, start, end);
                start = i + 1;
                end = start;
            } else if (i == start && c == ' ') {
                start++;
                end++;
            } else if (i >= start && c != ' ') {
                end = i + 1;
            }
        }
        addToken(tokens, str, start, end);
        return tokens;
    }

    private static void addToken(List<String> tokens, String str, int start, int end) {
        if (start < end) {
            tokens.add(str.substring(start, end));
        }
    }

        public static int countOccurrencesOf(String str, String token) {
        if (str == null || token == null || str.length() == 0 || token.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(token, pos)) != -1) {
            ++count;
            pos = idx + token.length();
        }
        return count;
    }

        public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; 
        int index = inString.indexOf(oldPattern);
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString, pos, index);
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        return sb.toString();
    }

        public static String collectionToCommaDelimitedString(Collection<?> collection) {
        return collectionToDelimitedString(collection, ", ");
    }

        public static String collectionToDelimitedString(Collection<?> collection, String delimiter) {
        if (collection == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

        public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(0))) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

        public static String trimLeadingCharacter(String str, char character) {
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && character == buf.charAt(0)) {
            buf.deleteCharAt(0);
        }
        return buf.toString();
    }

        public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

        public static boolean startsAndEndsWith(String str, String prefix, String... suffixes) {
        if (StringUtils.hasLength(prefix) && !str.startsWith(prefix)) {
            return false;
        }
        for (String suffix : suffixes) {
            if (str.endsWith(suffix) && (str.length() > (prefix + suffix).length())) {
                return true;
            }
        }
        return false;
    }

        public static String trimLineBreak(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder buf = new StringBuilder(str);
        while (buf.length() > 0 && isLineBreakCharacter(buf.charAt(buf.length() - 1))) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

        private static boolean isLineBreakCharacter(char ch) {
        return '\n' == ch || '\r' == ch;
    }

        public static String wrap(String str, int lineSize) {
        if (str.length() < lineSize) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int oldPos = 0;
        for (int pos = lineSize; pos < str.length(); pos += lineSize) {
            result.append(str, oldPos, pos).append("\n");
            oldPos = pos;
        }
        result.append(str.substring(oldPos));
        return result.toString();
    }

        public static String wordWrap(String str, int lineSize) {
        if (str.length() < lineSize) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int oldPos = 0;
        int pos = lineSize;
        while (pos < str.length()) {
            if (Character.isWhitespace(str.charAt(pos))) {
                pos++;
                continue;
            }

            String part = str.substring(oldPos, pos);
            int spacePos = part.lastIndexOf(' ');
            if (spacePos > 0) {
                pos = oldPos + spacePos + 1;
            }

            result.append(str.substring(oldPos, pos).trim()).append("\n");
            oldPos = pos;
            pos += lineSize;
        }
        result.append(str.substring(oldPos));
        return result.toString();
    }

        public static boolean isCharAnyOf(char c, String chars) {
        for (int i = 0; i < chars.length(); i++) {
            if (chars.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }
}