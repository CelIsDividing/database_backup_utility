package com.databasebackup.exception;

/**
 * Exception thrown when database connection operations fail.
 * This includes connection establishment, validation, and connection pool issues.
 */
public class DatabaseConnectionException extends Exception {
    
    private final String databaseType;
    private final String host;
    private final int port;

    /**
     * Constructor with message and cause
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.databaseType = null;
        this.host = null;
        this.port = 0;
    }

    /**
     * Constructor with message only
     */
    public DatabaseConnectionException(String message) {
        super(message);
        this.databaseType = null;
        this.host = null;
        this.port = 0;
    }

    /**
     * Constructor with detailed connection information
     */
    public DatabaseConnectionException(String databaseType, String host, int port, 
                                       String message, Throwable cause) {
        super(formatMessage(databaseType, host, port, message), cause);
        this.databaseType = databaseType;
        this.host = host;
        this.port = port;
    }

    /**
     * Constructor with detailed connection information and no cause
     */
    public DatabaseConnectionException(String databaseType, String host, int port, String message) {
        super(formatMessage(databaseType, host, port, message));
        this.databaseType = databaseType;
        this.host = host;
        this.port = port;
    }

    private static String formatMessage(String databaseType, String host, int port, String message) {
        return String.format("%s connection to %s:%d failed - %s", 
            databaseType, host, port, message);
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
