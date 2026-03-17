package com.databasebackup.database;

import com.databasebackup.database.impl.MySQLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DatabaseProviderInitializer
 * Registers all database connection providers at application startup.
 * This centralizes the registration of database-specific implementations.
 */
public class DatabaseProviderInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseProviderInitializer.class);
    private static boolean initialized = false;

    /**
     * Initialize all database providers
     * Should be called once at application startup
     */
    public static synchronized void initializeProviders() {
        if (initialized) {
            logger.debug("Database providers already initialized");
            return;
        }

        logger.info("Initializing database connection providers");

        try {
            // Register MySQL provider
            registerMySQLProvider();
            
            // PostgreSQL provider will be registered in future commits
            // registerPostgresqlProvider();
            
            // MongoDB provider will be registered in future commits
            // registerMongoDBProvider();

            initialized = true;
            logger.info("Database connection providers initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize database providers", e);
            throw new RuntimeException("Failed to initialize database providers", e);
        }
    }

    /**
     * Register MySQL connection provider
     */
    private static void registerMySQLProvider() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DatabaseConnectionFactory.registerProvider(
                DatabaseType.MYSQL,
                MySQLConnectionFactory.getMySQLProvider()
            );
            logger.info("MySQL provider registered successfully");
        } catch (ClassNotFoundException e) {
            logger.warn("MySQL driver not available in classpath. MySQL support will be disabled.");
            logger.debug("MySQL ClassNotFoundException", e);
        }
    }

    /**
     * Check if providers are initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset initialization (useful for testing)
     */
    public static void reset() {
        initialized = false;
        logger.debug("Database provider initialization reset");
    }
}
