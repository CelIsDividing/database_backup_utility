package com.databasebackup.database;

import com.databasebackup.exception.DatabaseConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating database connections.
 * Implements the factory pattern to provide database-specific connection implementations.
 */
public class DatabaseConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionFactory.class);

    private static final Map<DatabaseType, ConnectionProvider> providers = new HashMap<>();

    /**
     * Functional interface for creating database connections
     */
    @FunctionalInterface
    public interface ConnectionProvider {
        DatabaseConnection createConnection(ConnectionConfig config) throws DatabaseConnectionException;
    }

    /**
     * Register a connection provider for a database type
     */
    public static void registerProvider(DatabaseType type, ConnectionProvider provider) {
        providers.put(type, provider);
        logger.debug("Registered connection provider for {}", type);
    }

    /**
     * Create a database connection using the appropriate driver
     *
     * @param config the connection configuration
     * @return a DatabaseConnection instance
     * @throws DatabaseConnectionException if connection creation fails
     */
    public static DatabaseConnection createConnection(ConnectionConfig config) 
            throws DatabaseConnectionException {
        if (config == null) {
            throw new DatabaseConnectionException("Connection config cannot be null");
        }

        DatabaseType type = config.getDatabaseType();
        logger.info("Creating connection for {} database: {}:{}", 
            type.getDisplayName(), config.getHost(), config.getPort());

        ConnectionProvider provider = providers.get(type);
        if (provider == null) {
            throw new DatabaseConnectionException(
                String.format("No connection provider registered for database type: %s. " +
                    "Supported types: %s", type, DatabaseType.getSupportedTypes()));
        }

        try {
            DatabaseConnection connection = provider.createConnection(config);
            logger.info("Successfully created connection to {} database", type.getDisplayName());
            return connection;
        } catch (DatabaseConnectionException e) {
            logger.error("Failed to create connection to {} database at {}:{}", 
                type.getDisplayName(), config.getHost(), config.getPort(), e);
            throw e;
        } catch (Exception e) {
            throw new DatabaseConnectionException(
                type.getDisplayName(), 
                config.getHost(), 
                config.getPort(),
                "Unexpected error creating connection: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Create a connection and test it immediately
     *
     * @param config the connection configuration
     * @return a tested DatabaseConnection instance
     * @throws DatabaseConnectionException if connection creation or testing fails
     */
    public static DatabaseConnection createAndTestConnection(ConnectionConfig config) 
            throws DatabaseConnectionException {
        logger.info("Creating and testing connection to {}:{}", config.getHost(), config.getPort());
        
        DatabaseConnection connection = createConnection(config);
        try {
            connection.testConnection();
            logger.info("Connection test successful for {} database", config.getDatabaseType());
            return connection;
        } catch (DatabaseConnectionException e) {
            logger.error("Connection test failed", e);
            try {
                connection.close();
            } catch (Exception closeError) {
                logger.warn("Error closing failed connection", closeError);
            }
            throw e;
        }
    }

    /**
     * Get list of registered database types
     */
    public static String[] getRegisteredTypes() {
        return providers.keySet().stream()
            .map(DatabaseType::name)
            .toArray(String[]::new);
    }

    /**
     * Check if a database type is registered
     */
    public static boolean isTypeSupported(DatabaseType type) {
        return providers.containsKey(type);
    }

    /**
     * Clear all registered providers (useful for testing)
     */
    public static void clearProviders() {
        providers.clear();
        logger.debug("All connection providers cleared");
    }
}
