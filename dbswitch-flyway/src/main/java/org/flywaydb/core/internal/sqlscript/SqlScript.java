package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.resource.LoadableResource;

import java.util.Collection;

public interface SqlScript extends Comparable<SqlScript> {
        SqlStatementIterator getSqlStatements();

        int getSqlStatementCount();









        LoadableResource getResource();

        boolean executeInTransaction();

        void validate();
}