package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.configuration.Configuration;

import java.sql.Connection;

public interface Context {
        Configuration getConfiguration();
}