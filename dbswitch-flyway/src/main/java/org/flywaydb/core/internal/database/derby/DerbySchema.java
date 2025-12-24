package org.flywaydb.core.internal.database.derby;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DerbySchema extends Schema<DerbyDatabase, DerbyTable> {
        public DerbySchema(JdbcTemplate jdbcTemplate, DerbyDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT (*) FROM sys.sysschemas WHERE schemaname=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " RESTRICT");
    }

    @Override
    protected void doClean() throws SQLException {
        List<String> triggerNames = listObjectNames("TRIGGER", "");
        for (String statement : generateDropStatements("TRIGGER", triggerNames, "")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForConstraints()) {
            jdbcTemplate.execute(statement);
        }

        List<String> viewNames = listObjectNames("TABLE", "TABLETYPE='V'");
        for (String statement : generateDropStatements("VIEW", viewNames, "")) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        List<String> sequenceNames = listObjectNames("SEQUENCE", "");
        for (String statement : generateDropStatements("SEQUENCE", sequenceNames, "RESTRICT")) {
            jdbcTemplate.execute(statement);
        }
    }

        private List<String> generateDropStatementsForConstraints() throws SQLException {
        List<Map<String, String>> results = jdbcTemplate.queryForList("SELECT c.constraintname, t.tablename FROM sys.sysconstraints c" +
                " INNER JOIN sys.systables t ON c.tableid = t.tableid" +
                " INNER JOIN sys.sysschemas s ON c.schemaid = s.schemaid" +
                " WHERE c.type = 'F' AND s.schemaname = ?", name);

        List<String> statements = new ArrayList<>();
        for (Map<String, String> result : results) {
            String dropStatement = "ALTER TABLE " + database.quote(name, result.get("TABLENAME"))
                    + " DROP CONSTRAINT " + database.quote(result.get("CONSTRAINTNAME"));

            statements.add(dropStatement);
        }
        return statements;
    }

        private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix) {
        List<String> statements = new ArrayList<>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + " " + database.quote(name, objectName) + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    @Override
    protected DerbyTable[] doAllTables() throws SQLException {
        List<String> tableNames = listObjectNames("TABLE", "TABLETYPE='T'");

        DerbyTable[] tables = new DerbyTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DerbyTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

        private List<String> listObjectNames(String objectType, String querySuffix) throws SQLException {
        String query = "SELECT " + objectType + "name FROM sys.sys" + objectType + "s WHERE schemaid in (SELECT schemaid FROM sys.sysschemas where schemaname = ?)";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, name);
    }

    @Override
    public Table getTable(String tableName) {
        return new DerbyTable(jdbcTemplate, database, this, tableName);
    }
}