package org.flywaydb.core.api;

public enum MigrationType {
        SCHEMA(true, false),

        BASELINE(true, false),

        SQL(false, false),

        UNDO_SQL(false, true),

        JDBC(false, false),

        UNDO_JDBC(false, true),

        @Deprecated
    SPRING_JDBC(false, false),

        @Deprecated
    UNDO_SPRING_JDBC(false, true),

        CUSTOM(false, false),

        UNDO_CUSTOM(false, true);

    private final boolean synthetic;
    private final boolean undo;

    MigrationType(boolean synthetic, boolean undo) {
        this.synthetic = synthetic;
        this.undo = undo;
    }

        public boolean isSynthetic() {
        return synthetic;
    }

        public boolean isUndo() {
        return undo;
    }
}