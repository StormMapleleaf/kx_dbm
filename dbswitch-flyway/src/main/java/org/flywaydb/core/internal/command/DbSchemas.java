package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DbSchemas {
    private static final Log LOG = LogFactory.getLog(DbSchemas.class);

        private final Connection connection;

        private final Schema[] schemas;

        private final SchemaHistory schemaHistory;

        private final Database database;

        public DbSchemas(Database database, Schema[] schemas, SchemaHistory schemaHistory) {
        this.database = database;
        this.connection = database.getMainConnection();
        this.schemas = schemas;
        this.schemaHistory = schemaHistory;
    }

        public void create(final boolean baseline) {
        int retries = 0;
        while (true) {
            try {
                ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database).execute(new Callable<Object>() {
                    @Override
                    public Void call() {
                        List<Schema> createdSchemas = new ArrayList<>();
                        for (Schema schema : schemas) {
                            if (!schema.exists()) {
                                if (schema.getName() == null) {
                                    throw new FlywayException("Unable to determine schema for the schema history table." +
                                            " Set a default schema for the connection or specify one using the defaultSchema property!");
                                }
                                LOG.debug("Creating schema: " + schema);
                                schema.create();
                                createdSchemas.add(schema);
                            } else {
                                LOG.debug("Skipping creation of existing schema: " + schema);
                            }
                        }

                        if (!createdSchemas.isEmpty()) {
                            schemaHistory.create(baseline);
                            schemaHistory.addSchemasMarker(createdSchemas.toArray(new Schema[0]));
                        }

                        return null;
                    }
                });
                return;
            } catch (RuntimeException e) {
                if (++retries >= 10) {
                    throw e;
                }
                try {
                    LOG.debug("Schema creation failed. Retrying in 1 sec ...");
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // Ignore
                }
            }
        }
    }
}