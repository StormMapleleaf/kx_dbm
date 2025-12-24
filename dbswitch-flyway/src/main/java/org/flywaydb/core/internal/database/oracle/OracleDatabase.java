package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.RowMapper;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OracleDatabase extends Database<OracleConnection> {
    private static final String ORACLE_NET_TNS_ADMIN = "oracle.net.tns_admin";

        public static void enableTnsnamesOraSupport() {
        String tnsAdminEnvVar = System.getenv("TNS_ADMIN");
        String tnsAdminSysProp = System.getProperty(ORACLE_NET_TNS_ADMIN);
        if (StringUtils.hasLength(tnsAdminEnvVar) && tnsAdminSysProp == null) {
            System.setProperty(ORACLE_NET_TNS_ADMIN, tnsAdminEnvVar);
        }
    }

        public OracleDatabase(Configuration configuration, JdbcConnectionFactory jdbcConnectionFactory



    ) {
        super(configuration, jdbcConnectionFactory



        );
    }

    @Override
    protected OracleConnection doGetConnection(Connection connection) {
        return new OracleConnection(this, connection);
    }














    @Override
    public final void ensureSupported() {
        ensureDatabaseIsRecentEnough("10");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition("12.2", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessary("19.0");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace = configuration.getTablespace() == null
                ? ""
                : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table + " (\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR2(50),\n" +
                "    \"description\" VARCHAR2(200) NOT NULL,\n" +
                "    \"type\" VARCHAR2(20) NOT NULL,\n" +
                "    \"script\" VARCHAR2(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR2(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" NUMBER(1) NOT NULL,\n" +
                "    CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\")\n" +
                ")" + tablespace + ";\n" +
                (baseline ? getBaselineStatement(table) + ";\n" : "") +
                "CREATE INDEX \"" + table.getSchema().getName() + "\".\"" + table.getName() + "_s_idx\" ON " + table + " (\"success\");\n";
    }

    @Override
    public boolean supportsEmptyMigrationDescription() {
        return false;
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT USER FROM DUAL");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

        boolean queryReturnsRows(String query, String... params) throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForBoolean("SELECT CASE WHEN EXISTS(" + query + ") THEN 1 ELSE 0 END FROM DUAL", params);
    }

        boolean isPrivOrRoleGranted(String name) throws SQLException {
        return queryReturnsRows("SELECT 1 FROM SESSION_PRIVS WHERE PRIVILEGE = ? UNION ALL " +
                "SELECT 1 FROM SESSION_ROLES WHERE ROLE = ?", name, name);
    }

        private boolean isDataDictViewAccessible(String owner, String name) throws SQLException {
        return queryReturnsRows("SELECT * FROM ALL_TAB_PRIVS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?" +
                " AND PRIVILEGE = 'SELECT'", owner, name);
    }

        boolean isDataDictViewAccessible(String name) throws SQLException {
        return isDataDictViewAccessible("SYS", name);
    }

        String dbaOrAll(String baseName) throws SQLException {
        return isPrivOrRoleGranted("SELECT ANY DICTIONARY") || isDataDictViewAccessible("DBA_" + baseName)
                ? "DBA_" + baseName
                : "ALL_" + baseName;
    }

        private Set<String> getAvailableOptions() throws SQLException {
        return new HashSet<>(getMainConnection().getJdbcTemplate()
                .queryForStringList("SELECT PARAMETER FROM V$OPTION WHERE VALUE = 'TRUE'"));
    }

        boolean isFlashbackDataArchiveAvailable() throws SQLException {
        return getAvailableOptions().contains("Flashback Data Archive");
    }

        boolean isXmlDbAvailable() throws SQLException {
        return isDataDictViewAccessible("ALL_XML_TABLES");
    }

        boolean isDataMiningAvailable() throws SQLException {
        return getAvailableOptions().contains("Data Mining");
    }

        boolean isLocatorAvailable() throws SQLException {
        return isDataDictViewAccessible("MDSYS", "ALL_SDO_GEOM_METADATA");
    }

        Set<String> getSystemSchemas() throws SQLException {

        Set<String> result = new HashSet<>(Arrays.asList(
                "SYS", "SYSTEM", 
                "SYSBACKUP", "SYSDG", "SYSKM", "SYSRAC", "SYS$UMF", 
                "DBSNMP", "MGMT_VIEW", "SYSMAN", 
                "OUTLN", 
                "AUDSYS", 
                "ORACLE_OCM", 
                "APPQOSSYS", 
                "OJVMSYS", 
                "DVF", "DVSYS", 
                "DBSFWUSER", 
                "REMOTE_SCHEDULER_AGENT", 
                "DIP", 
                "APEX_PUBLIC_USER", "FLOWS_FILES", 
                "ANONYMOUS", "XDB", "XS$NULL", 
                "CTXSYS", 
                "LBACSYS", 
                "EXFSYS", 
                "MDDATA", "MDSYS", "SPATIAL_CSW_ADMIN_USR", "SPATIAL_WFS_ADMIN_USR", 
                "ORDDATA", "ORDPLUGINS", "ORDSYS", "SI_INFORMTN_SCHEMA", 
                "WMSYS", 
                "OLAPSYS", 
                "OWBSYS", "OWBSYS_AUDIT", 
                "GSMADMIN_INTERNAL", "GSMCATUSER", "GSMUSER", 
                "GGSYS", 
                "WK_TEST", "WKSYS", "WKPROXY", 
                "ODM", "ODM_MTR", "DMSYS", 
                "TSMSYS" 
        ));






        result.addAll(getMainConnection().getJdbcTemplate().queryForStringList("SELECT USERNAME FROM ALL_USERS " +
                        "WHERE REGEXP_LIKE(USERNAME, '^(APEX|FLOWS)_\\d+$')" +



                                " OR ORACLE_MAINTAINED = 'Y'"



        ));
























        return result;
    }
}