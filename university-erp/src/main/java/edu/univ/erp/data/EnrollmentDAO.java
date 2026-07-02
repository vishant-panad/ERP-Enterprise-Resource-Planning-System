package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnrollmentDAO {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentDAO.class.getName());

    // --- SQL Constants ---
    private static final String INSERT_SQL = 
        "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
    
    private static final String DELETE_BY_STUDENT_SECTION_SQL = 
    "UPDATE enrollments SET status = 'DROPPED' WHERE student_id = ? AND section_id = ?";
    
    private static final String COUNT_BY_SECTION_SQL = 
        "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'REGISTERED'";
    
    // JOIN Query to get Section details for a student's timetable
    private static final String SELECT_TIMETABLE_SQL = 
        "SELECT s.* FROM sections s " +
        "JOIN enrollments e ON s.section_id = e.section_id " +
        "WHERE e.student_id = ? AND e.status = 'REGISTERED'";

    private static final String SELECT_TRANSCRIPT_SQL = 
        "SELECT DISTINCT c.code, c.title, c.credits, s.semester, s.year, g.final_grade " +
        "FROM enrollments e " +
        "JOIN sections s ON e.section_id = s.section_id " +
        "JOIN courses c ON s.course_id = c.course_id " +
        "JOIN grades g ON e.enrollment_id = g.enrollment_id " +
        "WHERE e.student_id = ? AND g.final_grade IS NOT NULL " +
        "ORDER BY s.year DESC, s.semester DESC";

    
    public static class TranscriptRecord {
        public String courseCode;
        public String courseTitle;
        public int credits;
        public String semester;
        public int year;
        public String finalGrade;
    }

    // --- Methods ---

    
    public boolean register(int studentId, int sectionId) {
        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setSectionId(sectionId);
        e.setStatus("REGISTERED");
        return create(e);
    }


   
    public Result registerTransactional(int studentId, int sectionId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getErpConnection();
            conn.setAutoCommit(false);

            // 1. Lock section row
            final String SELECT_SECTION = "SELECT capacity FROM sections WHERE section_id = ? FOR UPDATE";
            int capacity = -1;
            try (PreparedStatement ps = conn.prepareStatement(SELECT_SECTION)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return Result.error("Section not found.");
                    }
                    capacity = rs.getInt("capacity");
                }
            }

            // 2. Count enrolled
            final String COUNT_SQL = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'REGISTERED' FOR UPDATE";
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement(COUNT_SQL)) {
                ps.setInt(1, sectionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) count = rs.getInt(1);
                }
            }

            if (count >= capacity) {
                conn.rollback();
                return Result.error("Section full.");
            }

            // 3. Insert enrollment
            // Uses "ON DUPLICATE KEY UPDATE" to reactivate a dropped student
            final String INSERT_SQL = 
                "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'REGISTERED') " +
                "ON DUPLICATE KEY UPDATE status = 'REGISTERED'";
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                ps.setInt(1, studentId);
                ps.setInt(2, sectionId);

                ps.executeUpdate();
            }

            conn.commit();
            return Result.success("Successfully registered!");

        } catch (SQLIntegrityConstraintViolationException dup) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}
            return Result.error("You are already registered for this section.");
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignore) {}
            return Result.error("Registration failed due to a database error.");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignore) {}
            }
        }
    }




    public boolean create(Enrollment enrollment) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getSectionId());
            stmt.setString(3, "REGISTERED");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        enrollment.setEnrollmentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("Duplicate enrollment: Student is already registered.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating enrollment", e);
        }
        return false;
    }

    /**
     * Drops a section by student ID and section ID.
     */
    public boolean drop(int studentId, int sectionId) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_STUDENT_SECTION_SQL)) {
            
            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            
            int rows = stmt.executeUpdate();
            return rows > 0; 
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error dropping section", e);
            return false;
        }
    }

    public int countBySection(int sectionId) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_SECTION_SQL)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting enrollments", e);
        }
        return 0;
    }

    
    public List<Section> listSectionsByStudent(int studentId) {
        List<Section> timetable = new ArrayList<>();
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_TIMETABLE_SQL)) {
            
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    timetable.add(mapSectionRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving timetable", e);
        }
        return timetable;
    }

   
    public List<TranscriptRecord> getAcademicRecord(int studentId) {
        List<TranscriptRecord> records = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_TRANSCRIPT_SQL)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TranscriptRecord r = new TranscriptRecord();
                    r.courseCode = rs.getString("code");
                    r.courseTitle = rs.getString("title");
                    r.credits = rs.getInt("credits");
                    r.semester = rs.getString("semester");
                    r.year = rs.getInt("year");
                    r.finalGrade = rs.getString("final_grade");
                    records.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching academic record", e);
        }
        return records;
    }

    // --- Private Helpers ---

    private Section mapSectionRow(ResultSet rs) throws SQLException {
        Section s = new Section();
        s.setSectionId(rs.getInt("section_id"));
        s.setCourseId(rs.getInt("course_id"));
        
        int instId = rs.getInt("instructor_id");
        if (!rs.wasNull()) {
             s.setInstructorId(instId);
        }
        
        s.setDayTime(rs.getString("day_time"));
        s.setRoom(rs.getString("room"));
        s.setCapacity(rs.getInt("capacity"));
        s.setSemester(rs.getString("semester"));
        s.setYear(rs.getInt("year"));
        return s;
    }
}