package com.databasebackup.database.impl;

import com.databasebackup.database.ConnectionConfig;
import com.databasebackup.database.DatabaseConnection;
import com.databasebackup.database.DatabaseType;
import com.databasebackup.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating MySQL database connections.
 * Provides MySQL-specific connection creation and configuration.
 */
public class MySQLConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(MySQLConnectionFactory.class);

    /**
     * Create a MySQL connection
     */
    public static DatabaseConnection createConnection(ConnectionConfig config) throws DatabaseConnectionException {
        if (!config.getDatabaseType().equals(DatabaseType.MYSQL)) {
            throw new IllegalArgumentException("MySQLConnectionFactory requires MySQL database type");
        }

        logger.info("Creating MySQL connection to {}:{}/{}", 
            config.getHost(), config.getPort(), config.getDatabase());

        MySQLConnection connection = new MySQLConnection(config);
        connection.connect();

        return connection;
    }

    /**
     * Create and immediately test MySQL connection
     */
    public static DatabaseConnection createAndTestConnection(ConnectionConfig config) throws DatabaseConnectionException {
        logger.info("Creating and testing MySQL connection");
        DatabaseConnection connection = createConnection(config);
        connection.testConnection();
        return connection;
    }

    /**
     * Get MySQL provider for factory registration
     */
    public static com.databasebackup.database.DatabaseConnectionFactory.ConnectionProvider getMySQLProvider() {
        return MySQLConnectionFactory::createConnection;
    }
}
