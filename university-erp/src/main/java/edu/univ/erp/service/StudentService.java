package edu.univ.erp.service;

import edu.univ.erp.access.AccessChecker;
import edu.univ.erp.access.MaintenanceChecker;
import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.Result;
import edu.univ.erp.util.TranscriptGenerator; // Import added
import edu.univ.erp.data.EnrollmentDAO.TranscriptRecord; // Import added

import java.io.IOException;
import java.util.List;

public class StudentService {

    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;

    public StudentService(SectionDAO sectionDAO, EnrollmentDAO enrollmentDAO) {
        this.sectionDAO = sectionDAO;
        this.enrollmentDAO = enrollmentDAO;
    }

    public Result<List<Section>> listCatalog(String term, int year) {
        // Access Check: 0 means global/public action
        if (!AccessChecker.isAllowed(0, "BROWSE_CATALOG")) {
            return Result.error("Access denied.");
        }
        
        // Pass info if maintenance is ON, but don't block reading
        boolean isMaintenance = MaintenanceChecker.isReadOnly();
        String msg = isMaintenance ? "Maintenance is ON. View only." : "Catalog retrieved.";
        
        List<Section> sections = sectionDAO.listByTerm(term, year);
        return Result.success(msg, sections);
    }

    public Result<Void> register(int studentId, int sectionId) {
        // 1. Access Check
        if (!AccessChecker.isAllowed(studentId, "REGISTER_SECTION")) {
            return Result.error("Access denied.");
        }

        // 2. Maintenance Check
        if (MaintenanceChecker.isReadOnly()) {
            return Result.error("Maintenance Mode is ON. Registration disabled.");
        }

        // 3. Transactional Logic
        return enrollmentDAO.registerTransactional(studentId, sectionId);
    }

    public Result<Void> dropSection(int studentId, int sectionId) {
        if (!AccessChecker.isAllowed(studentId, "DROP_SECTION")) {
            return Result.error("Access denied.");
        }

        if (MaintenanceChecker.isReadOnly()) {
            return Result.error("System is in Maintenance Mode. Dropping is disabled.");
        }

        boolean success = enrollmentDAO.drop(studentId, sectionId);
        if (success) {
            return Result.success("Section dropped successfully.");
        } else {
            return Result.error("Drop failed. You may not be enrolled in this section.");
        }
    }

    public Result<List<Section>> viewTimetable(int studentId) {
        if (!AccessChecker.isAllowed(studentId, "VIEW_TIMETABLE")) {
            return Result.error("Access denied.");
        }

        List<Section> timetable = enrollmentDAO.listSectionsByStudent(studentId);
        return Result.success("Timetable retrieved.", timetable);
    }

    /**
     * IMPLEMENTED: Generates a transcript (PDF or CSV) at the specified path.
     */
    public Result<Void> downloadTranscript(int studentId, String format, String absolutePath) {
        if (!AccessChecker.isAllowed(studentId, "DOWNLOAD_TRANSCRIPT")) {
            return Result.error("Access denied.");
        }

        // 1. Fetch Data
        List<TranscriptRecord> records = enrollmentDAO.getAcademicRecord(studentId);
        if (records.isEmpty()) {
            return Result.error("No completed courses found for transcript.");
        }

        // 2. Generate File
        try {
            if ("PDF".equalsIgnoreCase(format)) {
                // Pass studentId as a string for the header
                TranscriptGenerator.generatePDF(records, absolutePath, String.valueOf(studentId));
            } else {
                TranscriptGenerator.generateCSV(records, absolutePath);
            }
            return Result.success("Transcript saved successfully to: " + absolutePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Failed to save file: " + e.getMessage());
        }
    }
}