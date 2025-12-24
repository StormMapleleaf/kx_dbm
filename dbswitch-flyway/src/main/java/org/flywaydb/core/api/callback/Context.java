package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;

import java.sql.Connection;

public interface Context {

    Configuration getConfiguration();


    Connection getConnection();


    MigrationInfo getMigrationInfo();


    Statement getStatement();
}