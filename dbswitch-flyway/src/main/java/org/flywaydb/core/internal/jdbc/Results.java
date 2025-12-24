package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Warning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Results {
    public static final Results EMPTY_RESULTS = new Results();

    private final List<Result> results = new ArrayList<>();
    private final List<Warning> warnings = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();
    private SQLException exception;

    public void addResult(Result result) {
        results.add(result);
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
    }

    public void addError(Error error) {
        errors.add(error);
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public List<Result> getResults() {
        return results;
    }

    public SQLException getException() {
        return exception;
    }

    public void setException(SQLException exception) {
        this.exception = exception;
    }
}