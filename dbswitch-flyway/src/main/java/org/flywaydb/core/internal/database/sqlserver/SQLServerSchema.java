package org.flywaydb.core.internal.database.sqlserver;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLServerSchema extends Schema<SQLServerDatabase, SQLServerTable> {
    private static final Log LOG = LogFactory.getLog(SQLServerSchema.class);

    private final String databaseName;

        private enum ObjectType {
                AGGREGATE("AF"),
                CHECK_CONSTRAINT("C"),
                DEFAULT_CONSTRAINT("D"),
                FOREIGN_KEY("F"),
                INLINED_TABLE_FUNCTION("IF"),
                SCALAR_FUNCTION("FN"),
                CLR_SCALAR_FUNCTION("FS"),
                CLR_TABLE_VALUED_FUNCTION("FT"),
                STORED_PROCEDURE("P"),
                CLR_STORED_PROCEDURE("PC"),
                RULE("R"),
                SYNONYM("SN"),
                TABLE_VALUED_FUNCTION("TF"),
                ASSEMBLY_DML_TRIGGER("TA"),
                SQL_DML_TRIGGER("TR"),
                UNIQUE_CONSTRAINT("UQ"),
                USER_TABLE("U"),
                VIEW("V"),
                SEQUENCE_OBJECT("SO");

        final String code;

        ObjectType(String code) {
            assert code != null;
            this.code = code;
        }
    }

        private class DBObject {
                final String name;
                final long objectId;

        DBObject(long objectId, String name) {
            assert name != null;
            this.objectId = objectId;
            this.name = name;
        }
    }

        SQLServerSchema(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, String name) {
        super(jdbcTemplate, database, name);
        this.databaseName = databaseName;
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        boolean empty = queryDBObjects(ObjectType.SCALAR_FUNCTION, ObjectType.AGGREGATE,
                ObjectType.CLR_SCALAR_FUNCTION, ObjectType.CLR_TABLE_VALUED_FUNCTION, ObjectType.TABLE_VALUED_FUNCTION,
                ObjectType.STORED_PROCEDURE, ObjectType.CLR_STORED_PROCEDURE, ObjectType.USER_TABLE,
                ObjectType.SYNONYM, ObjectType.SEQUENCE_OBJECT, ObjectType.FOREIGN_KEY, ObjectType.VIEW).isEmpty();
        if (empty) {
            int objectCount = jdbcTemplate.queryForInt("SELECT count(*) FROM " +
                    "( " +
                    "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                    " WHERE t.is_user_defined = 1 AND s.name = ? " +
                    "Union " +
                    "SELECT name FROM sys.assemblies WHERE is_user_defined=1" +
                    ") R", name);
            empty = objectCount == 0;
        }

        return empty;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        List<DBObject> tables = queryDBObjects(ObjectType.USER_TABLE);

        for (String statement : cleanTriggers()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanForeignKeys(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanDefaultConstraints(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanUniqueConstraints(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanIndexes(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanComputedColumns(tables)) {
            try {
                jdbcTemplate.execute(statement);
            } catch (SQLException e) {
                LOG.debug("Ignoring dependency-related error: " + e.getMessage());
            }
        }
        for (String statement : cleanObjects("FUNCTION",
                ObjectType.SCALAR_FUNCTION,
                ObjectType.CLR_SCALAR_FUNCTION,
                ObjectType.CLR_TABLE_VALUED_FUNCTION,
                ObjectType.TABLE_VALUED_FUNCTION,
                ObjectType.INLINED_TABLE_FUNCTION)) {
            try {
                jdbcTemplate.execute(statement);
            } catch (SQLException e) {
                LOG.debug("Ignoring dependency-related error: " + e.getMessage());
            }
        }

        for (String statement : cleanComputedColumns(tables)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("PROCEDURE",
                ObjectType.STORED_PROCEDURE,
                ObjectType.CLR_STORED_PROCEDURE)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("VIEW", ObjectType.VIEW)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("FUNCTION",
                ObjectType.SCALAR_FUNCTION,
                ObjectType.CLR_SCALAR_FUNCTION,
                ObjectType.CLR_TABLE_VALUED_FUNCTION,
                ObjectType.TABLE_VALUED_FUNCTION,
                ObjectType.INLINED_TABLE_FUNCTION)) {
            jdbcTemplate.execute(statement);
        }

        SQLServerTable[] allTables = allTables();
        for (SQLServerTable table : allTables) {
            table.dropSystemVersioningIfPresent();
        }
        for (SQLServerTable table : allTables) {
            table.drop();
        }

        for (String statement : cleanObjects("AGGREGATE", ObjectType.AGGREGATE)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanTypes()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanAssemblies()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("SYNONYM", ObjectType.SYNONYM)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("RULE", ObjectType.RULE)) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanObjects("DEFAULT", ObjectType.DEFAULT_CONSTRAINT)) {
            jdbcTemplate.execute(statement);
        }




            for (String statement : cleanObjects("SEQUENCE", ObjectType.SEQUENCE_OBJECT)) {
                jdbcTemplate.execute(statement);
            }



    }

        private List<DBObject> queryDBObjects(ObjectType... types) throws SQLException {
        return queryDBObjectsWithParent(null, types);
    }

        private List<DBObject> queryDBObjectsWithParent(DBObject parent, ObjectType... types) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT obj.object_id, obj.name FROM sys.objects AS obj " +
                "LEFT JOIN sys.extended_properties AS eps " +
                "ON obj.object_id = eps.major_id " +
                "AND eps.class = 1 " +    
                "AND eps.minor_id = 0 " + 
                "AND eps.name='microsoft_database_tools_support' " + 
                "WHERE SCHEMA_NAME(obj.schema_id) = '" + name + "'  " +
                "AND eps.major_id IS NULL " + 
                "AND obj.is_ms_shipped = 0 " + 
                "AND obj.type IN (" 
        );

        boolean first = true;
        for (ObjectType type : types) {
            if (!first) {
                query.append(", ");
            }
            query.append("'").append(type.code).append("'");
            first = false;
        }
        query.append(")");

        if (parent != null) {
            query.append(" AND obj.parent_object_id = ").append(parent.objectId);
        }

        query.append(" order by create_date desc"




        );

        return jdbcTemplate.query(query.toString(), new RowMapper<DBObject>() {
            @Override
            public DBObject mapRow(ResultSet rs) throws SQLException {
                return new DBObject(rs.getLong("object_id"), rs.getString("name"));
            }
        });
    }

        private List<String> cleanForeignKeys(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            List<DBObject> fks = queryDBObjectsWithParent(table, ObjectType.FOREIGN_KEY,
                    ObjectType.CHECK_CONSTRAINT);
            for (DBObject fk : fks) {
                statements.add("ALTER TABLE " + database.quote(name, table.name) + " DROP CONSTRAINT " +
                        database.quote(fk.name));
            }
        }
        return statements;
    }

        private List<String> cleanComputedColumns(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> columns = jdbcTemplate.queryForStringList(
                    "SELECT name FROM sys.computed_columns WHERE object_id=OBJECT_ID(N'" + tableName + "')");
            for (String column : columns) {
                statements.add("ALTER TABLE " + tableName + " DROP COLUMN " + database.quote(column));
            }
        }
        return statements;
    }

        private List<String> cleanIndexes(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> indexes = jdbcTemplate.queryForStringList(
                    "SELECT name FROM sys.indexes" +
                            " WHERE object_id=OBJECT_ID(N'" + tableName + "')" +
                            " AND is_primary_key = 0" +
                            " AND is_unique_constraint = 0" +
                            " AND name IS NOT NULL");
            for (String index : indexes) {
                statements.add("DROP INDEX " + database.quote(index) + " ON " + tableName);
            }
        }
        return statements;
    }

        private List<String> cleanDefaultConstraints(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            String tableName = database.quote(name, table.name);
            List<String> indexes = jdbcTemplate.queryForStringList(
                    "SELECT i.name FROM sys.indexes i" +
                            " JOIN sys.index_columns ic on i.index_id = ic.index_id" +
                            " JOIN sys.columns c ON ic.column_id = c.column_id AND i.object_id = c.object_id" +
                            " WHERE i.object_id=OBJECT_ID(N'" + tableName + "')" +
                            " AND is_primary_key = 0" +
                            " AND is_unique_constraint = 1" +
                            " AND i.name IS NOT NULL" +
                            " GROUP BY i.name" +
                            " HAVING MAX(CAST(is_rowguidcol AS INT)) = 0 OR MAX(CAST(is_filestream AS INT)) = 0");
            for (String index : indexes) {
                statements.add("ALTER TABLE " + tableName + " DROP CONSTRAINT " + database.quote(index));
            }
        }
        return statements;
    }

        private List<String> cleanUniqueConstraints(List<DBObject> tables) throws SQLException {
        List<String> statements = new ArrayList<>();
        for (DBObject table : tables) {
            List<DBObject> dfs = queryDBObjectsWithParent(table, ObjectType.DEFAULT_CONSTRAINT);
            for (DBObject df : dfs) {
                statements.add("ALTER TABLE " + database.quote(name, table.name) + " DROP CONSTRAINT " + database.quote(df.name));
            }

        }
        return statements;
    }

        private List<String> cleanTypes() throws SQLException {
        List<String> typeNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                                " WHERE t.is_user_defined = 1 AND s.name = ?",
                        name
                );

        List<String> statements = new ArrayList<>();
        for (String typeName : typeNames) {
            statements.add("DROP TYPE " + database.quote(name, typeName));
        }
        return statements;
    }

        private List<String> cleanAssemblies() throws SQLException {
        List<String> assemblyNames =
                jdbcTemplate.queryForStringList("SELECT * FROM sys.assemblies WHERE is_user_defined=1");
        List<String> statements = new ArrayList<>();
        for (String assemblyName : assemblyNames) {
            statements.add("DROP ASSEMBLY " + database.quote(assemblyName));
        }
        return statements;
    }

        private List<String> cleanTriggers() throws SQLException {
        List<String> triggerNames =
                jdbcTemplate.queryForStringList("SELECT * FROM sys.triggers" +
                        " WHERE is_ms_shipped=0 AND parent_id=0 AND parent_class_desc='DATABASE'");
        List<String> statements = new ArrayList<>();
        for (String triggerName : triggerNames) {
            statements.add("DROP TRIGGER " + database.quote(triggerName) + " ON DATABASE");
        }
        return statements;
    }

        private List<String> cleanObjects(String dropQualifier, ObjectType... objectTypes) throws SQLException {
        List<String> statements = new ArrayList<>();
        List<DBObject> dbObjects = queryDBObjects(objectTypes);
        for (DBObject dbObject : dbObjects) {
            statements.add("DROP " + dropQualifier + " " + database.quote(name, dbObject.name));
        }

        return statements;
    }

    @Override
    protected SQLServerTable[] doAllTables() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        for (DBObject table : queryDBObjects(ObjectType.USER_TABLE)) {
            tableNames.add(table.name);
        }

        SQLServerTable[] tables = new SQLServerTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLServerTable(jdbcTemplate, database, databaseName, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SQLServerTable(jdbcTemplate, database, databaseName, this, tableName);
    }
}