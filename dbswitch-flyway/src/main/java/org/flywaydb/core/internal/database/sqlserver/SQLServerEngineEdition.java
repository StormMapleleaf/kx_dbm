package org.flywaydb.core.internal.database.sqlserver;

public enum SQLServerEngineEdition {
    
    PERSONAL_DESKTOP(1),
    STANDARD(2),
    ENTERPRISE(3),
    EXPRESS(4),
    SQL_DATABASE(5),
    SQL_DATA_WAREHOUSE(6),
    MANAGED_INSTANCE(8);

    private final int code;

    SQLServerEngineEdition(int code) {
        this.code = code;
    }

    public static SQLServerEngineEdition fromCode(int code) {
        for (SQLServerEngineEdition edition : values()) {
            if (edition.code == code) {
                return edition;
            }
        }
        throw new IllegalArgumentException("Unknown SQL Server engine edition: " + code);
    }
}