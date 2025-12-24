package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

public class SchemaHistoryFactory {
    private SchemaHistoryFactory() {
    }

        public static SchemaHistory getSchemaHistory(Configuration configuration,
                                                 SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                                 SqlScriptFactory sqlScriptFactory,
                                                 Database database, Schema schema



    ) {
        Table table = schema.getTable(configuration.getTable());
        JdbcTableSchemaHistory jdbcTableSchemaHistory =
                new JdbcTableSchemaHistory(sqlScriptExecutorFactory, sqlScriptFactory, database, table);









        return jdbcTableSchemaHistory;
    }
}