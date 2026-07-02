package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    // --- Configuration Constants ---
    // MySQL Driver
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    // Database Credentials (Assuming local default setup for development)
    private static final String DB_USER = "root"; // CHANGE THIS if your MySQL user is different
    private static final String DB_PASS = "Vish1250$$"; // CHANGE THIS if you have a different root password
    
    // Auth DB Connection Details
    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/auth_db";
    private static HikariDataSource authDataSource;

    // ERP DB Connection Details
    private static final String ERP_DB_URL = "jdbc:mysql://localhost:3306/erp_db";
    private static HikariDataSource erpDataSource;

    
    public static void initialize() {
        if (authDataSource == null || erpDataSource == null) {
            try {
                // Ensure the driver is loaded
                Class.forName(DRIVER_CLASS);
                
                // Initialize Auth DB Pool
                authDataSource = createDataSource(AUTH_DB_URL, "AuthPool");
                LOGGER.log(Level.INFO, "Auth DB Connection Pool initialized successfully.");

                // Initialize ERP DB Pool
                erpDataSource = createDataSource(ERP_DB_URL, "ErpPool");
                LOGGER.log(Level.INFO, "ERP DB Connection Pool initialized successfully.");

            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found. Please check your Maven dependencies.", e);
                // System exit is appropriate here as the app cannot function without DB access
                System.exit(1); 
            }
        }
    }

    /**
     * Helper method to create and configure a HikariDataSource.
     */
    private static HikariDataSource createDataSource(String url, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASS);
        config.setDriverClassName(DRIVER_CLASS);
        config.setPoolName(poolName);
        config.setMaximumPoolSize(10); // Max connections
        config.setMinimumIdle(5);      // Min idle connections
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }
    
   
    public static Connection getAuthConnection() throws SQLException {
        if (authDataSource == null) {
            initialize();
        }
        return authDataSource.getConnection();
    }

   
    public static Connection getErpConnection() throws SQLException {
        if (erpDataSource == null) {
            initialize();
        }
        return erpDataSource.getConnection();
    }
    
    
    public static void shutdown() {
        if (authDataSource != null) {
            authDataSource.close();
            authDataSource = null;
            LOGGER.log(Level.INFO, "Auth DB Connection Pool shutdown complete.");
        }
        if (erpDataSource != null) {
            erpDataSource.close();
            erpDataSource = null;
            LOGGER.log(Level.INFO, "ERP DB Connection Pool shutdown complete.");
        }
    }
}
