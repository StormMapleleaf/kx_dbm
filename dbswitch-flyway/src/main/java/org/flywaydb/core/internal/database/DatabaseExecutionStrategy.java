package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

public interface DatabaseExecutionStrategy {

        <T> T execute(final SqlCallable<T> callable) throws SQLException;
}