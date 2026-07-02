package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    private static final String SELECT_ALL = "SELECT * FROM courses";
    private static final String INSERT_COURSE = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";

    public CourseDAO() {
    }

    public List<Course> list() {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCode(rs.getString("code"));
                c.setTitle(rs.getString("title"));
                c.setCredits(rs.getInt("credits"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public Course getById(int courseId) {
        // Implementation omitted for brevity, but follows similar logic
        return null;
    }

    /**
     * Creates a new course and updates the course object with the generated ID.
     */
    public boolean create(Course course) {
        try (Connection conn = DatabaseManager.getErpConnection();
             // FIX 1: Add 'Statement.RETURN_GENERATED_KEYS' flag here
             PreparedStatement stmt = conn.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, course.getCode());
            stmt.setString(2, course.getTitle());
            stmt.setInt(3, course.getCredits());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // FIX 2: Retrieve the generated keys (the new ID)
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // FIX 3: Set the ID back into your Java object
                        int newId = generatedKeys.getInt(1);
                        course.setCourseId(newId);
                        System.out.println("DEBUG: Course created with ID: " + newId); // Optional debug
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Course course) {
        return false; // Implement similarly if needed
    }

    public boolean delete(int courseId) {
        return false; // Implement similarly if needed
    }
}
