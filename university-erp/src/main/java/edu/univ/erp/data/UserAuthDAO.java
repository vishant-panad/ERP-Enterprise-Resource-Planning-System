package edu.univ.erp.data;

import edu.univ.erp.domain.UserAuth;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAuthDAO {
    private static final Logger LOGGER = Logger.getLogger(UserAuthDAO.class.getName());

    private static final String SELECT_BY_USERNAME = 
        "SELECT user_id, username, role, password_hash, status FROM users_auth WHERE username = ?";
    
    private static final String UPDATE_LAST_LOGIN = 
        "UPDATE users_auth SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

    
    public UserAuth findByUsername(String username) {
        try (Connection conn = DatabaseManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user auth", e);
        }
        return null;
    }

    /**
     * Updates the last_login timestamp upon successful login.
     */
    public void updateLastLogin(int userId) {
        try (Connection conn = DatabaseManager.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating last login", e);
        }
    }

    private UserAuth mapRow(ResultSet rs) throws SQLException {
        UserAuth u = new UserAuth();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setRole(rs.getString("role"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setStatus(rs.getString("status"));
        return u;
    }
}