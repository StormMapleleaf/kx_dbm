package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SnowflakeSchema extends Schema<SnowflakeDatabase, SnowflakeTable> {
        SnowflakeSchema(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        String sql = "SHOW SCHEMAS LIKE '" + name + "'";
        List<Boolean> results = jdbcTemplate.query(sql, new RowMapper<Boolean>() {
            @Override
            public Boolean mapRow(ResultSet rs) throws SQLException {
                return true;
            }
        });
        return !results.isEmpty();
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = getObjectCount("TABLE") + getObjectCount("VIEW")
                + getObjectCount("SEQUENCE");

        return objectCount == 0;
    }

    private int getObjectCount(String objectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + database.quote(name), new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs) throws SQLException {
                return 1;
            }
        }).size();
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String dropStatement : generateDropStatements("VIEW")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("SEQUENCE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsWithArgs("USER FUNCTIONS", "FUNCTION")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsWithArgs("PROCEDURES", "PROCEDURE")) {
            jdbcTemplate.execute(dropStatement);
        }
    }

    @Override
    protected SnowflakeTable[] doAllTables() throws SQLException {
        List<SnowflakeTable> tables = jdbcTemplate.query("SHOW TABLES IN SCHEMA " + database.quote(name), new RowMapper<SnowflakeTable>() {
            @Override
            public SnowflakeTable mapRow(ResultSet rs) throws SQLException {
                String tableName = rs.getString("name");
                return (SnowflakeTable)getTable(tableName);
            }
        });
        return tables.toArray(new SnowflakeTable[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new SnowflakeTable(jdbcTemplate, database, this, tableName);
    }


    private List<String> generateDropStatements(final String objectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + objectType + "S IN SCHEMA " + database.quote(name), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs) throws SQLException {
                String tableName = rs.getString("name");
                return "DROP " + objectType + " " + database.quote(name) + "." + database.quote(tableName);
            }
        });
    }

    private List<String> generateDropStatementsWithArgs(final String showObjectType, final String dropObjectType) throws SQLException {
        return jdbcTemplate.query("SHOW " + showObjectType + " IN SCHEMA " + database.quote(name), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs) throws SQLException {
                String nameAndArgsList = rs.getString("arguments");
                int indexOfEndOfArgs = nameAndArgsList.indexOf(") RETURN ");
                String functionName = nameAndArgsList.substring(0, indexOfEndOfArgs + 1);
                return "DROP " + dropObjectType + " " + name + "." + functionName;
            }
        });
    }
}