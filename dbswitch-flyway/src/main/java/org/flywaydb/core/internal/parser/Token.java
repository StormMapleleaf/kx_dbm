package org.flywaydb.core.internal.parser;

public class Token {
    private final TokenType type;
    private final int pos;
    private final int line;
    private final int col;
    private final String text;
    private final String rawText;
    private final int parensDepth;

    public Token(TokenType type, int pos, int line, int col, String text, String rawText, int parensDepth) {
        this.type = type;
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.text = text;
        this.rawText = rawText;
        this.parensDepth = parensDepth;
    }

    public TokenType getType() {
        return type;
    }

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public String getText() {
        return text;
    }

    public String getRawText() {
        return rawText;
    }

    public int getParensDepth() {
        return parensDepth;
    }
}