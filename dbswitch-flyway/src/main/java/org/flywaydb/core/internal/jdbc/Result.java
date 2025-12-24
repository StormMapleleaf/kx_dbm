package org.flywaydb.core.internal.jdbc;

import java.util.List;

public class Result {
    private final long updateCount;
    private final List<String> columns;
    private final List<List<String>> data;
    private final String sql;

    public Result(long updateCount, List<String> columns, List<List<String>> data, String sql) {
        this.updateCount = updateCount;
        this.columns = columns;
        this.data = data;
        this.sql = sql;
    }

    public long getUpdateCount() {
        return updateCount;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<String>> getData() {
        return data;
    }

    public String getSql() {
        return sql;
    }
}