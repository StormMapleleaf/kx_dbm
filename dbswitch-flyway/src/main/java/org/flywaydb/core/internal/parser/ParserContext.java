package org.flywaydb.core.internal.parser;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.sqlscript.Delimiter;

import java.security.InvalidParameterException;

public class ParserContext {
    private int parensDepth = 0;
    private int blockDepth = 0;
    private Delimiter delimiter;
    private StatementType statementType;

    public ParserContext(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public void increaseParensDepth() {
        parensDepth++;
    }

    public void decreaseParensDepth() {
        parensDepth--;
    }

    public int getParensDepth() {
        return parensDepth;
    }

    public void increaseBlockDepth() {
        blockDepth++;
    }

    public void decreaseBlockDepth() {
        if (blockDepth == 0) {
            throw new FlywayException("Flyway parsing bug: unable to decrease block depth below 0");
        }
        blockDepth--;
    }

    public int getBlockDepth() {
        return blockDepth;
    }

    public Delimiter getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public void setStatementType(StatementType statementType) {
        if (statementType == null) {
            throw new InvalidParameterException("statementType must be non-null");
        }

        this.statementType = statementType;
    }

    public boolean isLetter(char c) {
        if (Character.isLetter(c)) {
            return true;
        }
        if (getStatementType() != StatementType.UNKNOWN) {
            return statementType.treatAsIfLetter(c);
        }
        return false;
    }
}