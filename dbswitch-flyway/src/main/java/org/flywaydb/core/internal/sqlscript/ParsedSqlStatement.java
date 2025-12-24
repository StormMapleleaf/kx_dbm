package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.Results;

public class ParsedSqlStatement implements SqlStatement {
    private final int pos;
    private final int line;
    private final int col;
    private final String sql;

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

        private final Delimiter delimiter;

    private final boolean canExecuteInTransaction;








    public ParsedSqlStatement(int pos, int line, int col, String sql, Delimiter delimiter,
                              boolean canExecuteInTransaction



    ) {
        this.pos = pos;
        this.line = line;
        this.col = col;
        this.sql = sql;
        this.delimiter = delimiter;
        this.canExecuteInTransaction = canExecuteInTransaction;



    }

    @Override
    public final int getLineNumber() {
        return line;
    }

    @Override
    public final String getSql() {
        return sql;
    }

    @Override
    public String getDelimiter() {
        return delimiter.toString();
    }

    @Override
    public boolean canExecuteInTransaction() {
        return canExecuteInTransaction;
    }













    @Override
    public Results execute(JdbcTemplate jdbcTemplate



    ) {
        return jdbcTemplate.executeStatement(sql);
    }
}