package com.databasebackup.database.impl;

import com.databasebackup.database.ConnectionConfig;
import com.databasebackup.database.DatabaseConnection;
import com.databasebackup.database.DatabaseType;
import com.databasebackup.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL-specific implementation of DatabaseConnection.
 * Provides MySQL connection management using JDBC driver.
 */
public class MySQLConnection implements DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(MySQLConnection.class);

    private final ConnectionConfig config;
    private Connection connection;
    private boolean isConnected = false;

    public MySQLConnection(ConnectionConfig config) {
        if (!config.getDatabaseType().equals(DatabaseType.MYSQL)) {
            throw new IllegalArgumentException("MySQLConnection requires MySQL database type");
        }
        this.config = config;
    }

    /**
     * Establish a connection to MySQL database
     */
    public void connect() throws DatabaseConnectionException {
        if (isConnected && connection != null) {
            logger.debug("Already connected to MySQL");
            return;
        }

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.debug("MySQL JDBC driver loaded successfully");

            // Build JDBC connection string
            String jdbcUrl = buildJdbcUrl();
            logger.debug("Connecting to: {}", jdbcUrl.replaceAll("password=[^&]*", "password=***"));

            // Create connection with timeout
            DriverManager.setLoginTimeout(config.getConnectionTimeoutSeconds());
            
            this.connection = DriverManager.getConnection(
                jdbcUrl,
                config.getUsername(),
                config.getPassword()
            );

            this.isConnected = true;
            logger.info("Successfully connected to MySQL database: {}", config.getDatabase());

        } catch (ClassNotFoundException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "MySQL JDBC driver not found. Please ensure mysql-connector-java is in classpath",
                e
            );
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Failed to connect: " + parseSQLException(e),
                e
            );
        }
    }

    /**
     * Build JDBC connection string for MySQL
     */
    private String buildJdbcUrl() {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://")
           .append(config.getHost())
           .append(":")
           .append(config.getPort())
           .append("/")
           .append(config.getDatabase());

        // Add SSL parameter if enabled
        if (config.isSslEnabled()) {
            url.append("?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        } else {
            url.append("?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        }

        // Add connection timeout
        url.append("&connectTimeout=").append(config.getConnectionTimeoutSeconds() * 1000);

        return url.toString();
    }

    /**
     * Parse SQL exception to provide meaningful error messages
     */
    private String parseSQLException(SQLException e) {
        int errorCode = e.getErrorCode();
        String state = e.getSQLState();

        switch (errorCode) {
            case 1045:
                return "Access denied. Check username and password";
            case 1049:
                return "Database '" + config.getDatabase() + "' does not exist";
            case 2003:
                return "Cannot connect to host '" + config.getHost() + ":" + config.getPort() + "'";
            case 2006:
                return "MySQL server has gone away";
            case 2013:
                return "Lost connection to MySQL server during query";
            default:
                if (state != null && state.startsWith("08")) {
                    return "Connection error: " + e.getMessage();
                }
                return e.getMessage();
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return config.getDatabaseType();
    }

    @Override
    public ConnectionConfig getConfig() {
        return config;
    }

    @Override
    public Object getUnderlyingConnection() {
        return connection;
    }

    @Override
    public void testConnection() throws DatabaseConnectionException {
        try {
            if (!isConnected) {
                connect();
            }

            // Execute ping query
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1");
                if (rs.next()) {
                    logger.info("Connection test successful");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Connection test failed: " + e.getMessage(),
                e
            );
        }
    }

    @Override
    public boolean isValid() {
        if (!isConnected || connection == null) {
            return false;
        }

        try {
            return connection.isValid(config.getConnectionTimeoutSeconds());
        } catch (SQLException e) {
            logger.warn("Error validating connection", e);
            return false;
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                isConnected = false;
                logger.info("MySQL connection closed");
            } catch (SQLException e) {
                logger.warn("Error closing MySQL connection", e);
            }
        }
    }

    @Override
    public void validateCredentials() throws DatabaseConnectionException {
        try {
            // Attempt to establish connection
            connect();
            logger.info("Credentials validated successfully");
        } catch (DatabaseConnectionException e) {
            throw e;
        }
    }

    @Override
    public String getDatabaseVersion() throws DatabaseConnectionException {
        try {
            if (!isConnected) {
                connect();
            }

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT VERSION()");
                if (rs.next()) {
                    String version = rs.getString(1);
                    logger.info("MySQL Version: {}", version);
                    return version;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Failed to retrieve database version: " + e.getMessage(),
                e
            );
        }
        return "Unknown";
    }

    @Override
    public Map<String, String> getMetadata() throws DatabaseConnectionException {
        Map<String, String> metadata = new HashMap<>();

        try {
            if (!isConnected) {
                connect();
            }

            DatabaseMetaData dbMetadata = connection.getMetaData();

            metadata.put("Database Product Name", dbMetadata.getDatabaseProductName());
            metadata.put("Database Product Version", dbMetadata.getDatabaseProductVersion());
            metadata.put("Driver Name", dbMetadata.getDriverName());
            metadata.put("Driver Version", dbMetadata.getDriverVersion());
            metadata.put("Database User", dbMetadata.getUserName());
            metadata.put("JDBC URL", dbMetadata.getURL());
            metadata.put("Connection Timeout", config.getConnectionTimeoutSeconds() + " seconds");

            // Get database statistics
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as db_count FROM information_schema.SCHEMATA WHERE SCHEMA_NAME NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')"
                );
                if (rs.next()) {
                    metadata.put("Database Count", String.valueOf(rs.getInt("db_count")));
                }
            }

            logger.debug("MySQL metadata retrieved successfully");

        } catch (SQLException e) {
            logger.warn("Error retrieving metadata", e);
            metadata.put("Error", e.getMessage());
        }

        return metadata;
    }

    @Override
    public void reconnect() throws DatabaseConnectionException {
        logger.info("Reconnecting to MySQL database");
        close();
        connect();
        testConnection();
    }

    @Override
    public String getConnectionPoolInfo() {
        return "Direct JDBC Connection - No pooling";
    }

    /**
     * Get table count in the connected database
     */
    public int getTableCount() throws DatabaseConnectionException {
        try {
            if (!isConnected) {
                connect();
            }

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as table_count FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + config.getDatabase() + "'"
                );
                if (rs.next()) {
                    return rs.getInt("table_count");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Failed to get table count: " + e.getMessage(),
                e
            );
        }
        return 0;
    }

    /**
     * Get database size in MB
     */
    public long getDatabaseSizeMB() throws DatabaseConnectionException {
        try {
            if (!isConnected) {
                connect();
            }

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as size_mb " +
                    "FROM information_schema.TABLES WHERE table_schema = '" + config.getDatabase() + "'"
                );
                if (rs.next()) {
                    return rs.getLong("size_mb");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Failed to get database size: " + e.getMessage(),
                e
            );
        }
        return 0;
    }

    /**
     * Check if database exists
     */
    public boolean databaseExists() throws DatabaseConnectionException {
        try {
            if (!isConnected) {
                connect();
            }

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = '" + config.getDatabase() + "'"
                );
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseConnectionException(
                config.getDatabaseType().getDisplayName(),
                config.getHost(),
                config.getPort(),
                "Failed to check if database exists: " + e.getMessage(),
                e
            );
        }
    }
}
