package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

public class DbBaseline {
    private static final Log LOG = LogFactory.getLog(DbBaseline.class);

        private final SchemaHistory schemaHistory;

        private final MigrationVersion baselineVersion;

        private final String baselineDescription;

        private final CallbackExecutor callbackExecutor;

        public DbBaseline(SchemaHistory schemaHistory, MigrationVersion baselineVersion,
                      String baselineDescription, CallbackExecutor callbackExecutor) {
        this.schemaHistory = schemaHistory;
        this.baselineVersion = baselineVersion;
        this.baselineDescription = baselineDescription;
        this.callbackExecutor = callbackExecutor;
    }

        public void baseline() {
        callbackExecutor.onEvent(Event.BEFORE_BASELINE);

        try {
            if (!schemaHistory.exists()) {
                schemaHistory.create(true);
                LOG.info("Successfully baselined schema with version: " + baselineVersion);
            } else {
                AppliedMigration baselineMarker = schemaHistory.getBaselineMarker();
                if (baselineMarker != null) {
                    if (baselineVersion.equals(baselineMarker.getVersion())
                            && baselineDescription.equals(baselineMarker.getDescription())) {
                        LOG.info("Schema history table " + schemaHistory + " already initialized with ("
                                + baselineVersion + "," + baselineDescription + "). Skipping.");
                    } else {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with ("
                                + baselineVersion + "," + baselineDescription
                                + ") as it has already been baselined with ("
                                + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")");
                    }
                } else {
                    if (schemaHistory.hasSchemasMarker() && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with version 0 as this version was used for schema creation");
                    }

                    if (schemaHistory.hasNonSyntheticAppliedMigrations()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations");
                    }

                    if (schemaHistory.allAppliedMigrations().isEmpty()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already exists, and is empty.\n" +
                                "Delete the schema history table with the clean command, and run baseline again.");
                    }

                    throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations.\n" +
                            "Delete the schema history table with the clean command, and run baseline again.");
                }
            }
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_BASELINE_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_BASELINE);
    }
}