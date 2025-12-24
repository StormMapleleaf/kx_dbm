package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.FlywayException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@SuppressWarnings("SqlDialectInspection")
public enum DatabaseType {
    COCKROACHDB("CockroachDB", Types.NULL, false),
    DB2("DB2", Types.VARCHAR, true),



    DERBY("Derby", Types.VARCHAR, true),
    FIREBIRD("Firebird", Types.NULL, true), 
    H2("H2", Types.VARCHAR, true),
    HSQLDB("HSQLDB", Types.VARCHAR, true),
    INFORMIX("Informix", Types.VARCHAR, true),
    MARIADB("MariaDB", Types.VARCHAR, true),
    MYSQL("MySQL", Types.VARCHAR, true),
    ORACLE("Oracle", Types.VARCHAR, true),
    POSTGRESQL("PostgreSQL", Types.NULL, true),
    REDSHIFT("Redshift", Types.VARCHAR, true),
    SQLITE("SQLite", Types.VARCHAR, false),
    SQLSERVER("SQL Server", Types.VARCHAR, true),
    SYBASEASE_JTDS("Sybase ASE", Types.NULL, true),
    SYBASEASE_JCONNECT("Sybase ASE", Types.VARCHAR, true),
    SAPHANA("SAP HANA", Types.VARCHAR, true),
    SNOWFLAKE("Snowflake", Types.VARCHAR, false);

    private final String name;

    private final int nullType;

    private final boolean supportsReadOnlyTransactions;

    DatabaseType(String name, int nullType, boolean supportsReadOnlyTransactions) {
        this.name = name;
        this.nullType = nullType;
        this.supportsReadOnlyTransactions = supportsReadOnlyTransactions;
    }

    public static DatabaseType fromJdbcConnection(Connection connection) {
        DatabaseMetaData databaseMetaData = JdbcUtils.getDatabaseMetaData(connection);
        String databaseProductName = JdbcUtils.getDatabaseProductName(databaseMetaData);
        String databaseProductVersion = JdbcUtils.getDatabaseProductVersion(databaseMetaData);

        return fromDatabaseProductNameAndVersion(databaseProductName, databaseProductVersion, connection);
    }

    private static DatabaseType fromDatabaseProductNameAndVersion(String databaseProductName,
                                                                  String databaseProductVersion,
                                                                  Connection connection) {
        if (databaseProductName.startsWith("Apache Derby")) {
            return DERBY;
        }
        if (databaseProductName.startsWith("SQLite")) {
            return SQLITE;
        }
        if (databaseProductName.startsWith("H2")) {
            return H2;
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
            return HSQLDB;
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
            return SQLSERVER;
        }

        if (databaseProductName.startsWith("MariaDB")
                || (databaseProductName.contains("MySQL") && databaseProductVersion.contains("MariaDB"))
                || (databaseProductName.contains("MySQL") && getSelectVersionOutput(connection).contains("MariaDB"))) {
            return MARIADB;
        }

        if (databaseProductName.contains("MySQL")) {
            return MYSQL;
        }
        if (databaseProductName.startsWith("Oracle")) {
            return ORACLE;
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
            String selectVersionQueryOutput = getSelectVersionOutput(connection);
            if (databaseProductName.startsWith("PostgreSQL 8") && selectVersionQueryOutput.contains("Redshift")) {
                return REDSHIFT;
            }
            if (selectVersionQueryOutput.contains("CockroachDB")) {
                return COCKROACHDB;
            }
            return POSTGRESQL;
        }
        if (databaseProductName.startsWith("DB2")) {





            return DB2;
        }
        if (databaseProductName.startsWith("ASE")) {
            return SYBASEASE_JTDS;
        }
        if (databaseProductName.startsWith("Adaptive Server Enterprise")) {
            return SYBASEASE_JCONNECT;
        }
        if (databaseProductName.startsWith("HDB")) {
            return SAPHANA;
        }
        if (databaseProductName.startsWith("Informix")) {
            return INFORMIX;
        }
        if (databaseProductName.startsWith("Firebird")) {
            return FIREBIRD;
        }
        if (databaseProductName.startsWith("Snowflake")) {
            return SNOWFLAKE;
        }
        throw new FlywayException("Unsupported Database: " + databaseProductName);
    }

        public static String getSelectVersionOutput(Connection connection) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String result;
        try {
            statement = connection.prepareStatement("SELECT version()");
            resultSet = statement.executeQuery();
            result = null;
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            return "";
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
        }

        return result;
    }

        public String getName() {
        return name;
    }

        public int getNullType() {
        return nullType;
    }

    @Override
    public String toString() {
        return name;
    }











}