
package org.flywaydb.core.api.android;

import android.content.Context;


public class ContextHolder {
    private ContextHolder() {}


    private static Context context;

    public static Context getContext() {
        return context;
    }


    public static void setContext(Context context) {
        ContextHolder.context = context;
    }
}