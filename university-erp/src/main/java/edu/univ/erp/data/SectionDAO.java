package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SectionDAO {
    private static final Logger LOGGER = Logger.getLogger(SectionDAO.class.getName());

    // --- SQL Constants ---
    private static final String SELECT_BY_INSTRUCTOR = "SELECT * FROM sections WHERE instructor_id = ?";
    private static final String SELECT_BY_TERM = "SELECT * FROM sections WHERE semester = ? AND year = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM sections WHERE section_id = ?";
    private static final String INSERT_SECTION = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?)";

   
    public List<Section> listByTerm(String semester, int year) {
        List<Section> list = new ArrayList<>();
        // 1. The SQL now uses the WHERE clause again
        String sql = "SELECT * FROM sections WHERE semester = ? AND year = ?";
        
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // 2. We securely fill in the blanks
            stmt.setString(1, semester);
            stmt.setInt(2, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing sections by term", e);
        }
        return list;
    }

    public Section getById(int sectionId) { 
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching section " + sectionId, e);
        }
        return null; 
    }
    
    
    public List<Section> getByInstructor(int instructorId) {
        List<Section> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_INSTRUCTOR)) {
            
            stmt.setInt(1, instructorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing instructor sections", e);
        }
        return list;
    }

    public boolean create(Section section) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SECTION, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, section.getCourseId());
            if (section.getInstructorId() != null) {
                stmt.setInt(2, section.getInstructorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setString(6, section.getSemester());
            stmt.setInt(7, section.getYear());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) section.setSectionId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating section", e);
        }
        return false;
    }

    // Stub: Implement if needed for Admin features
    public boolean update(Section section) { return false; }
    public boolean delete(int sectionId) { return false; }
    public List<Section> list() { return new ArrayList<>(); } // Generic list all

    // Helper to map ResultSet to Domain Object
    private Section mapRow(ResultSet rs) throws SQLException {
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

    private static final String UPDATE_INSTRUCTOR_SQL = 
        "UPDATE sections SET instructor_id = ? WHERE section_id = ?";

    
    public boolean assignInstructor(int sectionId, int instructorId) {
        try (Connection conn = DatabaseManager.getErpConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_INSTRUCTOR_SQL)) {
            
            stmt.setInt(1, instructorId);
            stmt.setInt(2, sectionId);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning instructor", e);
            return false;
        }
    }
}