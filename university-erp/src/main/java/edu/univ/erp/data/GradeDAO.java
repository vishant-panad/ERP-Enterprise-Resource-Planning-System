package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GradeDAO {

    private static final Logger LOGGER = Logger.getLogger(GradeDAO.class.getName());

    private static final String UPSERT_GRADE = 
        "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE score = VALUES(score)";

    private static final String SELECT_BY_ENROLLMENT = 
        "SELECT * FROM grades WHERE enrollment_id = ?";

    private static final String UPDATE_FINAL_GRADE = 
        "UPDATE grades SET final_grade = ? WHERE enrollment_id = ?";

    /**
     * FIX: Implemented logic to return all grade components for a specific student's enrollment.
     */
    public List<Grade> listByEnrollment(int enrollmentId) {
        List<Grade> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ENROLLMENT)) {
            
            stmt.setInt(1, enrollmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grade g = new Grade();
                    g.setGradeId(rs.getInt("grade_id"));
                    g.setEnrollmentId(rs.getInt("enrollment_id"));
                    g.setComponent(rs.getString("component"));
                    g.setScore(rs.getDouble("score"));
                    g.setFinalGrade(rs.getString("final_grade"));
                    list.add(g);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing grades", e);
        }
        return list;
    }

    // Used by Instructors to enter scores
    public boolean saveScore(int enrollmentId, String component, double score) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPSERT_GRADE)) {
            stmt.setInt(1, enrollmentId);
            stmt.setString(2, component);
            stmt.setDouble(3, score);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * FIX: Implemented logic to update the final letter grade.
     * Note: In many schemas, final grade might be stored in 'enrollments' table or 'grades' table.
     * Your schema has 'final_grade' in the 'grades' table, but usually, a final grade applies to the enrollment,
     * not a specific component row.
     * * However, based on your schema: "final_grade VARCHAR(5) NULL COMMENT 'Letter grade'" is in table 'grades'.
     * This implies we might need to update *all* rows for that enrollment, or insert a specific 'Final' row.
     * * Strategy used here: We update ALL rows for this enrollment to have the same final grade, 
     * or we expect a specific row. Given the ambiguity, I will update all rows for that enrollment 
     * so it appears regardless of which component you look at.
     */
    public boolean updateFinalGrade(int enrollmentId, String finalGrade) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_FINAL_GRADE)) {
            
            stmt.setString(1, finalGrade);
            stmt.setInt(2, enrollmentId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating final grade", e);
            return false;
        }
    }
}