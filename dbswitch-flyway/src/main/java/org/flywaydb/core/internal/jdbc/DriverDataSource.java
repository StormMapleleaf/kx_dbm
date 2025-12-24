package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.FeatureDetector;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class DriverDataSource implements DataSource {
    private static final Log LOG = LogFactory.getLog(DriverDataSource.class);

        public enum DriverType {
        DB2("jdbc:db2:", "com.ibm.db2.jcc.DB2Driver"),
        DERBY_CLIENT("jdbc:derby://", "org.apache.derby.jdbc.ClientDriver"),
        DERBY_EMBEDDED("jdbc:derby:", "org.apache.derby.jdbc.EmbeddedDriver"),
        FIREBIRD("jdbc:firebird:", "org.firebirdsql.jdbc.FBDriver"),
        FIREBIRD_SQL("jdbc:firebirdsql:", "org.firebirdsql.jdbc.FBDriver"),
        H2("jdbc:h2:", "org.h2.Driver"),
        HSQL("jdbc:hsqldb:", "org.hsqldb.jdbcDriver"),
        INFORMIX("jdbc:informix-sqli:", "com.informix.jdbc.IfxDriver"),
        JTDS("jdbc:jtds:", "net.sourceforge.jtds.jdbc.Driver"),
        MARIADB("jdbc:mariadb:", "org.mariadb.jdbc.Driver"),
        MYSQL("jdbc:mysql:", "com.mysql.cj.jdbc.Driver"),
        MYSQL_GOOGLE("jdbc:google:", "com.mysql.jdbc.GoogleDriver"),
        ORACLE("jdbc:oracle", "oracle.jdbc.OracleDriver"),
        POSTGRESQL("jdbc:postgresql:", "org.postgresql.Driver"),
        REDSHIFT("jdbc:redshift:", "com.amazon.redshift.jdbc42.Driver"),
        SAPHANA("jdbc:sap:", "com.sap.db.jdbc.Driver"),
        SNOWFLAKE("jdbc:snowflake:", "net.snowflake.client.jdbc.SnowflakeDriver"),
        SQLDROID("jdbc:sqldroid:", "org.sqldroid.SQLDroidDriver"),
        SQLLITE("jdbc:sqlite:", "org.sqlite.JDBC"),
        SQLSERVER("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        SYBASE("jdbc:sybase:", "com.sybase.jdbc4.jdbc.SybDriver"),
        TEST_CONTAINERS("jdbc:tc:", "org.testcontainers.jdbc.ContainerDatabaseDriver");

        DriverType(String prefix, String driverClass) {
            this.prefix = prefix;
            this.driverClass = driverClass;
        }

        public String prefix;
        public String driverClass;

        public boolean matches(String url) {
            return url.startsWith(prefix);
        }
    }

    private static final String MYSQL_LEGACY_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String REDSHIFT_JDBC4_DRIVER = "com.amazon.redshift.jdbc4.Driver";
    private static final String REDSHIFT_JDBC41_DRIVER = "com.amazon.redshift.jdbc41.Driver";

        private static final String APPLICATION_NAME = "Flyway by Redgate";

        private Driver driver;

        private final String url;

        private final DriverType type;

        private final String user;

        private final String password;

        private final Properties defaultProps;

        private final ClassLoader classLoader;

        private boolean autoCommit = true;

        public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password) throws FlywayException {
        this(classLoader, driverClass, url, user, password, new Properties());
    }

        public DriverDataSource(ClassLoader classLoader, String driverClass, String url, String user, String password,
                            Properties props) throws FlywayException {
        this.classLoader = classLoader;
        this.url = detectFallbackUrl(url);
        this.type = detectDriverTypeForUrl(url);

        if (!StringUtils.hasLength(driverClass)) {
            if (type == null) {
                throw new FlywayException("Unable to autodetect JDBC driver for url: " + url);
            }

            driverClass = detectDriverForType(type);
        }

        this.defaultProps = new Properties(props);
        this.defaultProps.putAll(detectPropsForType(type));

        try {
            this.driver = ClassUtils.instantiate(driverClass, classLoader);
        } catch (FlywayException e) {
            String backupDriverClass = detectBackupDriverForType(type);
            if (backupDriverClass == null) {
                throw new FlywayException("Unable to instantiate JDBC driver: " + driverClass
                        + " => Check whether the jar file is present", e,
                        ErrorCode.JDBC_DRIVER);
            }
            try {
                this.driver = ClassUtils.instantiate(backupDriverClass, classLoader);
            } catch (Exception e1) {
                throw new FlywayException(
                        "Unable to instantiate JDBC driver: " + driverClass + " => Check whether the jar file is present", e,
                        ErrorCode.JDBC_DRIVER);
            }
        }

        this.user = detectFallbackUser(user);
        this.password = detectFallbackPassword(password);
    }

        private String detectFallbackUrl(String url) {
        if (!StringUtils.hasText(url)) {
            String boxfuseDatabaseUrl = System.getenv("BOXFUSE_DATABASE_URL");
            if (StringUtils.hasText(boxfuseDatabaseUrl)) {
                return boxfuseDatabaseUrl;
            }

            throw new FlywayException("Missing required JDBC URL. Unable to create DataSource!");
        }

        if (!url.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid JDBC URL (should start with jdbc:) : " + url);
        }
        return url;
    }

        private String detectFallbackUser(String user) {
        if (!StringUtils.hasText(user)) {
            String boxfuseDatabaseUser = System.getenv("BOXFUSE_DATABASE_USER");
            if (StringUtils.hasText(boxfuseDatabaseUser)) {
                return boxfuseDatabaseUser;
            }
        }
        return user;
    }

        public static boolean detectUserRequiredByUrl(String url) {
        if (DriverDataSource.DriverType.SNOWFLAKE.matches(url)
                || DriverDataSource.DriverType.POSTGRESQL.matches(url)) {
            return !url.contains("user=");
        }
        if (DriverDataSource.DriverType.SQLSERVER.matches(url)) {
            return !url.contains("integratedSecurity=")
                    && !url.contains("authentication=ActiveDirectoryIntegrated")
                    && !url.contains("authentication=ActiveDirectoryMSI");
        }
        if (DriverDataSource.DriverType.ORACLE.matches(url)) {
            Pattern pattern = Pattern.compile("^jdbc:oracle:thin:[a-zA-Z0-9#_$]+/[a-zA-Z0-9#_$]+@//.*");
            return !pattern.matcher(url).matches();
        }
        return true;
    }

        public static boolean detectPasswordRequiredByUrl(String url) {
        if (DriverDataSource.DriverType.SNOWFLAKE.matches(url)) {
            return !url.contains("private_key_file=");
        }
        if (DriverDataSource.DriverType.POSTGRESQL.matches(url)) {
            return !url.contains("password=");
        }
        if (DriverDataSource.DriverType.SQLSERVER.matches(url)) {
            return !url.contains("integratedSecurity=")
                    && !url.contains("authentication=ActiveDirectoryIntegrated")
                    && ! url.contains("authentication=ActiveDirectoryMSI");
        }
        if (DriverDataSource.DriverType.ORACLE.matches(url)) {
            Pattern pattern = Pattern.compile("^jdbc:oracle:thin:[a-zA-Z0-9#_$]+/[a-zA-Z0-9#_$]+@//.*");
            return !pattern.matcher(url).matches();
        }
        return true;
    }

        private String detectFallbackPassword(String password) {
        if (!StringUtils.hasText(password)) {
            String boxfuseDatabasePassword = System.getenv("BOXFUSE_DATABASE_PASSWORD");
            if (StringUtils.hasText(boxfuseDatabasePassword)) {
                return boxfuseDatabasePassword;
            }
        }
        return password;
    }

        private Properties detectPropsForType(DriverType type) {
        Properties result = new Properties();

        if (DriverType.ORACLE.equals(type)) {
            String osUser = System.getProperty("user.name");
            result.put("v$session.osuser", osUser.substring(0, Math.min(osUser.length(), 30)));
            result.put("v$session.program", APPLICATION_NAME);
            result.put("oracle.net.keepAlive", "true");
            String oobb = ClassUtils.getStaticFieldValue("oracle.jdbc.OracleConnection", "CONNECTION_PROPERTY_THIN_NET_DISABLE_OUT_OF_BAND_BREAK", classLoader);
            result.put(oobb, "true");
        } else if (DriverType.SQLSERVER.equals(type)) {
            result.put("applicationName", APPLICATION_NAME);
        } else if (DriverType.POSTGRESQL.equals(type)) {
            result.put("ApplicationName", APPLICATION_NAME);
        } else if (DriverType.MYSQL.equals(type) || DriverType.MARIADB.equals(type)) {
            result.put("connectionAttributes", "program_name:" + APPLICATION_NAME);
        } else if (DriverType.DB2.equals(type)) {
            result.put("clientProgramName", APPLICATION_NAME);
            result.put("retrieveMessagesFromServerOnGetMessage", "true");
        } else if (DriverType.SYBASE.equals(type)) {
            result.put("APPLICATIONNAME", APPLICATION_NAME);
        } else if (DriverType.SAPHANA.equals(type)) {
            result.put("SESSIONVARIABLE:APPLICATION", APPLICATION_NAME);
        } else if (DriverType.FIREBIRD_SQL.equals(type) || DriverType.FIREBIRD.equals(type)) {
            result.put("processName", APPLICATION_NAME);
        }


        return result;
    }

        private DriverType detectDriverTypeForUrl(String url) {
        for (DriverType type : DriverType.values()) {
            if (type.matches(url)) {
                return type;
            }
        }

        return null;
    }

        private String detectBackupDriverForType(DriverType type) {
        if (DriverType.MYSQL.equals(type) && ClassUtils.isPresent(MYSQL_LEGACY_JDBC_DRIVER, classLoader)) {
            return MYSQL_LEGACY_JDBC_DRIVER;
        }

        if (DriverType.MYSQL.equals(type) && ClassUtils.isPresent(DriverType.MARIADB.driverClass, classLoader)) {
            LOG.warn("You are attempting to connect to a MySQL database using the MariaDB driver." +
                    " This is known to cause issues." +
                    " An upgrade to Oracle's MySQL JDBC driver is highly recommended.");
            return DriverType.MARIADB.driverClass;
        }

        if (DriverType.REDSHIFT.equals(type)) {
            if (ClassUtils.isPresent(REDSHIFT_JDBC41_DRIVER, classLoader)) {
                return REDSHIFT_JDBC41_DRIVER;
            }
            return REDSHIFT_JDBC4_DRIVER;
        }

        return null;
    }

        private String detectDriverForType(DriverType type) {
        if (DriverType.SQLLITE.equals(type)) {
            if (new FeatureDetector(classLoader).isAndroidAvailable()) {
                return DriverType.SQLDROID.driverClass;
            }
        }

        return type.driverClass;
    }

        public Driver getDriver() {
        return this.driver;
    }

        public String getUrl() {
        return this.url;
    }

        public String getUser() {
        return this.user;
    }

        public String getPassword() {
        return this.password;
    }

        @Override
    public Connection getConnection() throws SQLException {
        return getConnectionFromDriver(getUser(), getPassword());
    }

        @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnectionFromDriver(username, password);
    }


        protected Connection getConnectionFromDriver(String username, String password) throws SQLException {
        Properties props = new Properties(this.defaultProps);
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }

        Connection connection = driver.connect(url, props);
        if (connection == null) {
            throw new FlywayException("Unable to connect to " + url);
        }
        connection.setAutoCommit(autoCommit);
        return connection;
    }

        public boolean isAutoCommit() {
        return autoCommit;
    }

        public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public void setLoginTimeout(int timeout) {
        unsupportedMethod("setLoginTimeout");
    }

    @Override
    public PrintWriter getLogWriter() {
        unsupportedMethod("getLogWriter");
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter pw) {
        unsupportedMethod("setLogWriter");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        unsupportedMethod("unwrap");
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return DataSource.class.equals(iface);
    }

    @Override
    public Logger getParentLogger() {
        unsupportedMethod("getParentLogger");
        return null;
    }

    private void unsupportedMethod(String methodName) {
        throw new UnsupportedOperationException(methodName);
    }

        public void shutdownDatabase() {
        if (DriverType.DERBY_EMBEDDED.equals(type)) {
            try {
                int i = url.indexOf(";");
                String shutdownUrl = (i < 0 ? url : url.substring(0, i)) + ";shutdown=true";

                driver.connect(shutdownUrl, new Properties());
            } catch (SQLException e) {
                LOG.debug("Expected error on Derby Embedded Database shutdown: " + e.getMessage());
            }
        }
    }
}