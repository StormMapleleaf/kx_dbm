package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Table;

import java.sql.SQLException;
import java.util.List;

public class SybaseASESchema extends Schema<SybaseASEDatabase, SybaseASETable> {
    SybaseASESchema(JdbcTemplate jdbcTemplate, SybaseASEDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return true;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) from sysobjects ob where (ob.type='U' or ob.type = 'V' or ob.type = 'P' or ob.type = 'TR') and ob.name != 'sysquerymetrics'") == 0;
    }

    @Override
    protected void doCreate() {
    }

    @Override
    protected void doDrop() throws SQLException {
        doClean();
    }

        @Override
    protected void doClean() throws SQLException {
        dropObjects("V");
        dropObjects("U");
        dropObjects("P");
        dropObjects("TR");
    }

    @Override
    protected SybaseASETable[] doAllTables() throws SQLException {
        List<String> tableNames = retrieveAllTableNames();

        SybaseASETable[] result = new SybaseASETable[tableNames.size()];

        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            result[i] = new SybaseASETable(jdbcTemplate, database, this, tableName);
        }

        return result;
    }

    @Override
    public Table getTable(String tableName) {
        return new SybaseASETable(jdbcTemplate, database, this, tableName);
    }

        private List<String> retrieveAllTableNames() throws SQLException {
        return jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", "U");
    }

    private void dropObjects(String sybaseObjType) throws SQLException {
        List<String> objNames = jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", sybaseObjType);

        for (String name : objNames) {
            String sql;

            if ("U".equals(sybaseObjType)) {
                sql = "drop table ";
            } else if ("V".equals(sybaseObjType)) {
                sql = "drop view ";
            } else if ("P".equals(sybaseObjType)) {
                sql = "drop procedure ";
            } else if ("TR".equals(sybaseObjType)) {
                sql = "drop trigger ";
            } else {
                throw new IllegalArgumentException("Unknown database object type " + sybaseObjType);
            }

            jdbcTemplate.execute(sql + name);
        }
    }
}