package org.flywaydb.core.internal.sqlscript;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.resource.Resource;

import java.sql.SQLException;

public class FlywaySqlScriptException extends FlywaySqlException {
    private final Resource resource;
    private final SqlStatement statement;

        public FlywaySqlScriptException(Resource resource, SqlStatement statement, SQLException sqlException) {
        super(resource == null ? "Script failed" : "Migration " + resource.getFilename() + " failed", sqlException);
        this.resource = resource;
        this.statement = statement;
    }

        public Resource getResource() {
        return resource;
    }

        public int getLineNumber() {
        return statement == null ? -1 : statement.getLineNumber();
    }

        public String getStatement() {
        return statement == null ? "" : statement.getSql();
    }

        public SqlStatement getSqlStatement() {
        return statement;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (resource != null) {
            message += "Location   : " + resource.getAbsolutePath() + " (" + resource.getAbsolutePathOnDisk() + ")\n";
        }
        if (statement != null) {
            message += "Line       : " + getLineNumber() + "\n";
            message += "Statement  : " + getStatement() + "\n";
        }
        return message;
    }
}