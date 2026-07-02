package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.access.MaintenanceChecker;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.StudentGradeDTO; // Import DTO
import edu.univ.erp.service.ServiceFactory;
import edu.univ.erp.util.Result;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class InstructorPanel extends JPanel {

    private final InstructorService instructorService;
    private final int instructorId;

    // UI Components
    private JComboBox<Section> sectionComboBox;
    private JTable gradeTable;
    private DefaultTableModel gradeModel;
    private JLabel lblStatus;
    private JButton btnSave;
    private JButton btnCompute;
    private JLabel lblMaintenance;

    public InstructorPanel(ServiceFactory factory) {
        this.instructorService = factory.getInstructorService();
        this.instructorId = SessionManager.getCurrentUser().getUserId();

        setLayout(new BorderLayout());
        initComponents();
        loadMySections();
        checkMaintenanceMode();
    }

    private void initComponents() {
        // --- Top Panel: Selection & Status ---
        JPanel topPanel = new JPanel(new MigLayout("insets 10, fillx", "[][grow][]"));
        
        // Maintenance Warning Banner (Hidden by default)
        lblMaintenance = new JLabel("⚠️ SYSTEM IN MAINTENANCE MODE - READ ONLY");
        lblMaintenance.setForeground(Color.RED);
        lblMaintenance.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaintenance.setVisible(false);
        topPanel.add(lblMaintenance, "span, center, wrap");

        topPanel.add(new JLabel("Select Section:"));
        
        sectionComboBox = new JComboBox<>();
        sectionComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Section) {
                    Section s = (Section) value;
                    setText(String.format("Sec %d: (Course %d) %s - %s", 
                        s.getSectionId(), s.getCourseId(), s.getDayTime(), s.getSemester()));
                }
                return this;
            }
        });
        sectionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                loadGradebook((Section) e.getItem());
            }
        });
        topPanel.add(sectionComboBox, "growx");

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            loadMySections();
            checkMaintenanceMode();
            // Reload current gradebook if a section is selected
            if (sectionComboBox.getSelectedItem() != null) {
                loadGradebook((Section) sectionComboBox.getSelectedItem());
            }
        });
        topPanel.add(btnRefresh, "wrap");

        add(topPanel, BorderLayout.NORTH);

        // --- Center Panel: Gradebook Table ---
        // Columns: EnrollmentID (Hidden), Student Info, Quiz (20%), Midterm (30%), EndSem (50%), Final, GradeID(Hidden)
        String[] cols = {"Enrollment ID", "Student Info", "Quiz (20)", "Midterm (30)", "End-Sem (50)", "Final Grade"};
        
        gradeModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing Score columns (2, 3, 4) if NOT in maintenance mode
                if (MaintenanceChecker.isReadOnly()) return false;
                return column == 2 || column == 3 || column == 4;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2 && columnIndex <= 4) return Double.class; // Ensure numbers for scores
                return String.class;
            }
        };

        gradeTable = new JTable(gradeModel);
        // Hide Enrollment ID column (index 0)
        gradeTable.removeColumn(gradeTable.getColumnModel().getColumn(0));

        add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // --- Bottom Panel: Actions ---
        JPanel bottomPanel = new JPanel(new MigLayout("insets 10, fillx", "push[][]")); // push pushes buttons to right

        lblStatus = new JLabel("Ready");
        bottomPanel.add(lblStatus, "growx"); // Left side status

        btnSave = new JButton("Save Scores");
        btnSave.setBackground(new Color(0, 120, 215));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> handleSaveScores());
        bottomPanel.add(btnSave);

        btnCompute = new JButton("Calculate Final Grades");
        btnCompute.addActionListener(e -> handleComputeGrades());
        bottomPanel.add(btnCompute);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- Logic ---

    private void checkMaintenanceMode() {
        boolean isReadOnly = MaintenanceChecker.isReadOnly();
        lblMaintenance.setVisible(isReadOnly);
        btnSave.setEnabled(!isReadOnly);
        btnCompute.setEnabled(!isReadOnly);
        gradeTable.setEnabled(!isReadOnly); // Disables editing in table
        
        if (isReadOnly) {
            lblStatus.setText("Maintenance Mode ON. View Only.");
        } else {
            lblStatus.setText("Ready to edit scores.");
        }
    }

    private void loadMySections() {
        sectionComboBox.removeAllItems();
        Result<List<Section>> result = instructorService.getMySections(instructorId);
        
        if (result.isSuccess()) {
            for (Section s : result.getData()) {
                sectionComboBox.addItem(s);
            }
            if (sectionComboBox.getItemCount() > 0) {
                sectionComboBox.setSelectedIndex(0);
                loadGradebook((Section) sectionComboBox.getSelectedItem());
            }
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage());
        }
    }

    /**
     * UPDATED: Now wires up to the actual Service Layer
     */
    private void loadGradebook(Section section) {
        if (section == null) return;
        
        gradeModel.setRowCount(0); // Clear table
        lblStatus.setText("Loading students...");
        
        // Call the service to get the "Spreadsheet" view [cite: 36, 129]
        Result<List<StudentGradeDTO>> result = instructorService.getGradebook(section.getSectionId());
        
        if (result.isSuccess()) {
            List<StudentGradeDTO> students = result.getData();
            
            for (StudentGradeDTO dto : students) {
                gradeModel.addRow(new Object[]{
                    dto.enrollmentId,
                    dto.studentName,  // Matches Student Info column
                    dto.quizScore,    // Matches Quiz (20)
                    dto.midtermScore, // Matches Midterm (30)
                    dto.endSemScore,  // Matches End-Sem (50)
                    dto.finalGrade    // Matches Final Grade
                });
            }
            lblStatus.setText("Loaded " + students.size() + " student(s).");
        } else {
            lblStatus.setText("Error loading data.");
            JOptionPane.showMessageDialog(this, "Failed to load gradebook: " + result.getMessage());
        }
    }

    private void handleSaveScores() {
        System.out.println("DEBUG: Starting Save Process...");
        if (gradeTable.isEditing()) gradeTable.getCellEditor().stopCellEditing();

        int rowCount = gradeModel.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            Object idObj = gradeModel.getValueAt(i, 0);
            if (idObj == null) continue;
            int enrollmentId = (int) idObj;

            Double quiz = parseScore(gradeModel.getValueAt(i, 2));
            Double mid = parseScore(gradeModel.getValueAt(i, 3));
            Double end = parseScore(gradeModel.getValueAt(i, 4));

            // 1. Save Quiz
            if (quiz != null) {
                System.out.println("Saving Quiz: " + quiz);
                instructorService.updateScore(instructorId, enrollmentId, "Quiz", quiz);
            }

            // 2. Save Midterm (THIS WAS MISSING)
            if (mid != null) {
                System.out.println("Saving Midterm: " + mid);
                instructorService.updateScore(instructorId, enrollmentId, "Midterm", mid);
            }

            // 3. Save End-Sem (THIS WAS MISSING)
            if (end != null) {
                System.out.println("Saving End-Sem: " + end);
                instructorService.updateScore(instructorId, enrollmentId, "End-Sem", end);
            }
        }

        lblStatus.setText("Scores saved.");
        // JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
    }

    private void handleComputeGrades() {
        // 1. Save what is currently on screen
        handleSaveScores(); 

        if (gradeTable.isEditing()) gradeTable.getCellEditor().stopCellEditing();
        
        int rowCount = gradeModel.getRowCount();
        boolean anyUpdates = false;
        
        for (int i = 0; i < rowCount; i++) {
            int enrollmentId = (int) gradeModel.getValueAt(i, 0);
            Result<String> res = instructorService.computeFinalGrade(instructorId, enrollmentId);
            
            if (res.isSuccess()) {
                anyUpdates = true;
            }
        }
        
        if (anyUpdates) {
            JOptionPane.showMessageDialog(this, "Final grades calculated and saved.");
            
            // --- ADD THIS LINE (The Auto-Refresh) ---
            // This forces the table to reload data from the database immediately
            loadGradebook((Section) sectionComboBox.getSelectedItem()); 
            // ----------------------------------------
            
        } else {
            JOptionPane.showMessageDialog(this, "Could not calculate grades.");
        }
    }

    private Double parseScore(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        try {
            String str = value.toString().trim();
            if (str.isEmpty()) return null;
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}