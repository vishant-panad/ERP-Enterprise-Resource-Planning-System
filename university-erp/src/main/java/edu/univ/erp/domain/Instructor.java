package edu.univ.erp.domain;

public class Instructor {
    private int userId; // Foreign Key to users_auth.user_id
    private String department;

    // Constructors
    public Instructor() {}

    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
