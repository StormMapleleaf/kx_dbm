package org.flywaydb.core.internal.database.sybasease;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

public class SybaseASEConnection extends Connection<SybaseASEDatabase> {
    SybaseASEConnection(SybaseASEDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }


    @Override
    public Schema getSchema(String name) {
        return new SybaseASESchema(jdbcTemplate, database, "dbo");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return "dbo";
    }
}