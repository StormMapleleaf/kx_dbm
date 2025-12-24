package org.flywaydb.core.internal.parser;

public class PositionTracker {
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private int markPos = 0;
    private int markLine = 1;
    private int markCol = 1;

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public void nextPos() {
        pos++;
    }

    public void nextCol() {
        col++;
    }

    public void linefeed() {
        line++;
        col = 1;
    }

    public void carriageReturn() {
        col = 1;
    }

    public void mark() {
        markPos = pos;
        markLine = line;
        markCol = col;
    }

    public void reset() {
        pos = markPos;
        line = markLine;
        col = markCol;
    }
}