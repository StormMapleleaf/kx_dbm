package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.callback.Error;

public class ErrorImpl implements Error {
    private final int code;
    private final String state;
    private final String message;
    private boolean handled;

        public ErrorImpl(int code, String state, String message) {
        this.code = code;
        this.state = state;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    @Override
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}