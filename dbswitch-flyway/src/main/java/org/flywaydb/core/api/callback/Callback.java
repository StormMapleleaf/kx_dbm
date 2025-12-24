package org.flywaydb.core.api.callback;

public interface Callback {
    boolean supports(Event event, Context context);

    boolean canHandleInTransaction(Event event, Context context);

    void handle(Event event, Context context);
}