package com.databasebackup.cli;

import com.databasebackup.database.ConnectionConfig;
import com.databasebackup.database.DatabaseConnectionFactory;
import com.databasebackup.database.DatabaseType;
import com.databasebackup.exception.InvalidConnectionConfigException;
import com.databasebackup.utils.ConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * CLI command for testing database connections.
 * Validates connection parameters and tests connectivity to the specified database.
 */
@Command(
    name = "test-connection",
    description = "Test database connection with provided credentials",
    mixinStandardHelpOptions = true
)
public class TestConnectionCommand implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TestConnectionCommand.class);

    @Option(
        names = {"-t", "--type"},
        description = "Database type: ${COMPLETION-CANDIDATES}",
        required = true,
        paramLabel = "TYPE"
    )
    private String databaseType;

    @Option(
        names = {"-h", "--host"},
        description = "Database host address",
        required = true,
        paramLabel = "HOST"
    )
    private String host;

    @Option(
        names = {"-p", "--port"},
        description = "Database port (default: database type default)",
        paramLabel = "PORT"
    )
    private Integer port;

    @Option(
        names = {"-u", "--username"},
        description = "Database username",
        required = true,
        paramLabel = "USERNAME"
    )
    private String username;

    @Option(
        names = {"-P", "--password"},
        description = "Database password (prompt if not provided)",
        paramLabel = "PASSWORD"
    )
    private String password;

    @Option(
        names = {"-d", "--database"},
        description = "Database name",
        paramLabel = "DATABASE"
    )
    private String database;

    @Option(
        names = {"--ssl"},
        description = "Enable SSL connection",
        defaultValue = "false"
    )
    private boolean sslEnabled;

    @Option(
        names = {"--timeout"},
        description = "Connection timeout in seconds (default: 30)",
        defaultValue = "30",
        paramLabel = "SECONDS"
    )
    private int timeoutSeconds;

    @Override
    public void run() {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
            System.out.println("║           Testing Database Connection                              ║");
            System.out.println("╚════════════════════════════════════════════════════════════════════╝\n");

            // Parse and validate database type
            DatabaseType dbType = parseDatabaseType();

            // Build connection config
            ConnectionConfig config = buildConnectionConfig(dbType);

            // Validate configuration
            logger.info("Validating connection configuration");
            ConnectionValidator.validate(config);
            System.out.println("✓ Configuration validation passed");

            // Resolve password if needed
            if (password == null || password.isEmpty()) {
                password = promptForPassword();
                // Rebuild config with password
                config = buildConnectionConfig(dbType);
            }

            // Print connection details
            printConnectionDetails(config);

            // Create and test connection
            logger.info("Testing connection to {}:{}", host, port != null ? port : dbType.getDefaultPort());
            System.out.println("\n[INFO] Attempting to connect...");

            try {
                DatabaseConnectionFactory.createAndTestConnection(config);
                System.out.println("✓ Connection test SUCCESSFUL!");
                System.out.println("\n[SUCCESS] Database connection is working correctly.\n");
            } catch (Exception e) {
                System.out.println("✗ Connection test FAILED!");
                System.out.println("\n[ERROR] " + e.getMessage() + "\n");
                logger.error("Connection test failed", e);
                System.exit(1);
            }

        } catch (InvalidConnectionConfigException e) {
            System.out.println("✗ Invalid configuration!");
            System.out.println("\n[ERROR] " + e.getMessage() + "\n");
            logger.error("Configuration validation failed", e);
            System.exit(1);
        } catch (Exception e) {
            System.out.println("✗ Unexpected error!");
            System.out.println("\n[ERROR] " + e.getMessage() + "\n");
            logger.error("Unexpected error", e);
            System.exit(1);
        }
    }

    private DatabaseType parseDatabaseType() throws InvalidConnectionConfigException {
        try {
            return DatabaseType.fromString(databaseType);
        } catch (IllegalArgumentException e) {
            throw new InvalidConnectionConfigException("type", e.getMessage());
        }
    }

    private ConnectionConfig buildConnectionConfig(DatabaseType dbType) 
            throws InvalidConnectionConfigException {
        ConnectionConfig.Builder builder = new ConnectionConfig.Builder(dbType)
            .host(host)
            .username(username)
            .password(password != null ? password : "")
            .connectionTimeoutSeconds(timeoutSeconds)
            .sslEnabled(sslEnabled);

        if (port != null) {
            builder.port(port);
        }

        if (database != null && !database.isEmpty()) {
            builder.database(database);
        } else if (!dbType.equals(DatabaseType.SQLITE)) {
            builder.database(databaseType.toLowerCase());
        }

        return builder.build();
    }

    private String promptForPassword() {
        System.out.print("[INPUT] Enter database password: ");
        char[] passwordArray = System.console().readPassword();
        return new String(passwordArray);
    }

    private void printConnectionDetails(ConnectionConfig config) {
        System.out.println("Connection Details:");
        System.out.println("  Database Type  : " + config.getDatabaseType().getDisplayName());
        System.out.println("  Host           : " + config.getHost());
        System.out.println("  Port           : " + config.getPort());
        System.out.println("  Username       : " + config.getUsername());
        System.out.println("  Database       : " + (config.getDatabase().isEmpty() ? "(default)" : config.getDatabase()));
        System.out.println("  SSL Enabled    : " + config.isSslEnabled());
        System.out.println("  Timeout        : " + config.getConnectionTimeoutSeconds() + "s");
    }
}
