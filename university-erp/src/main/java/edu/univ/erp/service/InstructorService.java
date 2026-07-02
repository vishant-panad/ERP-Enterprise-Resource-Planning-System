package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.access.MaintenanceChecker;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.DatabaseManager; 
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstructorService {

    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;

    // --- DTO for Gradebook View ---
    public static class StudentGradeDTO {
        public int enrollmentId;
        public String studentName; // Using Roll No for anonymity/simplicity
        public Double quizScore;
        public Double midtermScore;
        public Double endSemScore;
        public String finalGrade;
        
        public StudentGradeDTO(int enrollmentId, String studentName, Double q, Double m, Double e, String f) {
            this.enrollmentId = enrollmentId;
            this.studentName = studentName;
            this.quizScore = q;
            this.midtermScore = m;
            this.endSemScore = e;
            this.finalGrade = f;
        }
    }

    public InstructorService(SectionDAO sectionDAO, EnrollmentDAO enrollmentDAO, GradeDAO gradeDAO) {
        this.sectionDAO = sectionDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.gradeDAO = gradeDAO;
    }

    public Result<List<Section>> getMySections(int instructorId) {
        if (!AccessChecker.isAllowed(instructorId, "VIEW_MY_SECTIONS")) {
            return Result.error("Access denied.");
        }
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser().getUserId() != instructorId) {
            return Result.error("You can only view your own sections.");
        }
        return Result.success("Sections retrieved.", sectionDAO.getByInstructor(instructorId));
    }

    public Result<Void> updateScore(int instructorId, int enrollmentId, String component, double score) {
        if (!AccessChecker.isAllowed(instructorId, "ENTER_GRADES")) return Result.error("Access denied.");
        if (MaintenanceChecker.isReadOnly()) return Result.error("Maintenance Mode is ON.");
        if (score < 0) return Result.error("Score cannot be negative.");

        boolean success = gradeDAO.saveScore(enrollmentId, component, score);
        return success ? Result.success("Score saved.") : Result.error("Failed to save score.");
    }

    public Result<String> computeFinalGrade(int instructorId, int enrollmentId) {
        if (MaintenanceChecker.isReadOnly()) return Result.error("Maintenance Mode is ON.");

        List<Grade> grades = gradeDAO.listByEnrollment(enrollmentId);
        if (grades == null || grades.isEmpty()) return Result.error("No grades found.");

        Map<String, Double> scores = grades.stream().collect(Collectors.toMap(
                g -> g.getComponent().toLowerCase(), Grade::getScore, (existing, replacement) -> existing));

        double quiz = scores.getOrDefault("quiz", 0.0);
        double midterm = scores.getOrDefault("midterm", 0.0);
        double endSem = scores.getOrDefault("end-sem", 0.0); // Looking for "end-sem" key

        double finalScore = (quiz ) + (midterm ) + (endSem );
        String letterGrade = determineLetterGrade(finalScore);

        boolean saved = gradeDAO.updateFinalGrade(enrollmentId, letterGrade);
        return saved ? Result.success(letterGrade) : Result.error("Failed to save final grade.");
    }

    
    public Result<List<StudentGradeDTO>> getGradebook(int sectionId) {

        String sql = 
            "SELECT e.enrollment_id, s.roll_no, " +
            "MAX(CASE WHEN UPPER(TRIM(g.component)) = 'QUIZ' THEN g.score END) as quiz, " +
            "MAX(CASE WHEN UPPER(TRIM(g.component)) = 'MIDTERM' THEN g.score END) as midterm, " +
            "MAX(CASE WHEN UPPER(TRIM(g.component)) = 'END-SEM' THEN g.score END) as endsem, " +
            "MAX(g.final_grade) as final_grade " +
            "FROM enrollments e " +
            "JOIN students s ON e.student_id = s.user_id " +
            "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
            "WHERE e.section_id = ? AND e.status = 'REGISTERED' " +
            "GROUP BY e.enrollment_id, s.roll_no";

        List<StudentGradeDTO> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Helper to safely get Double (handle NULLs)
                    Double q = rs.getObject("quiz") != null ? rs.getDouble("quiz") : null;
                    Double m = rs.getObject("midterm") != null ? rs.getDouble("midterm") : null;
                    Double e = rs.getObject("endsem") != null ? rs.getDouble("endsem") : null;
                    
                    list.add(new StudentGradeDTO(
                        rs.getInt("enrollment_id"),
                        rs.getString("roll_no"),
                        q, m, e,
                        rs.getString("final_grade")
                    ));
                }
            }
            return Result.success("Gradebook loaded", list);

        } catch (SQLException e) {
            e.printStackTrace();
            return Result.error("Database error loading gradebook.");
        }
    }

    private String determineLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}