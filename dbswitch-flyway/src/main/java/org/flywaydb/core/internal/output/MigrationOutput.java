package org.flywaydb.core.internal.output;

public class MigrationOutput {
    public String category;
    public String version;
    public String description;
    public String type;
    public String installedOn;
    public String state;
    public String undoable;
    public String filepath;
    public String installedBy;
    public int executionTime;

    public MigrationOutput(String category, String version, String description, String type, String installedOn,
                           String state, String undoable, String filepath, String installedBy, int executionTime) {
        this.category = category;
        this.version = version;
        this.description = description;
        this.type = type;
        this.installedOn = installedOn;
        this.state = state;
        this.undoable = undoable;
        this.filepath = filepath;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
    }
}