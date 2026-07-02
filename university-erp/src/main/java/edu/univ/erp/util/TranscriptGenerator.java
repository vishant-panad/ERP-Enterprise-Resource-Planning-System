package edu.univ.erp.util;

import edu.univ.erp.data.EnrollmentDAO.TranscriptRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TranscriptGenerator {

    public static void generateCSV(List<TranscriptRecord> records, String filePath) throws IOException {
        try (FileWriter out = new FileWriter(filePath);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("Year", "Sem", "Code", "Title", "Credits", "Grade"))) {
            
            for (TranscriptRecord r : records) {
                printer.printRecord(r.year, r.semester, r.courseCode, r.courseTitle, r.credits, r.finalGrade);
            }
        }
    }

    public static void generatePDF(List<TranscriptRecord> records, String filePath, String studentInfo) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.newLineAtOffset(50, 750);
                content.showText("Official Academic Transcript");
                
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(0, -30);
                content.showText("Student ID: " + studentInfo);

                int y = 680;
                content.setFont(PDType1Font.COURIER, 10); // Monospaced for simpler alignment
                
                // Header
                content.newLineAtOffset(0, -40);
                content.showText(String.format("%-6s %-8s %-10s %-30s %-5s %-5s", "Year", "Sem", "Code", "Title", "Cr", "Gr"));
                content.newLineAtOffset(0, -15);
                content.showText("-------------------------------------------------------------------------------");

                for (TranscriptRecord r : records) {
                    y -= 15;
                    if (y < 50) { // New page if needed (simplified)
                        content.endText();
                        break; 
                    }
                    content.newLineAtOffset(0, -15);
                    // Basic formatting
                    String line = String.format("%-6d %-8s %-10s %-30s %-5d %-5s", 
                        r.year, r.semester, r.courseCode, 
                        truncate(r.courseTitle, 28), 
                        r.credits, r.finalGrade);
                    content.showText(line);
                }
                content.endText();
            }
            doc.save(filePath);
        }
    }

    private static String truncate(String s, int len) {
        if (s.length() <= len) return s;
        return s.substring(0, len - 3) + "...";
    }
}