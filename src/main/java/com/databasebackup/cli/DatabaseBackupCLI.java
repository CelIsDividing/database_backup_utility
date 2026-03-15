package com.databasebackup.cli;

import picocli.commandline.Command;
import picocli.commandline.CommandLine;
import picocli.commandline.HelpCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Backup Utility - A comprehensive CLI tool for backing up multiple database types.
 * 
 * Supports various DBMS such as MySQL, PostgreSQL, MongoDB, SQLite, and others.
 * Features include automatic scheduling, compression, cloud storage integration, and logging.
 */
@Command(
    name = "db-backup",
    version = "Database Backup Utility 1.0.0",
    description = "A comprehensive CLI utility for backing up multiple database types with cloud storage support",
    subcommands = {TestConnectionCommand.class, HelpCommand.class},
    mixinStandardHelpOptions = true,
    sortOptions = false
)
public class DatabaseBackupCLI implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupCLI.class);

    @Override
    public void run() {
        // Display help message when no subcommand is provided
        System.out.println("Database Backup Utility v1.0.0");
        System.out.println("Use 'db-backup --help' for usage information");
    }

    /**
     * Main entry point for the CLI application
     */
    public static void main(String[] args) {
        logger.info("Starting Database Backup Utility");
        
        try {
            DatabaseBackupCLI cli = new DatabaseBackupCLI();
            CommandLine commandLine = new CommandLine(cli);
            
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } catch (Exception e) {
            logger.error("Error running Database Backup Utility", e);
            System.exit(1);
        }
    }
}
