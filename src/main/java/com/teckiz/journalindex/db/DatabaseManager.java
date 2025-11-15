package com.teckiz.journalindex.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lightweight database connection manager using HikariCP
 * No Spring Framework dependencies
 */
public class DatabaseManager {
    
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private static volatile HikariDataSource dataSource;
    private static final Object initLock = new Object();
    
    /**
     * Initialize database connection pool lazily
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (initLock) {
                if (dataSource == null) {
                    try {
                        logger.info("=== INITIALIZING DATABASE CONNECTION POOL ===");
                        
                        // Get configuration from environment variables
                        String dbUrl = System.getenv("DB_URL");
                        String dbUsername = System.getenv("DB_USERNAME");
                        String dbPassword = System.getenv("DB_PASSWORD");
                        String mysqlHost = System.getenv("MYSQL_HOST");
                        String mysqlPort = System.getenv("MYSQL_PORT");
                        String mysqlDatabase = System.getenv("MYSQL_DATABASE");
                        String mysqlSslMode = System.getenv("MYSQL_SSL_MODE");
                        String mysqlConnectionTimeout = System.getenv("MYSQL_CONNECTION_TIMEOUT");
                        String mysqlSocketTimeout = System.getenv("MYSQL_SOCKET_TIMEOUT");
                        
                        // Defaults
                        if (mysqlPort == null || mysqlPort.isEmpty()) {
                            mysqlPort = "3306";
                        }
                        if (mysqlDatabase == null || mysqlDatabase.isEmpty()) {
                            mysqlDatabase = "teckiz_test";
                        }
                        if (mysqlSslMode == null || mysqlSslMode.isEmpty()) {
                            mysqlSslMode = "REQUIRED";
                        }
                        if (mysqlConnectionTimeout == null || mysqlConnectionTimeout.isEmpty()) {
                            mysqlConnectionTimeout = "30000";
                        }
                        if (mysqlSocketTimeout == null || mysqlSocketTimeout.isEmpty()) {
                            mysqlSocketTimeout = "30000";
                        }
                        
                        // Extract host from DB_URL if MYSQL_HOST is not set
                        String hostToUse = mysqlHost;
                        if (hostToUse == null || hostToUse.isBlank()) {
                            if (dbUrl != null && !dbUrl.isBlank()) {
                                if (dbUrl.startsWith("jdbc:mysql://")) {
                                    // Extract host from JDBC URL
                                    try {
                                        String urlWithoutPrefix = dbUrl.substring("jdbc:mysql://".length());
                                        int colonIndex = urlWithoutPrefix.indexOf(':');
                                        if (colonIndex > 0) {
                                            hostToUse = urlWithoutPrefix.substring(0, colonIndex);
                                        }
                                    } catch (Exception e) {
                                        logger.warn("Could not extract host from DB_URL, using as-is");
                                        hostToUse = dbUrl;
                                    }
                                } else {
                                    // DB_URL is just the host name
                                    hostToUse = dbUrl;
                                }
                            }
                        }
                        
                        // Build JDBC URL
                        String finalUrl = dbUrl;
                        if (finalUrl == null || !finalUrl.startsWith("jdbc:")) {
                            // Convert SSL mode to proper useSSL value
                            String useSslValue = "TRUE";
                            if (mysqlSslMode != null && !mysqlSslMode.isBlank()) {
                                String sslModeUpper = mysqlSslMode.toUpperCase();
                                if ("FALSE".equals(sslModeUpper) || "NO".equals(sslModeUpper) || "DISABLED".equals(sslModeUpper)) {
                                    useSslValue = "FALSE";
                                }
                            }
                            
                            finalUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=%s&requireSSL=%s&serverTimezone=UTC&connectTimeout=%s&socketTimeout=%s",
                                    hostToUse, 
                                    mysqlPort, 
                                    mysqlDatabase, 
                                    useSslValue,
                                    "REQUIRED".equalsIgnoreCase(mysqlSslMode) ? "TRUE" : "FALSE",
                                    mysqlConnectionTimeout, 
                                    mysqlSocketTimeout);
                        }
                        
                        logger.info("Connecting to database: {} (host: {})", mysqlDatabase, hostToUse);
                        
                        // Configure HikariCP
                        HikariConfig config = new HikariConfig();
                        config.setJdbcUrl(finalUrl);
                        config.setUsername(dbUsername);
                        config.setPassword(dbPassword);
                        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                        
                        // Optimized for Lambda
                        config.setMinimumIdle(0);  // Don't create connections during initialization
                        config.setMaximumPoolSize(5);
                        config.setConnectionTimeout(5000);  // 5 seconds
                        config.setIdleTimeout(300000);  // 5 minutes
                        config.setMaxLifetime(600000);  // 10 minutes
                        config.setConnectionTestQuery("SELECT 1");
                        config.setInitializationFailTimeout(-1);  // Don't fail if pool can't initialize immediately
                        config.setRegisterMbeans(false);  // Disable JMX
                        
                        dataSource = new HikariDataSource(config);
                        logger.info("✅ Database connection pool initialized successfully");
                        
                    } catch (Exception e) {
                        logger.error("Failed to initialize database connection pool", e);
                        throw new RuntimeException("Failed to initialize database connection pool: " + e.getMessage(), e);
                    }
                }
            }
        }
        return dataSource;
    }
    
    /**
     * Get a connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    /**
     * Close the connection pool (for cleanup if needed)
     */
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}

