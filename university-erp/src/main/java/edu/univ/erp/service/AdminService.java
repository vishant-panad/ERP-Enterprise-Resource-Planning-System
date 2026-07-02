package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.DatabaseManager;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.Result;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminService {

    // --- SQL Constants ---
    private static final String INSERT_AUTH_SQL = 
        "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
    
    private static final String INSERT_STUDENT_PROFILE_SQL = 
        "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
    
    private static final String INSERT_INSTRUCTOR_PROFILE_SQL = 
        "INSERT INTO instructors (user_id, department) VALUES (?, ?)";

    private static final String TOGGLE_MAINTENANCE_SQL = 
        "INSERT INTO settings (`key`, `value`) VALUES ('maintenance_on', ?) " +
        "ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";

    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;

    public AdminService(CourseDAO courseDAO, SectionDAO sectionDAO) {
        this.courseDAO = courseDAO;
        this.sectionDAO = sectionDAO;
    }

   
    public Result<Void> setMaintenanceMode(int adminId, boolean enable) {
        if (!AccessChecker.isAllowed(adminId, "MANAGE_SYSTEM")) {
            return Result.error("Access denied. Admin rights required.");
        }

        String newValue = enable ? "true" : "false";

        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(TOGGLE_MAINTENANCE_SQL)) {
            
            stmt.setString(1, newValue);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                return Result.success("Maintenance mode set to: " + newValue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Result.error("Failed to update maintenance settings.");
    }

   
    public Result<Integer> createStudent(int adminId, String username, String rawPassword, 
                                      String rollNo, String program, int year) {
        if (!AccessChecker.isAllowed(adminId, "ADD_USER")) return Result.error("Access denied.");
        
        return createUserTransaction(username, rawPassword, "STUDENT", (userId, erpConn) -> {
            try (PreparedStatement stmt = erpConn.prepareStatement(INSERT_STUDENT_PROFILE_SQL)) {
                stmt.setInt(1, userId);
                stmt.setString(2, rollNo);
                stmt.setString(3, program);
                stmt.setInt(4, year);
                stmt.executeUpdate();
            }
        });
    }

    public Result<Integer> createInstructor(int adminId, String username, String rawPassword, String department) {
        if (!AccessChecker.isAllowed(adminId, "ADD_USER")) return Result.error("Access denied.");
        
        return createUserTransaction(username, rawPassword, "INSTRUCTOR", (userId, erpConn) -> {
            try (PreparedStatement stmt = erpConn.prepareStatement(INSERT_INSTRUCTOR_PROFILE_SQL)) {
                stmt.setInt(1, userId);
                stmt.setString(2, department);
                stmt.executeUpdate();
            }
        });
    }

   
    public Result<Integer> createCourse(int adminId, String code, String title, int credits) {
        if (!AccessChecker.isAllowed(adminId, "MANAGE_COURSES")) return Result.error("Access denied.");

        Course c = new Course();
        c.setCode(code);
        c.setTitle(title);
        c.setCredits(credits);

        if (courseDAO.create(c)) {
            return Result.success("Course created successfully", c.getCourseId());
        }
        return Result.error("Failed to create course. Code might be duplicate.");
    }

    
    public Result<Integer> createSection(int adminId, int courseId, String dayTime, String room, int capacity, String semester, int year) {
        if (!AccessChecker.isAllowed(adminId, "MANAGE_SECTIONS")) return Result.error("Access denied.");

        Section s = new Section();
        s.setCourseId(courseId);
        s.setDayTime(dayTime);
        s.setRoom(room);
        s.setCapacity(capacity);
        s.setSemester(semester);
        s.setYear(year);
        s.setInstructorId(null); 

        if (sectionDAO.create(s)) {
            // FIX: Return the new Section ID
            return Result.success("Section created successfully", s.getSectionId());
        }
        return Result.error("Failed to create section. Check for conflicts.");
    }

    
    public Result<Void> assignInstructor(int adminId, int sectionId, int instructorId) {
        if (!AccessChecker.isAllowed(adminId, "ASSIGN_INSTRUCTOR")) return Result.error("Access denied.");

        if (sectionDAO.assignInstructor(sectionId, instructorId)) {
            return Result.success("Instructor assigned successfully.");
        }
        return Result.error("Failed to assign instructor.");
    }

    
    @FunctionalInterface
    private interface ProfileCreator {
        void createProfile(int userId, Connection erpConn) throws SQLException;
    }

    private Result<Integer> createUserTransaction(String username, String rawPassword, String role, ProfileCreator profileCreator) {
        Connection authConn = null;
        Connection erpConn = null;

        try {
            authConn = DatabaseManager.getAuthConnection();
            erpConn = DatabaseManager.getErpConnection();
            
            authConn.setAutoCommit(false);
            erpConn.setAutoCommit(false);

            // 1. Auth Insert
            int userId = -1;
            String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            
            try (PreparedStatement stmt = authConn.prepareStatement(INSERT_AUTH_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, role);
                stmt.setString(3, hash);
                stmt.executeUpdate();
                
                var rs = stmt.getGeneratedKeys();
                if (rs.next()) userId = rs.getInt(1);
            }

            if (userId == -1) throw new SQLException("Failed to create auth user.");

            profileCreator.createProfile(userId, erpConn);

            authConn.commit();
            erpConn.commit();
            
            return Result.success(role + " created successfully", userId);

        } catch (SQLException e) {
            rollback(authConn);
            rollback(erpConn);
            return Result.error("Error creating user: " + e.getMessage());
        } finally {
            resetAutoCommit(authConn);
            resetAutoCommit(erpConn);
        }
    }
    
    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
    
    private void resetAutoCommit(Connection conn) {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}