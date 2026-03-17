package com.databasebackup.database.impl;

import com.databasebackup.database.ConnectionConfig;
import com.databasebackup.database.DatabaseType;
import com.databasebackup.exception.InvalidConnectionConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MySQLConnection class
 */
public class MySQLConnectionTest {

    private ConnectionConfig validConfig;

    @BeforeEach
    public void setUp() throws InvalidConnectionConfigException {
        validConfig = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .port(3306)
            .username("root")
            .password("test")
            .database("test_db")
            .build();
    }

    @Test
    public void testCreateMySQLConnection() {
        assertNotNull(validConfig);
        assertEquals(DatabaseType.MYSQL, validConfig.getDatabaseType());
        assertEquals("localhost", validConfig.getHost());
        assertEquals(3306, validConfig.getPort());
        assertEquals("test_db", validConfig.getDatabase());
    }

    @Test
    public void testConnectionConfigWithSSL() throws InvalidConnectionConfigException {
        ConnectionConfig sslConfig = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("db.example.com")
            .port(3306)
            .username("admin")
            .password("secured")
            .database("production")
            .sslEnabled(true)
            .build();

        assertTrue(sslConfig.isSslEnabled());
        assertEquals("db.example.com", sslConfig.getHost());
    }

    @Test
    public void testConnectionConfigWithCustomTimeout() throws InvalidConnectionConfigException {
        ConnectionConfig customConfig = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .username("user")
            .password("pass")
            .database("db")
            .connectionTimeoutSeconds(60)
            .build();

        assertEquals(60, customConfig.getConnectionTimeoutSeconds());
    }

    @Test
    public void testInvalidHostThrowsException() {
        assertThrows(InvalidConnectionConfigException.class, () -> {
            new ConnectionConfig.Builder(DatabaseType.MYSQL)
                .host("")
                .username("user")
                .password("pass")
                .database("db")
                .build();
        });
    }

    @Test
    public void testInvalidPortThrowsException() {
        assertThrows(InvalidConnectionConfigException.class, () -> {
            new ConnectionConfig.Builder(DatabaseType.MYSQL)
                .host("localhost")
                .port(99999)
                .username("user")
                .password("pass")
                .database("db")
                .build();
        });
    }

    @Test
    public void testMissingUsernameThrowsException() {
        assertThrows(InvalidConnectionConfigException.class, () -> {
            new ConnectionConfig.Builder(DatabaseType.MYSQL)
                .host("localhost")
                .port(3306)
                .password("pass")
                .database("db")
                .build();
        });
    }

    @Test
    public void testMissingDatabaseThrowsException() {
        assertThrows(InvalidConnectionConfigException.class, () -> {
            new ConnectionConfig.Builder(DatabaseType.MYSQL)
                .host("localhost")
                .port(3306)
                .username("user")
                .password("pass")
                .build();
        });
    }

    @Test
    public void testMySQLConnectionConfiguration() throws InvalidConnectionConfigException {
        assertEquals(DatabaseType.MYSQL, validConfig.getDatabaseType());
        assertFalse(validConfig.isSslEnabled());
        assertEquals(30, validConfig.getConnectionTimeoutSeconds());
    }

    @Test
    public void testConfigEquality() throws InvalidConnectionConfigException {
        ConnectionConfig config1 = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .port(3306)
            .username("user")
            .password("pass")
            .database("db")
            .build();

        ConnectionConfig config2 = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .port(3306)
            .username("user")
            .password("pass")
            .database("db")
            .build();

        assertEquals(config1, config2);
    }

    @Test
    public void testConfigHashCode() throws InvalidConnectionConfigException {
        ConnectionConfig config1 = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .port(3306)
            .username("user")
            .password("pass")
            .database("db")
            .build();

        ConnectionConfig config2 = new ConnectionConfig.Builder(DatabaseType.MYSQL)
            .host("localhost")
            .port(3306)
            .username("user")
            .password("pass")
            .database("db")
            .build();

        assertEquals(config1.hashCode(), config2.hashCode());
    }
}
