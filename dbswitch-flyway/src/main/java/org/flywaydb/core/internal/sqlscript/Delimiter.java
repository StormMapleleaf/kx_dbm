package org.flywaydb.core.internal.sqlscript;

public class Delimiter {
    public static final Delimiter SEMICOLON = new Delimiter(";", false



    );
    public static final Delimiter GO = new Delimiter("GO", true



    );

        private final String delimiter;

        private final boolean aloneOnLine;








        public Delimiter(String delimiter, boolean aloneOnLine



    ) {
        this.delimiter = delimiter;
        this.aloneOnLine = aloneOnLine;



    }

        public String getDelimiter() {
        return delimiter;
    }

        public boolean isAloneOnLine() {
        return aloneOnLine;
    }











    @Override
    public String toString() {
        return (aloneOnLine ? "\n" : "") + delimiter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Delimiter delimiter1 = (Delimiter) o;

        return aloneOnLine == delimiter1.aloneOnLine && delimiter.equals(delimiter1.delimiter);
    }

    @Override
    public int hashCode() {
        int result = delimiter.hashCode();
        result = 31 * result + (aloneOnLine ? 1 : 0);
        return result;
    }
}