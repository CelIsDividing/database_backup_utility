package com.databasebackup.database;

import com.databasebackup.exception.InvalidConnectionConfigException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration object for database connections.
 * Holds all necessary parameters for establishing a connection to a database.
 */
public class ConnectionConfig {

    private final DatabaseType databaseType;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final Map<String, String> additionalProperties;
    private final int connectionTimeoutSeconds;
    private final boolean sslEnabled;

    private ConnectionConfig(Builder builder) {
        this.databaseType = builder.databaseType;
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.password = builder.password;
        this.database = builder.database;
        this.additionalProperties = new HashMap<>(builder.additionalProperties);
        this.connectionTimeoutSeconds = builder.connectionTimeoutSeconds;
        this.sslEnabled = builder.sslEnabled;
    }

    // Getters
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public Map<String, String> getAdditionalProperties() {
        return new HashMap<>(additionalProperties);
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "databaseType=" + databaseType +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", database='" + database + '\'' +
                ", sslEnabled=" + sslEnabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionConfig that = (ConnectionConfig) o;
        return port == that.port &&
                connectionTimeoutSeconds == that.connectionTimeoutSeconds &&
                sslEnabled == that.sslEnabled &&
                databaseType == that.databaseType &&
                Objects.equals(host, that.host) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(database, that.database);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseType, host, port, username, database, connectionTimeoutSeconds, sslEnabled);
    }

    /**
     * Builder class for ConnectionConfig
     */
    public static class Builder {
        private DatabaseType databaseType;
        private String host;
        private int port = -1;
        private String username;
        private String password;
        private String database;
        private Map<String, String> additionalProperties = new HashMap<>();
        private int connectionTimeoutSeconds = 30;
        private boolean sslEnabled = false;

        public Builder(DatabaseType databaseType) {
            this.databaseType = databaseType;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder addProperty(String key, String value) {
            this.additionalProperties.put(key, value);
            return this;
        }

        public Builder connectionTimeoutSeconds(int timeout) {
            this.connectionTimeoutSeconds = timeout;
            return this;
        }

        public Builder sslEnabled(boolean enabled) {
            this.sslEnabled = enabled;
            return this;
        }

        /**
         * Build and validate the ConnectionConfig
         * @return validated ConnectionConfig
         * @throws InvalidConnectionConfigException if validation fails
         */
        public ConnectionConfig build() throws InvalidConnectionConfigException {
            if (databaseType == null) {
                throw new InvalidConnectionConfigException("databaseType", "Database type is required");
            }

            if (host == null || host.isEmpty()) {
                throw new InvalidConnectionConfigException("host", "Host is required");
            }

            if (port == -1) {
                port = databaseType.getDefaultPort();
            }

            if (port <= 0 || port > 65535) {
                throw new InvalidConnectionConfigException("port", 
                    "Port must be between 1 and 65535, got: " + port);
            }

            if (username == null || username.isEmpty()) {
                throw new InvalidConnectionConfigException("username", "Username is required");
            }

            if (password == null) {
                password = "";
            }

            if (database == null || database.isEmpty()) {
                // Some databases like SQLite don't require a database name
                if (!databaseType.equals(DatabaseType.SQLITE)) {
                    throw new InvalidConnectionConfigException("database", "Database name is required");
                }
                database = "";
            }

            if (connectionTimeoutSeconds <= 0) {
                throw new InvalidConnectionConfigException("connectionTimeoutSeconds", 
                    "Connection timeout must be positive");
            }

            return new ConnectionConfig(this);
        }
    }
}
