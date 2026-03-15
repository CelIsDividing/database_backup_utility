package com.databasebackup.database;

/**
 * Enumeration of supported database management systems (DBMS).
 * Each type maps to a specific database driver and connection handler.
 */
public enum DatabaseType {
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", 3306),
    POSTGRESQL("PostgreSQL", "org.postgresql.Driver", 5432),
    MONGODB("MongoDB", "mongodb", 27017),
    SQLITE("SQLite", "org.sqlite.JDBC", 0),
    MARIADB("MariaDB", "org.mariadb.jdbc.Driver", 3306),
    ORACLE("Oracle", "oracle.jdbc.driver.OracleDriver", 1521),
    SQLSERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", 1433);

    private final String displayName;
    private final String driverClass;
    private final int defaultPort;

    DatabaseType(String displayName, String driverClass, int defaultPort) {
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.defaultPort = defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Parse database type from string
     * @param type database type as string (case-insensitive)
     * @return DatabaseType enum
     * @throws IllegalArgumentException if type is not supported
     */
    public static DatabaseType fromString(String type) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }
        
        try {
            return DatabaseType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported database type: " + type + 
                ". Supported types: " + getSupportedTypes());
        }
    }

    /**
     * Get comma-separated list of supported database types
     */
    public static String getSupportedTypes() {
        StringBuilder sb = new StringBuilder();
        for (DatabaseType type : DatabaseType.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.name().toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Check if database requires a port number
     */
    public boolean requiresPort() {
        return this != SQLITE;
    }

    /**
     * Check if this is a NoSQL database
     */
    public boolean isNoSQL() {
        return this == MONGODB;
    }
}
