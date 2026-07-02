package edu.univ.erp.access;

import edu.univ.erp.data.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MaintenanceChecker {
    
    public static boolean isReadOnly() {
        String sql = "SELECT value FROM settings WHERE `key` = 'maintenance_on'";
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to OFF if error
    }
}