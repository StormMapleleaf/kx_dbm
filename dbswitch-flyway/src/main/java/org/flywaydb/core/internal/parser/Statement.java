package org.flywaydb.core.internal.parser;

import java.util.List;

class Statement {
    private final int pos;
    private final int line;
    private final int col;
    private final StatementType statementType;
    private final String sql;
    private final List<Token> tokens;

    Statement(int pos, int line, int col, StatementType statementType, String sql, List<Token> tokens) {
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.statementType = statementType;
        this.sql = sql;
        this.tokens = tokens;
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

    public StatementType getStatementType() {
        return statementType;
    }

    public String getSql() {
        return sql;
    }

    public List<Token> getTokens() {
        return tokens;
    }
}