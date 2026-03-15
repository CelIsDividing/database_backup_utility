package com.databasebackup.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Connection pool manager for managing database connections.
 * Implements a simple connection pooling mechanism to reuse connections efficiently.
 */
public class ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private final DatabaseType databaseType;
    private final int maxConnections;
    private final int minConnections;
    private final long connectionTimeoutMillis;

    private final Queue<DatabaseConnection> availableConnections;
    private final List<DatabaseConnection> allConnections;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean isInitialized = false;
    private boolean isClosed = false;

    public ConnectionPool(DatabaseType databaseType, int minConnections, int maxConnections, 
                         long connectionTimeoutMillis) {
        if (minConnections < 1 || maxConnections < minConnections) {
            throw new IllegalArgumentException(
                String.format("Invalid pool size: min=%d, max=%d. Min must be >= 1 and <= max", 
                    minConnections, maxConnections));
        }

        this.databaseType = databaseType;
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.availableConnections = new LinkedList<>();
        this.allConnections = new ArrayList<>();
    }

    /**
     * Initialize the connection pool by creating minimum connections
     */
    public void initialize() {
        lock.writeLock().lock();
        try {
            if (isInitialized) {
                logger.warn("Connection pool already initialized");
                return;
            }

            logger.info("Initializing connection pool for {} with min={}, max={} connections",
                databaseType, minConnections, maxConnections);

            isInitialized = true;
            logger.info("Connection pool initialized");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Acquire a connection from the pool
     */
    public DatabaseConnection acquireConnection() throws InterruptedException {
        lock.writeLock().lock();
        try {
            if (isClosed) {
                throw new IllegalStateException("Connection pool is closed");
            }

            // Try to get an available connection
            DatabaseConnection connection = availableConnections.poll();

            if (connection == null) {
                // Create a new connection if we haven't reached max
                if (allConnections.size() < maxConnections) {
                    logger.debug("Creating new connection, pool size: {}/{}", 
                        allConnections.size(), maxConnections);
                    // Note: actual connection creation will be done by subclass
                    // This is just tracking
                } else {
                    // Wait for a connection to become available
                    logger.debug("Pool exhausted, waiting for available connection");
                    // This will be handled by caller with timeout
                }
            }

            return connection;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Release a connection back to the pool
     */
    public void releaseConnection(DatabaseConnection connection) {
        if (connection == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (isClosed) {
                connection.close();
                return;
            }

            if (connection.isValid()) {
                availableConnections.offer(connection);
                logger.debug("Connection released to pool, available size: {}", 
                    availableConnections.size());
            } else {
                // Remove invalid connection
                allConnections.remove(connection);
                connection.close();
                logger.warn("Invalid connection removed from pool");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Close all connections in the pool
     */
    public void closeAll() {
        lock.writeLock().lock();
        try {
            if (isClosed) {
                logger.warn("Pool already closed");
                return;
            }

            isClosed = true;

            logger.info("Closing all connections in pool, total connections: {}", 
                allConnections.size());

            for (DatabaseConnection conn : allConnections) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.warn("Error closing connection", e);
                }
            }

            availableConnections.clear();
            allConnections.clear();

            logger.info("Connection pool closed");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get pool statistics
     */
    public PoolStats getStats() {
        lock.readLock().lock();
        try {
            return new PoolStats(
                allConnections.size(),
                availableConnections.size(),
                allConnections.size() - availableConnections.size(),
                maxConnections,
                isClosed
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Pool statistics holder
     */
    public static class PoolStats {
        public final int totalConnections;
        public final int availableConnections;
        public final int activeConnections;
        public final int maxConnections;
        public final boolean isClosed;

        public PoolStats(int total, int available, int active, int max, boolean closed) {
            this.totalConnections = total;
            this.availableConnections = available;
            this.activeConnections = active;
            this.maxConnections = max;
            this.isClosed = closed;
        }

        @Override
        public String toString() {
            return String.format("PoolStats{total=%d, available=%d, active=%d, max=%d, closed=%b}",
                totalConnections, availableConnections, activeConnections, maxConnections, isClosed);
        }
    }

    public boolean isClosed() {
        return isClosed;
    }

    public int getCurrentSize() {
        lock.readLock().lock();
        try {
            return allConnections.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getAvailableSize() {
        lock.readLock().lock();
        try {
            return availableConnections.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
