package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.flywaydb.core.internal.database.oracle.OracleSchema.ObjectType.*;

public class OracleSchema extends Schema<OracleDatabase, OracleTable> {
    private static final Log LOG = LogFactory.getLog(OracleSchema.class);

        OracleSchema(JdbcTemplate jdbcTemplate, OracleDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

        public boolean isSystem() throws SQLException {
        return database.getSystemSchemas().contains(name);
    }

        boolean isDefaultSchemaForUser() throws SQLException {
        return name.equals(database.doGetCurrentUser());
    }

    @Override
    protected boolean doExists() throws SQLException {
        return database.queryReturnsRows("SELECT * FROM ALL_USERS WHERE USERNAME = ?", name);
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return !supportedTypesExist(jdbcTemplate, database, this);
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE USER " + database.quote(name) + " IDENTIFIED BY "
                + database.quote("FFllyywwaayy00!!"));
        jdbcTemplate.execute("GRANT RESOURCE TO " + database.quote(name));
        jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP USER " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        if (isSystem()) {
            throw new FlywayException("Clean not supported on Oracle for system schema " + database.quote(name) + "! " +
                    "It must not be changed in any way except by running an Oracle-supplied script!");
        }

        if (database.isFlashbackDataArchiveAvailable()) {
            disableFlashbackArchiveForFbaTrackedTables();
        }

        if (database.isLocatorAvailable()) {
            cleanLocatorMetadata();
        }

        Set<String> objectTypeNames = getObjectTypeNames(jdbcTemplate, database, this);

        List<ObjectType> objectTypesToClean = Arrays.asList(
                TRIGGER,
                QUEUE_TABLE,
                FILE_WATCHER,
                SCHEDULER_CHAIN,
                SCHEDULER_JOB,
                SCHEDULER_PROGRAM,
                SCHEDULE,
                RULE_SET,
                RULE,
                EVALUATION_CONTEXT,
                FILE_GROUP,
                XML_SCHEMA,
                MINING_MODEL,
                REWRITE_EQUIVALENCE,
                SQL_TRANSLATION_PROFILE,
                MATERIALIZED_VIEW,
                MATERIALIZED_VIEW_LOG,
                DIMENSION,
                VIEW,
                DOMAIN_INDEX,
                DOMAIN_INDEX_TYPE,
                TABLE,
                INDEX,
                CLUSTER,
                SEQUENCE,
                OPERATOR,
                FUNCTION,
                PROCEDURE,
                PACKAGE,
                CONTEXT,
                LIBRARY,
                TYPE,
                SYNONYM,
                JAVA_SOURCE,
                JAVA_CLASS,
                JAVA_RESOURCE,

                DATABASE_LINK,
                CREDENTIAL,

                DATABASE_DESTINATION,
                SCHEDULER_GROUP,
                CUBE,
                CUBE_DIMENSION,
                CUBE_BUILD_PROCESS,
                MEASURE_FOLDER,

                ASSEMBLY,
                JAVA_DATA
        );

        for (ObjectType objectType : objectTypesToClean) {
            if (objectTypeNames.contains(objectType.getName())) {
                LOG.debug("Cleaning objects of type " + objectType + " ...");
                objectType.dropObjects(jdbcTemplate, database, this);
            }
        }

        if (isDefaultSchemaForUser()) {
            jdbcTemplate.execute("PURGE RECYCLEBIN");
        }
    }

        private void disableFlashbackArchiveForFbaTrackedTables() throws SQLException {
        boolean dbaViewAccessible = database.isPrivOrRoleGranted("SELECT ANY DICTIONARY")
                || database.isDataDictViewAccessible("DBA_FLASHBACK_ARCHIVE_TABLES");

        if (!dbaViewAccessible && !isDefaultSchemaForUser()) {
            LOG.warn("Unable to check and disable Flashback Archive for tables in schema " + database.quote(name) +
                    " by user \"" + database.doGetCurrentUser() + "\": DBA_FLASHBACK_ARCHIVE_TABLES is not accessible");
            return;
        }

        boolean oracle18orNewer = database.getVersion().isAtLeast("18");

        String queryForFbaTrackedTables = "SELECT TABLE_NAME FROM " + (dbaViewAccessible ? "DBA_" : "USER_")
                + "FLASHBACK_ARCHIVE_TABLES WHERE OWNER_NAME = ?"
                + (oracle18orNewer ? " AND STATUS='ENABLED'" : "");
        List<String> tableNames = jdbcTemplate.queryForStringList(queryForFbaTrackedTables, name);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE " + database.quote(name, tableName) + " NO FLASHBACK ARCHIVE");
            while (database.queryReturnsRows(queryForFbaTrackedTables + " AND TABLE_NAME = ?", name, tableName)) {
                try {
                    LOG.debug("Actively waiting for Flashback cleanup on table: " + database.quote(name, tableName));
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
                }
            }
        }

        if (oracle18orNewer) {
            while (database.queryReturnsRows("SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ?\n"
                    + " AND TABLE_NAME LIKE 'SYS_FBA_DDL_COLMAP_%'", name)) {
                try {
                    LOG.debug("Actively waiting for Flashback colmap cleanup");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new FlywayException("Waiting for Flashback colmap cleanup interrupted", e);
                }
            }
        }
    }

        private boolean locatorMetadataExists() throws SQLException {
        return database.queryReturnsRows("SELECT * FROM ALL_SDO_GEOM_METADATA WHERE OWNER = ?", name);
    }

        private void cleanLocatorMetadata() throws SQLException {
        if (!locatorMetadataExists()) {
            return;
        }

        if (!isDefaultSchemaForUser()) {
            LOG.warn("Unable to clean Oracle Locator metadata for schema " + database.quote(name) +
                    " by user \"" + database.doGetCurrentUser() + "\": unsupported operation");
            return;
        }

        jdbcTemplate.getConnection().commit();
        jdbcTemplate.execute("DELETE FROM USER_SDO_GEOM_METADATA");
        jdbcTemplate.getConnection().commit();
    }

    @Override
    protected OracleTable[] doAllTables() throws SQLException {
        List<String> tableNames = TABLE.getObjectNames(jdbcTemplate, database, this);

        OracleTable[] tables = new OracleTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new OracleTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new OracleTable(jdbcTemplate, database, this, tableName);
    }


        public enum ObjectType {
        TABLE("TABLE", "CASCADE CONSTRAINTS PURGE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                boolean referencePartitionedTablesExist = database.queryReturnsRows(
                        "SELECT * FROM ALL_PART_TABLES WHERE OWNER = ? AND PARTITIONING_TYPE = 'REFERENCE'",
                        schema.getName());
                boolean xmlDbAvailable = database.isXmlDbAvailable();

                StringBuilder tablesQuery = new StringBuilder();
                tablesQuery.append("WITH TABLES AS (\n" +
                        "  SELECT TABLE_NAME, OWNER\n" +
                        "  FROM ALL_TABLES\n" +
                        "  WHERE OWNER = ?\n" +
                        "    AND (IOT_TYPE IS NULL OR IOT_TYPE NOT LIKE '%OVERFLOW%')\n" +
                        "    AND NESTED != 'YES'\n" +
                        "    AND SECONDARY != 'Y'\n");

                if (xmlDbAvailable) {
                    tablesQuery.append("  UNION ALL\n" +
                            "  SELECT TABLE_NAME, OWNER\n" +
                            "  FROM ALL_XML_TABLES\n" +
                            "  WHERE OWNER = ?\n" +
                            "    AND TABLE_NAME NOT LIKE 'BIN$________________________$_'\n");
                }

                tablesQuery.append(")\n" +
                        "SELECT t.TABLE_NAME\n" +
                        "FROM TABLES t\n");

                if (referencePartitionedTablesExist) {
                    tablesQuery.append("  LEFT JOIN ALL_PART_TABLES pt\n" +
                            "    ON t.OWNER = pt.OWNER\n" +
                            "   AND t.TABLE_NAME = pt.TABLE_NAME\n" +
                            "   AND pt.PARTITIONING_TYPE = 'REFERENCE'\n" +
                            "  LEFT JOIN ALL_CONSTRAINTS fk\n" +
                            "    ON pt.OWNER = fk.OWNER\n" +
                            "   AND pt.TABLE_NAME = fk.TABLE_NAME\n" +
                            "   AND pt.REF_PTN_CONSTRAINT_NAME = fk.CONSTRAINT_NAME\n" +
                            "   AND fk.CONSTRAINT_TYPE = 'R'\n" +
                            "  LEFT JOIN ALL_CONSTRAINTS puk\n" +
                            "    ON fk.R_OWNER = puk.OWNER\n" +
                            "   AND fk.R_CONSTRAINT_NAME = puk.CONSTRAINT_NAME\n" +
                            "   AND puk.CONSTRAINT_TYPE IN ('P', 'U')\n" +
                            "  LEFT JOIN TABLES p\n" +
                            "    ON puk.OWNER = p.OWNER\n" +
                            "   AND puk.TABLE_NAME = p.TABLE_NAME\n" +
                            "START WITH p.TABLE_NAME IS NULL\n" +
                            "CONNECT BY PRIOR t.TABLE_NAME = p.TABLE_NAME\n" +
                            "ORDER BY LEVEL DESC");
                }

                int n = 1 + (xmlDbAvailable ? 1 : 0);
                String[] params = new String[n];
                Arrays.fill(params, schema.getName());

                return jdbcTemplate.queryForStringList(tablesQuery.toString(), params);
            }
        },

        QUEUE_TABLE("QUEUE TABLE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT QUEUE_TABLE FROM ALL_QUEUE_TABLES WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_AQADM.DROP_QUEUE_TABLE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        MATERIALIZED_VIEW_LOG("MATERIALIZED VIEW LOG") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT MASTER FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " ON " + database.quote(schema.getName(), objectName);
            }
        },

        INDEX("INDEX") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ?" +
                                " AND INDEX_TYPE NOT LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        DOMAIN_INDEX("INDEX", "FORCE") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT INDEX_NAME FROM ALL_INDEXES WHERE OWNER = ? AND INDEX_TYPE LIKE '%DOMAIN%'",
                        schema.getName()
                );
            }
        },

        DOMAIN_INDEX_TYPE("INDEXTYPE", "FORCE"),

        OPERATOR("OPERATOR", "FORCE"),

        CLUSTER("CLUSTER", "INCLUDING TABLES CASCADE CONSTRAINTS"),

        VIEW("VIEW", "CASCADE CONSTRAINTS"),

        MATERIALIZED_VIEW("MATERIALIZED VIEW", "PRESERVE TABLE"),

        DIMENSION("DIMENSION") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT DIMENSION_NAME FROM ALL_DIMENSIONS WHERE OWNER = ?",
                        schema.getName()
                );
            }
        },

        SYNONYM("SYNONYM", "FORCE"),

        SEQUENCE("SEQUENCE"),

        PROCEDURE("PROCEDURE"),
        FUNCTION("FUNCTION"),
        PACKAGE("PACKAGE"),

        CONTEXT("CONTEXT") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT NAMESPACE FROM " + database.dbaOrAll("CONTEXT") + " WHERE SCHEMA = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " " + database.quote(objectName); // no owner
            }
        },

        TRIGGER("TRIGGER"),

        TYPE("TYPE", "FORCE"),

        JAVA_SOURCE("JAVA SOURCE"),
        JAVA_CLASS("JAVA CLASS"),
        JAVA_RESOURCE("JAVA RESOURCE"),

        LIBRARY("LIBRARY"),

        XML_SCHEMA("XML SCHEMA") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                if (!database.isXmlDbAvailable()) {
                    return Collections.emptyList();
                }
                return jdbcTemplate.queryForStringList(
                        "SELECT QUAL_SCHEMA_URL FROM " + database.dbaOrAll("XML_SCHEMAS") + " WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_XMLSCHEMA.DELETESCHEMA('" + objectName + "', DELETE_OPTION => DBMS_XMLSCHEMA.DELETE_CASCADE_FORCE); END;";
            }
        },

        REWRITE_EQUIVALENCE("REWRITE EQUIVALENCE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN SYS.DBMS_ADVANCED_REWRITE.DROP_REWRITE_EQUIVALENCE('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },

        SQL_TRANSLATION_PROFILE("SQL TRANSLATION PROFILE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SQL_TRANSLATOR.DROP_PROFILE('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },




        MINING_MODEL("MINING MODEL") {
            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {



                    return super.getObjectNames(jdbcTemplate, database, schema);







            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_DATA_MINING.DROP_MODEL('" +



                                database.quote(schema.getName(), objectName)



                        + "'); END;";
            }
        },

        SCHEDULER_JOB("JOB") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_JOB('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULER_PROGRAM("PROGRAM") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_PROGRAM('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULE("SCHEDULE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_SCHEDULE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        SCHEDULER_CHAIN("CHAIN") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_CHAIN('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        FILE_WATCHER("FILE WATCHER") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_FILE_WATCHER('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        RULE_SET("RULE SET") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_RULE_SET('" + database.quote(schema.getName(), objectName) + "', DELETE_RULES => FALSE); END;";
            }
        },
        RULE("RULE") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_RULE('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        EVALUATION_CONTEXT("EVALUATION CONTEXT") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_RULE_ADM.DROP_EVALUATION_CONTEXT('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },
        FILE_GROUP("FILE GROUP") {
            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_FILE_GROUP.DROP_FILE_GROUP('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },


        
        DATABASE_LINK("DATABASE LINK") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
                return jdbcTemplate.queryForStringList(
                        "SELECT DB_LINK FROM " + database.dbaOrAll("DB_LINKS") + " WHERE OWNER = ?",
                        schema.getName()
                );
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "DROP " + this.getName() + " " + objectName; 
            }
        },
        CREDENTIAL("CREDENTIAL") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_CREDENTIAL('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        DATABASE_DESTINATION("DESTINATION") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_DATABASE_DESTINATION('" + database.quote(schema.getName(), objectName) + "'); END;";
            }
        },
        SCHEDULER_GROUP("SCHEDULER GROUP") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }

            @Override
            public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
                return "BEGIN DBMS_SCHEDULER.DROP_GROUP('" + database.quote(schema.getName(), objectName) + "', FORCE => TRUE); END;";
            }
        },

        CUBE("CUBE") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },
        CUBE_DIMENSION("CUBE DIMENSION") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },
        CUBE_BUILD_PROCESS("CUBE BUILD PROCESS") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()), "cube build processes");
            }
        },
        MEASURE_FOLDER("MEASURE FOLDER") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },

        ASSEMBLY("ASSEMBLY") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()), "assemblies");
            }
        },
        JAVA_DATA("JAVA DATA") {
            @Override
            public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) {
                super.warnUnsupported(database.quote(schema.getName()));
            }
        },

        CAPTURE("CAPTURE"),
        APPLY("APPLY"),
        DIRECTORY("DIRECTORY"),
        RESOURCE_PLAN("RESOURCE PLAN"),
        CONSUMER_GROUP("CONSUMER GROUP"),
        JOB_CLASS("JOB CLASS"),
        WINDOWS("WINDOW"),
        EDITION("EDITION"),
        AGENT_DESTINATION("DESTINATION"),
        UNIFIED_AUDIT_POLICY("UNIFIED AUDIT POLICY");

                private final String name;

                private final String dropOptions;

        ObjectType(String name, String dropOptions) {
            this.name = name;
            this.dropOptions = dropOptions;
        }

        ObjectType(String name) {
            this(name, "");
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return super.toString().replace('_', ' ');
        }

                public List<String> getObjectNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            return jdbcTemplate.queryForStringList(
                    "SELECT DISTINCT OBJECT_NAME FROM ALL_OBJECTS WHERE OWNER = ? AND OBJECT_TYPE = ?",
                    schema.getName(), this.getName()
            );
        }

                public String generateDropStatement(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema, String objectName) {
            return "DROP " + this.getName() + " " + database.quote(schema.getName(), objectName) +
                    (StringUtils.hasText(dropOptions) ? " " + dropOptions : "");
        }

                public void dropObjects(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            for (String objectName : getObjectNames(jdbcTemplate, database, schema)) {
                jdbcTemplate.execute(generateDropStatement(jdbcTemplate, database, schema, objectName));
            }
        }

        private void warnUnsupported(String schemaName, String typeDesc) {
            LOG.warn("Unable to clean " + typeDesc + " for schema " + schemaName + ": unsupported operation");
        }

        private void warnUnsupported(String schemaName) {
            warnUnsupported(schemaName, this.toString().toLowerCase() + "s");
        }

                public static Set<String> getObjectTypeNames(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            boolean xmlDbAvailable = database.isXmlDbAvailable();








            String query =
                    "SELECT DISTINCT OBJECT_TYPE FROM " + database.dbaOrAll("OBJECTS") + " WHERE OWNER = ? " +
                            "UNION SELECT '" + MATERIALIZED_VIEW_LOG.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_MVIEW_LOGS WHERE LOG_OWNER = ?) " +
                            "UNION SELECT '" + DIMENSION.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_DIMENSIONS WHERE OWNER = ?) " +
                            "UNION SELECT '" + QUEUE_TABLE.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM ALL_QUEUE_TABLES WHERE OWNER = ?) " +
                            "UNION SELECT '" + DATABASE_LINK.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM " + database.dbaOrAll("DB_LINKS") + " WHERE OWNER = ?) " +
                            "UNION SELECT '" + CONTEXT.getName() + "' FROM DUAL WHERE EXISTS(" +
                            "SELECT * FROM " + database.dbaOrAll("CONTEXT") + " WHERE SCHEMA = ?) " +
                            (xmlDbAvailable
                                    ? "UNION SELECT '" + XML_SCHEMA.getName() + "' FROM DUAL WHERE EXISTS(" +
                                    "SELECT * FROM " + database.dbaOrAll("XML_SCHEMAS") + " WHERE OWNER = ?) "
                                    : "") +



                                    "UNION SELECT '" + CREDENTIAL.getName() + "' FROM DUAL WHERE EXISTS(" +
                                            "SELECT * FROM ALL_SCHEDULER_CREDENTIALS WHERE OWNER = ?) "








                    ;

            int n = 6 + (xmlDbAvailable ? 1 : 0) +



                            1



                    ;
            String[] params = new String[n];
            Arrays.fill(params, schema.getName());

            return new HashSet<>(jdbcTemplate.queryForStringList(query, params));
        }

                public static boolean supportedTypesExist(JdbcTemplate jdbcTemplate, OracleDatabase database, OracleSchema schema) throws SQLException {
            Set<String> existingTypeNames = new HashSet<>(getObjectTypeNames(jdbcTemplate, database, schema));

            existingTypeNames.removeAll(Arrays.asList(
                    DATABASE_LINK.getName(),
                    CREDENTIAL.getName(),
                    DATABASE_DESTINATION.getName(),
                    SCHEDULER_GROUP.getName(),
                    CUBE.getName(),
                    CUBE_DIMENSION.getName(),
                    CUBE_BUILD_PROCESS.getName(),
                    MEASURE_FOLDER.getName(),
                    ASSEMBLY.getName(),
                    JAVA_DATA.getName()
            ));

            return !existingTypeNames.isEmpty();
        }
    }
}