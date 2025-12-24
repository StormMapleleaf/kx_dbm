package org.flywaydb.core.internal.output;

import java.util.List;

public class InfoOutput {
    public String flywayVersion;
    public String database;
    public String schemaVersion;
    public String schemaName;
    public List<MigrationOutput> migrations;

    public InfoOutput(String flywayVersion,
                      String database,
                      String schemaVersion,
                      String schemaName,
                      List<MigrationOutput> migrations) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaVersion = schemaVersion;
        this.schemaName = schemaName;
        this.migrations = migrations;
    }
}