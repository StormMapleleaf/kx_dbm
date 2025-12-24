package org.flywaydb.core.internal.logging.android;

import org.flywaydb.core.api.logging.Log;

public class AndroidLog implements Log {
        private static final String TAG = "Flyway";

    @Override
    public boolean isDebugEnabled() {
        return android.util.Log.isLoggable(TAG, android.util.Log.DEBUG);
    }

    @Override
    public void debug(String message) {
        android.util.Log.d(TAG, message);
    }

    @Override
    public void info(String message) {
        android.util.Log.i(TAG, message);
    }

    @Override
    public void warn(String message) {
        android.util.Log.w(TAG, message);
    }

    @Override
    public void error(String message) {
        android.util.Log.e(TAG, message);
    }

    @Override
    public void error(String message, Exception e) {
        android.util.Log.e(TAG, message, e);
    }
}