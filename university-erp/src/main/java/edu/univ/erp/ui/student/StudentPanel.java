package edu.univ.erp.ui.student;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.ServiceFactory;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.util.Result;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class StudentPanel extends JPanel {

    private final StudentService studentService;
    private final int studentId;
    
    // UI Components
    private JTable catalogTable;
    private DefaultTableModel catalogModel;
    private JTable timetableTable;
    private DefaultTableModel timetableModel;

    public StudentPanel(ServiceFactory factory) {
        this.studentService = factory.getStudentService();
        this.studentId = SessionManager.getCurrentUser().getUserId();

        setLayout(new BorderLayout());
        initComponents();
        
        // Load initial data
        refreshCatalog();
        refreshTimetable();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Course Catalog (Register)
        tabbedPane.addTab("Course Catalog", createCatalogPanel());

        // Tab 2: My Timetable (Drop)
        tabbedPane.addTab("My Timetable", createTimetablePanel());
        
        // Tab 3: Transcript (Download)
        tabbedPane.addTab("Transcript", createTranscriptPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- TAB 1: CATALOG ---
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        // Header
        JLabel lblHeader = new JLabel("Available Courses (Fall 2025)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblHeader, "wrap");

        // Table
        String[] cols = {"ID", "Code", "Title", "Credits", "Instructor", "Day/Time", "Room", "Seats"};
        catalogModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        catalogTable = new JTable(catalogModel);
        panel.add(new JScrollPane(catalogTable), "grow, wrap");

        // Buttons
        JButton btnRegister = new JButton("Register Selected");
        btnRegister.setBackground(new Color(0, 120, 215));
        btnRegister.setForeground(Color.WHITE);
        
        btnRegister.addActionListener(e -> handleRegister());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshCatalog());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnRefresh);
        btnPanel.add(btnRegister);
        panel.add(btnPanel, "growx");

        return panel;
    }

    // --- TAB 2: TIMETABLE ---
    private JPanel createTimetablePanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[][grow][]"));

        JLabel lblHeader = new JLabel("My Registered Sections");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(lblHeader, "wrap");

        String[] cols = {"Section ID", "Code", "Title", "Time", "Room", "Semester"};
        timetableModel = new DefaultTableModel(cols, 0) {
            @Override // Make table read-only
            public boolean isCellEditable(int row, int col) { return false; }
        };
        timetableTable = new JTable(timetableModel);
        panel.add(new JScrollPane(timetableTable), "grow, wrap");

        JButton btnDrop = new JButton("Drop Selected");
        btnDrop.setBackground(new Color(220, 53, 69)); // Red color
        btnDrop.setForeground(Color.WHITE);
        btnDrop.addActionListener(e -> handleDrop());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnDrop);
        panel.add(btnPanel, "growx");

        return panel;
    }
    
    // --- TAB 3: TRANSCRIPT ---
    private JPanel createTranscriptPanel() {
        JPanel panel = new JPanel(new MigLayout("insets 50, center"));
        
        JLabel lblTitle = new JLabel("Academic Transcript");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel lblDesc = new JLabel("Download your official academic record including all graded courses.");
        
        JButton btnPdf = new JButton("Download PDF");
        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPdf.addActionListener(e -> handleDownload("PDF"));
        
        JButton btnCsv = new JButton("Download CSV");
        btnCsv.addActionListener(e -> handleDownload("CSV"));

        panel.add(lblTitle, "wrap, center");
        panel.add(lblDesc, "wrap, center, gaptop 10");
        panel.add(btnPdf, "split 2, center, gaptop 20, w 150!");
        panel.add(btnCsv, "center, w 150!");
        
        return panel;
    }

    // --- HANDLERS ---

    private void refreshCatalog() {
        catalogModel.setRowCount(0);
        // Hardcoded term for now, consistent with Service logic
        Result<List<Section>> result = studentService.listCatalog("Fall", 2025);
        
        if (result.isSuccess()) {
            for (Section s : result.getData()) {
                // Ideally fetch Course Title via join, for now simplistic display
                catalogModel.addRow(new Object[]{
                    s.getSectionId(),
                    "CID-" + s.getCourseId(), // In real app, join Course table to get "CS101"
                    "Course #" + s.getCourseId(), // Placeholder title
                    4, // Placeholder credits
                    s.getInstructorId() == null ? "TBA" : s.getInstructorId(),
                    s.getDayTime(),
                    s.getRoom(),
                    s.getCapacity()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage());
        }
    }

    private void refreshTimetable() {
        timetableModel.setRowCount(0);
        Result<List<Section>> result = studentService.viewTimetable(studentId);
        
        if (result.isSuccess()) {
            for (Section s : result.getData()) {
                timetableModel.addRow(new Object[]{
                    s.getSectionId(),
                    "CID-" + s.getCourseId(),
                    "Course #" + s.getCourseId(),
                    s.getDayTime(),
                    s.getRoom(),
                    s.getSemester()
                });
            }
        }
    }

    private void handleRegister() {
        int row = catalogTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to register.");
            return;
        }

        int sectionId = (int) catalogModel.getValueAt(row, 0);
        Result<Void> result = studentService.register(studentId, sectionId);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            refreshTimetable(); // Update the other tab
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDrop() {
        int row = timetableTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to drop.");
            return;
        }

        int sectionId = (int) timetableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to drop this section?");
        
        if (confirm == JOptionPane.YES_OPTION) {
            Result<Void> result = studentService.dropSection(studentId, sectionId);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Section Dropped.");
                refreshTimetable();
                refreshCatalog(); // Refresh catalog to show seat availability if we tracked it live
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleDownload(String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new File("transcript." + format.toLowerCase()));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            Result<Void> res = studentService.downloadTranscript(studentId, format, fileToSave.getAbsolutePath());
            
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}