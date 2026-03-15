package com.databasebackup.utils;

import com.databasebackup.database.ConnectionConfig;
import com.databasebackup.exception.InvalidConnectionConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Utility class for validating database connection parameters.
 * Provides various validation methods for connection configuration.
 */
public class ConnectionValidator {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionValidator.class);

    private static final Pattern HOSTNAME_PATTERN = 
        Pattern.compile("^(([a-zA-Z0-9](-?[a-zA-Z0-9])*.)*[a-zA-Z0-9](-?[a-zA-Z0-9])*|localhost|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
    
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final int MIN_USERNAME_LENGTH = 1;
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final int MIN_DATABASE_NAME_LENGTH = 1;
    private static final int MAX_DATABASE_NAME_LENGTH = 255;

    /**
     * Validate entire connection configuration
     */
    public static void validate(ConnectionConfig config) throws InvalidConnectionConfigException {
        if (config == null) {
            throw new InvalidConnectionConfigException("config", "Connection config cannot be null");
        }

        validateHost(config.getHost());
        validatePort(config.getPort());
        validateUsername(config.getUsername());
        validateDatabase(config.getDatabase());
        validateTimeout(config.getConnectionTimeoutSeconds());
    }

    /**
     * Validate hostname/IP address
     */
    public static void validateHost(String host) throws InvalidConnectionConfigException {
        if (host == null || host.isEmpty()) {
            throw new InvalidConnectionConfigException("host", "Host cannot be null or empty");
        }

        if (host.length() > 255) {
            throw new InvalidConnectionConfigException("host", 
                "Host name too long (max 255 characters)");
        }

        if (!HOSTNAME_PATTERN.matcher(host).matches()) {
            throw new InvalidConnectionConfigException("host",
                "Invalid hostname or IP address format: " + host);
        }

        logger.debug("Host validation passed: {}", host);
    }

    /**
     * Validate port number
     */
    public static void validatePort(int port) throws InvalidConnectionConfigException {
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new InvalidConnectionConfigException("port",
                String.format("Port must be between %d and %d, got: %d", 
                    MIN_PORT, MAX_PORT, port));
        }

        logger.debug("Port validation passed: {}", port);
    }

    /**
     * Validate username
     */
    public static void validateUsername(String username) throws InvalidConnectionConfigException {
        if (username == null || username.isEmpty()) {
            throw new InvalidConnectionConfigException("username", 
                "Username cannot be null or empty");
        }

        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidConnectionConfigException("username",
                String.format("Username length must be between %d and %d characters",
                    MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH));
        }

        logger.debug("Username validation passed");
    }

    /**
     * Validate database name
     */
    public static void validateDatabase(String database) throws InvalidConnectionConfigException {
        if (database == null) {
            throw new InvalidConnectionConfigException("database", "Database name cannot be null");
        }

        if (!database.isEmpty()) {
            if (database.length() > MAX_DATABASE_NAME_LENGTH) {
                throw new InvalidConnectionConfigException("database",
                    String.format("Database name too long (max %d characters)", 
                        MAX_DATABASE_NAME_LENGTH));
            }

            // Check for invalid characters
            if (!database.matches("^[a-zA-Z0-9_\\-]*$")) {
                throw new InvalidConnectionConfigException("database",
                    "Database name contains invalid characters. Only alphanumeric, underscore, and hyphen allowed");
            }
        }

        logger.debug("Database validation passed");
    }

    /**
     * Validate connection timeout
     */
    public static void validateTimeout(int timeoutSeconds) throws InvalidConnectionConfigException {
        if (timeoutSeconds <= 0) {
            throw new InvalidConnectionConfigException("connectionTimeout",
                "Connection timeout must be positive");
        }

        if (timeoutSeconds > 3600) {
            logger.warn("Connection timeout is very large: {} seconds", timeoutSeconds);
        }

        logger.debug("Timeout validation passed: {} seconds", timeoutSeconds);
    }

    /**
     * Validate password strength (optional, can be disabled)
     */
    public static void validatePasswordStrength(String password) throws InvalidConnectionConfigException {
        if (password == null) {
            throw new InvalidConnectionConfigException("password", "Password cannot be null");
        }

        if (password.isEmpty()) {
            logger.warn("Empty password detected - ensure this is intentional");
        }

        if (password.length() > 1000) {
            throw new InvalidConnectionConfigException("password",
                "Password is too long (max 1000 characters)");
        }
    }

    /**
     * Check if hostname is localhost
     */
    public static boolean isLocalhost(String host) {
        return host != null && (host.equals("localhost") || 
                                host.equals("127.0.0.1") || 
                                host.equals("::1"));
    }

    /**
     * Check if IP address is private/internal
     */
    public static boolean isPrivateIP(String host) {
        if (host == null) {
            return false;
        }

        // Check for localhost
        if (isLocalhost(host)) {
            return true;
        }

        // Try to parse as IP and check private ranges
        try {
            String[] parts = host.split("\\.");
            if (parts.length == 4) {
                int[] octets = new int[4];
                for (int i = 0; i < 4; i++) {
                    octets[i] = Integer.parseInt(parts[i]);
                }

                // 10.0.0.0 - 10.255.255.255
                if (octets[0] == 10) return true;

                // 172.16.0.0 - 172.31.255.255
                if (octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31) return true;

                // 192.168.0.0 - 192.168.255.255
                if (octets[0] == 192 && octets[1] == 168) return true;
            }
        } catch (NumberFormatException e) {
            logger.debug("Could not parse IP address: {}", host);
        }

        return false;
    }
}
