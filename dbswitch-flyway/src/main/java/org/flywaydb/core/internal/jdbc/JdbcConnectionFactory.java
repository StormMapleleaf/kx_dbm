package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.exception.FlywaySqlException;


import org.flywaydb.core.internal.util.ExceptionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class JdbcConnectionFactory {
    private static final Log LOG = LogFactory.getLog(JdbcConnectionFactory.class);

    private final DataSource dataSource;
    private final int connectRetries;
    private final DatabaseType databaseType;
    private final String jdbcUrl;
    private final String driverInfo;
    private final String productName;

    private Connection firstConnection;
    private ConnectionInitializer connectionInitializer;
















        public JdbcConnectionFactory(DataSource dataSource, int connectRetries



    ) {
        this.dataSource = dataSource;
        this.connectRetries = connectRetries;

        firstConnection = JdbcUtils.openConnection(dataSource, connectRetries);

        this.databaseType = DatabaseType.fromJdbcConnection(firstConnection);
        final DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(firstConnection);
        this.jdbcUrl = getJdbcUrl(databaseMetaData);
        this.driverInfo = getDriverInfo(databaseMetaData);
        this.productName = JdbcUtils.getDatabaseProductName(databaseMetaData);





    }

    public void setConnectionInitializer(ConnectionInitializer connectionInitializer) {
        this.connectionInitializer = connectionInitializer;
    }



























































        public DatabaseType getDatabaseType() {
        return databaseType;
    }

        public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDriverInfo() {
        return driverInfo;
    }

    public String getProductName() {
        return productName;
    }

        public Connection openConnection() throws FlywayException {
        Connection connection =
                firstConnection == null ? JdbcUtils.openConnection(dataSource, connectRetries) : firstConnection;
        firstConnection = null;

        if (connectionInitializer != null) {
            connectionInitializer.initialize(this, connection);
        }












        return connection;
    }

    public interface ConnectionInitializer {
        void initialize(JdbcConnectionFactory jdbcConnectionFactory, Connection connection);
    }

    
    private static String getJdbcUrl(DatabaseMetaData databaseMetaData) {
        String url;
        try {
            url = databaseMetaData.getURL();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the JDBC connection URL!", e);
        }
        if (url == null) {
            return "";
        }
        return filterUrl(url);
    }

        static String filterUrl(String url) {
        int questionMark = url.indexOf("?");
        if (questionMark >= 0 && !url.contains("?databaseName=")) {
            url = url.substring(0, questionMark);
        }
        url = url.replaceAll("://.*:.*@", "://");
        return url;
    }

    private static String getDriverInfo(DatabaseMetaData databaseMetaData) {
        try {
            return databaseMetaData.getDriverName() + " " + databaseMetaData.getDriverVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read database driver info: " + e.getMessage(), e);
        }
    }
}