package com.databasebackup.database;

import com.databasebackup.exception.DatabaseConnectionException;
import java.sql.Connection;
import java.util.Map;

/**
 * Abstract interface for database connections.
 * Provides a unified API for interacting with different database types.
 */
public interface DatabaseConnection {

    /**
     * Get the database type
     */
    DatabaseType getDatabaseType();

    /**
     * Get the connection configuration
     */
    ConnectionConfig getConfig();

    /**
     * Get the underlying database connection object
     * For SQL databases, this is java.sql.Connection
     * For NoSQL databases, this is the respective driver connection object
     */
    Object getUnderlyingConnection();

    /**
     * Test the database connection
     * @throws DatabaseConnectionException if connection test fails
     */
    void testConnection() throws DatabaseConnectionException;

    /**
     * Check if the connection is still valid
     */
    boolean isValid();

    /**
     * Close the database connection
     */
    void close();

    /**
     * Validate database credentials
     * @throws DatabaseConnectionException if validation fails
     */
    void validateCredentials() throws DatabaseConnectionException;

    /**
     * Get database version information
     */
    String getDatabaseVersion() throws DatabaseConnectionException;

    /**
     * Get additional database metadata
     */
    Map<String, String> getMetadata() throws DatabaseConnectionException;

    /**
     * Reconnect to the database
     */
    void reconnect() throws DatabaseConnectionException;

    /**
     * Get connection pool information
     */
    String getConnectionPoolInfo();
}
