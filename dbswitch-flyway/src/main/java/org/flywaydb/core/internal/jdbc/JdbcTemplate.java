package org.flywaydb.core.internal.jdbc;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcTemplate {
        private final Connection connection;

        private final int nullType;

        public JdbcTemplate(Connection connection) {
        this(connection, DatabaseType.fromJdbcConnection(connection));
    }

        public JdbcTemplate(Connection connection, DatabaseType databaseType) {
        this.connection = connection;
        this.nullType = databaseType.getNullType();
    }

        public Connection getConnection() {
        return connection;
    }

        public List<Map<String, String>> queryForList(String query, Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Map<String, String>> result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    rowMap.put(resultSet.getMetaData().getColumnLabel(i), resultSet.getString(i));
                }
                result.add(rowMap);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public List<String> queryForStringList(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<String> result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public int queryForInt(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        int result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getInt(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public boolean queryForBoolean(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        boolean result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            resultSet.next();
            result = resultSet.getBoolean(1);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public String queryForString(String query, String... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = prepareStatement(query, params);
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public void execute(String sql, Object... params) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(sql, params);
            statement.execute();
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

        public Results executeStatement(String sql) {
        Results results = new Results();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.setEscapeProcessing(false);
            boolean hasResults;
            try {
                hasResults = statement.execute(sql);
            } finally {
                extractWarnings(results, statement);
            }
            extractResults(results, statement, sql, hasResults);
        } catch (final SQLException e) {
            extractErrors(results, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }
        return results;
    }

    private void extractWarnings(Results results, Statement statement) throws SQLException {
        SQLWarning warning = statement.getWarnings();
        while (warning != null) {
            int code = warning.getErrorCode();
            String state = warning.getSQLState();
            String message = warning.getMessage();

            if (state == null)
            {
                state = "";
            }

            if (message == null)
            {
                message = "";
            }

            results.addWarning(new WarningImpl(code, state, message));
            warning = warning.getNextWarning();
        }
    }

    public void extractErrors(Results results, SQLException e) {







        results.setException(e);
    }

    private void extractResults(Results results, Statement statement, String sql, boolean hasResults) throws SQLException {
        int updateCount = -1;
        while (hasResults || (updateCount = statement.getUpdateCount()) != -1) {
            List<String> columns = null;
            List<List<String>> data = null;
            if (hasResults) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    columns = new ArrayList<>();
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metadata.getColumnName(i));
                    }

                    data = new ArrayList<>();
                    while (resultSet.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.add(resultSet.getString(i));
                        }
                        data.add(row);
                    }
                }
            }
            results.addResult(new Result(updateCount, columns, data, sql));
            hasResults = statement.getMoreResults();
        }
    }

        public void update(String sql, Object... params) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(sql, params);
            statement.executeUpdate();
        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

        private PreparedStatement prepareStatement(String sql, Object[] params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                statement.setNull(i + 1, nullType);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) params[i]);
            } else {
                statement.setString(i + 1, params[i].toString());
            }
        }
        return statement;
    }

        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<T> results;
        try {
            statement = prepareStatement(sql, params);
            resultSet = statement.executeQuery();

            results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return results;
    }






































}