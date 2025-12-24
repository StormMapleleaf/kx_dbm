package org.flywaydb.core.internal.database.db2;

import org.flywaydb.core.internal.database.base.Function;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.database.base.Type;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DB2Schema extends Schema<DB2Database, DB2Table> {
        DB2Schema(JdbcTemplate jdbcTemplate, DB2Database database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT count(*) from ("
                + "SELECT 1 FROM syscat.schemata WHERE schemaname=?"
                + ")", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from ("
                + "select 1 from syscat.tables where tabschema = ? "
                + "union "
                + "select 1 from syscat.views where viewschema = ? "
                + "union "
                + "select 1 from syscat.sequences where seqschema = ? "
                + "union "
                + "select 1 from syscat.indexes where indschema = ? "
                + "union "
                + "select 1 from syscat.routines where ROUTINESCHEMA = ? "
                + "union "
                + "select 1 from syscat.triggers where trigschema = ? "
                + ")", name, name, name, name, name, name) == 0;
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
            List<String> dropVersioningStatements = generateDropVersioningStatement();
            if (!dropVersioningStatements.isEmpty()) {
                for (String dropTableStatement : generateDropStatements("S", "TABLE")) {
                    jdbcTemplate.execute(dropTableStatement);
                }
            }

            for (String dropVersioningStatement : dropVersioningStatements) {
                jdbcTemplate.execute(dropVersioningStatement);
            }




        for (String dropStatement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("A", "ALIAS")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatements("G", "TABLE")) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String dropStatement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsForProcedures()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsForTriggers()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (String dropStatement : generateDropStatementsForModules()) {
            jdbcTemplate.execute(dropStatement);
        }

        for (Function function : allFunctions()) {
            function.drop();
        }

        for (Type type : allTypes()) {
            type.drop();
        }
    }

        private List<String> generateDropStatementsForProcedures() throws SQLException {
        String dropProcGenQuery =
                "select SPECIFICNAME from SYSCAT.ROUTINES where ROUTINETYPE='P' and ROUTINESCHEMA = '" + name + "'";
        return buildDropStatements("DROP SPECIFIC PROCEDURE", dropProcGenQuery);
    }

        private List<String> generateDropStatementsForTriggers() throws SQLException {
        String dropTrigGenQuery = "select TRIGNAME from SYSCAT.TRIGGERS where TRIGSCHEMA = '" + name + "'";
        return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
    }

        private List<String> generateDropStatementsForSequences() throws SQLException {
        String dropSeqGenQuery = "select SEQNAME from SYSCAT.SEQUENCES where SEQSCHEMA = '" + name
                + "' and SEQTYPE='S'";
        return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
    }

        private List<String> generateDropStatementsForViews() throws SQLException {
        String dropSeqGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='V' AND TABSCHEMA = '" + name + "'" +




                        " and substr(property,19,1) <> 'Y'"



                ;

        return buildDropStatements("DROP VIEW", dropSeqGenQuery);
    }

    private List<String> generateDropStatementsForModules() throws SQLException {
        String dropSeqGenQuery =
                "select MODULENAME from syscat.modules where MODULESCHEMA = '"
                + name
                + "' and OWNERTYPE='U'";


        return buildDropStatements("DROP MODULE", dropSeqGenQuery);
    }

        private List<String> generateDropStatements(String tableType, String objectType) throws SQLException {
        String dropTablesGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='" + tableType + "' and TABSCHEMA = '"
                + name + "'";
        return buildDropStatements("DROP " + objectType, dropTablesGenQuery);
    }

        private List<String> buildDropStatements(final String dropPrefix, final String query) throws SQLException {
        List<String> dropStatements = new ArrayList<>();
        List<String> dbObjects = jdbcTemplate.queryForStringList(query);
        for (String dbObject : dbObjects) {
            dropStatements.add(dropPrefix + " " + database.quote(name, dbObject));
        }
        return dropStatements;
    }

        private List<String> generateDropVersioningStatement() throws SQLException {
        List<String> dropVersioningStatements = new ArrayList<>();
        Table[] versioningTables = findTables("select TABNAME from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", name);
        for (Table table : versioningTables) {
            dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
        }

        return dropVersioningStatements;
    }

    private DB2Table[] findTables(String sqlQuery, String... params) throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(sqlQuery, params);
        DB2Table[] tables = new DB2Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DB2Table(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    protected DB2Table[] doAllTables() throws SQLException {
        return findTables("select TABNAME from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = ?", name);
    }

    @Override
    protected Function[] doAllFunctions() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList(
                "select SPECIFICNAME from SYSCAT.ROUTINES where"
                        + " ROUTINETYPE='F'"
                        + " AND ORIGIN IN ("
                        + "'E', " 
                        + "'M', " 
                        + "'Q', " 
                        + "'U')"  
                        + " and ROUTINESCHEMA = ?", name);

        List<Function> functions = new ArrayList<>();
        for (String functionName : functionNames) {
            functions.add(getFunction(functionName));
        }

        return functions.toArray(new Function[0]);
    }

    @Override
    public Table getTable(String tableName) {
        return new DB2Table(jdbcTemplate, database, this, tableName);
    }

    @Override
    protected Type getType(String typeName) {
        return new DB2Type(jdbcTemplate, database, this, typeName);
    }

    @Override
    public Function getFunction(String functionName, String... args) {
        return new DB2Function(jdbcTemplate, database, this, functionName, args);
    }
}