package org.flywaydb.core.internal.sqlscript;

import java.sql.Connection;

public interface SqlScriptExecutorFactory {
        SqlScriptExecutor createSqlScriptExecutor(Connection connection



    );
}