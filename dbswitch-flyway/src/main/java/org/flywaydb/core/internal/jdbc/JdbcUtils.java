package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {
    private static final Log LOG = LogFactory.getLog(JdbcUtils.class);

        private JdbcUtils() {
    }

        public static Connection openConnection(DataSource dataSource, int connectRetries) throws FlywayException {
        int retries = 0;
        while (true) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                if ("08S01".equals(e.getSQLState()) && e.getMessage().contains("This driver is not configured for integrated authentication")) {
                    throw new FlywaySqlException("Unable to obtain connection from database"
                            + getDataSourceInfo(dataSource) + ": " + e.getMessage() + "\nTo setup integrated authentication see https://flywaydb.org/documentation/database/sqlserver#windows-authentication--azure-active-directory", e);
                }

                if (++retries > connectRetries) {
                    throw new FlywaySqlException("Unable to obtain connection from database"
                            + getDataSourceInfo(dataSource) + ": " + e.getMessage(), e);
                }
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                String msg = "Connection error: " + e.getMessage();
                if (rootCause != null && rootCause != e && rootCause.getMessage() != null) {
                    msg += " (Caused by " + rootCause.getMessage() + ")";
                }
                LOG.warn(msg + " Retrying in 1 sec...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    throw new FlywaySqlException("Unable to obtain connection from database"
                            + getDataSourceInfo(dataSource) + ": " + e.getMessage(), e);
                }
            }
        }
    }

    private static String getDataSourceInfo(DataSource dataSource) {
        if (!(dataSource instanceof DriverDataSource)) {
            return "";
        }
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        return " (" + driverDataSource.getUrl() + ") for user '" + driverDataSource.getUser() + "'";
    }

        public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (Exception e) {
            LOG.error("Error while closing database connection: " + e.getMessage(), e);
        }
    }

        public static void closeStatement(Statement statement) {
        if (statement == null) {
            return;
        }

        try {
            statement.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC statement", e);
        }
    }

        public static void closeResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
        } catch (SQLException e) {
            LOG.error("Error while closing JDBC resultSet", e);
        }
    }

        public static DatabaseMetaData getDatabaseMetaData(Connection connection) {
        DatabaseMetaData databaseMetaData;
        try {
            databaseMetaData = connection.getMetaData();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database connection metadata: " + e.getMessage(), e);
        }
        if (databaseMetaData == null) {
            throw new FlywayException("Unable to read database connection metadata while it is null!");
        }
        return databaseMetaData;
    }

        public static String getDatabaseProductName(DatabaseMetaData databaseMetaData) {
        try {
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            if (databaseProductName == null) {
                throw new FlywayException("Unable to determine database. Product name is null.");
            }

            int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
            int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();

            return databaseProductName + " " + databaseMajorVersion + "." + databaseMinorVersion;
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database product name", e);
        }
    }

        public static String getDatabaseProductVersion(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database product version", e);
        }
    }

        public static String getDriverName(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDriverName();
        } catch (SQLException e) {
            throw new FlywaySqlException("Error while determining database driver name", e);
        }
    }
}