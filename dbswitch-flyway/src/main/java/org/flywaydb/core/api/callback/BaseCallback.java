package org.flywaydb.core.api.callback;

public abstract class BaseCallback implements Callback {
    @Override
    public boolean supports(Event event, Context context) {
        return true;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }
}