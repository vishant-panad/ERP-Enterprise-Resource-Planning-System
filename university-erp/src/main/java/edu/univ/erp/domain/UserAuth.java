package edu.univ.erp.domain;

import java.sql.Timestamp;

public class UserAuth {
    private int userId; // Links to profiles in ERP DB
    private String username;
    private String role; // ENUM('ADMIN', 'INSTRUCTOR', 'STUDENT')
    private String passwordHash; // Secure hash (bcrypt)
    private String status;
    private Timestamp lastLogin;

    // Constructors
    public UserAuth() {}

    public UserAuth(int userId, String username, String role, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }
}
