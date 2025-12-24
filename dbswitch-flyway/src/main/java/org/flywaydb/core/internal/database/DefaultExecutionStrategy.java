package org.flywaydb.core.internal.database;

import org.flywaydb.core.internal.util.SqlCallable;

import java.sql.SQLException;

public class DefaultExecutionStrategy implements DatabaseExecutionStrategy {

    public <T> T execute(final SqlCallable<T> callable) throws SQLException {
        return callable.call();
    }
}