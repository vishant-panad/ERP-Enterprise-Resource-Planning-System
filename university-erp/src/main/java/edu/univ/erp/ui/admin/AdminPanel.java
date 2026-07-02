package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.access.MaintenanceChecker;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.ServiceFactory;
import edu.univ.erp.util.Result;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    private final AdminService adminService;
    private final int adminId;

    public AdminPanel(ServiceFactory factory) {
        this.adminService = factory.getAdminService();
        this.adminId = SessionManager.getCurrentUser().getUserId();

        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: User Management (Add Students/Instructors)
        tabbedPane.addTab("User Management", createUserPanel());

        // Tab 2: Academic Management (Courses/Sections)
        tabbedPane.addTab("Academic Management", createAcademicPanel());

        // Tab 3: System Settings (Maintenance Mode)
        tabbedPane.addTab("System Control", createSystemPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: USER MANAGEMENT
    // ==========================================
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow][grow]", "[top]"));

        // --- Left: Add Student ---
        JPanel pnlStudent = new JPanel(new MigLayout("wrap 2, insets 15", "[][grow]", "[]10[]"));
        pnlStudent.setBorder(BorderFactory.createTitledBorder("Add New Student"));

        JTextField txtStuUser = new JTextField();
        JPasswordField txtStuPass = new JPasswordField();
        JTextField txtRoll = new JTextField();
        JTextField txtProgram = new JTextField("B.Tech CS"); // Default
        JSpinner spnYear = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

        pnlStudent.add(new JLabel("Username:")); pnlStudent.add(txtStuUser, "growx");
        pnlStudent.add(new JLabel("Password:")); pnlStudent.add(txtStuPass, "growx");
        pnlStudent.add(new JLabel("Roll No:"));  pnlStudent.add(txtRoll, "growx");
        pnlStudent.add(new JLabel("Program:"));  pnlStudent.add(txtProgram, "growx");
        pnlStudent.add(new JLabel("Year:"));     pnlStudent.add(spnYear, "width 60!");

        JButton btnAddStudent = new JButton("Create Student");
        btnAddStudent.setBackground(new Color(0, 120, 215));
        btnAddStudent.setForeground(Color.WHITE);
        btnAddStudent.addActionListener(e -> {
            // Changed <Void> to <Integer>
            Result<Integer> res = adminService.createStudent(
                adminId, txtStuUser.getText(), new String(txtStuPass.getPassword()),
                txtRoll.getText(), txtProgram.getText(), (int) spnYear.getValue()
            );
            
            if(res.isSuccess()) { 
                // Show the ID in the message
                JOptionPane.showMessageDialog(this, res.getMessage() + "\nNew User ID: " + res.getData());
                txtStuUser.setText(""); txtStuPass.setText(""); txtRoll.setText(""); 
            } else {
                handleResult(res, "");
            }
        });
        pnlStudent.add(btnAddStudent, "span 2, center, gaptop 15");


        // --- Right: Add Instructor ---
        JPanel pnlInst = new JPanel(new MigLayout("wrap 2, insets 15", "[][grow]", "[]10[]"));
        pnlInst.setBorder(BorderFactory.createTitledBorder("Add New Instructor"));

        JTextField txtInstUser = new JTextField();
        JPasswordField txtInstPass = new JPasswordField();
        JTextField txtDept = new JTextField("Computer Science");

        pnlInst.add(new JLabel("Username:")); pnlInst.add(txtInstUser, "growx");
        pnlInst.add(new JLabel("Password:")); pnlInst.add(txtInstPass, "growx");
        pnlInst.add(new JLabel("Department:")); pnlInst.add(txtDept, "growx");

        JButton btnAddInst = new JButton("Create Instructor");
        btnAddInst.setBackground(new Color(0, 150, 136)); // Teal
        btnAddInst.setForeground(Color.WHITE);
        btnAddInst.addActionListener(e -> {
            // Changed <Void> to <Integer>
            Result<Integer> res = adminService.createInstructor(
                adminId, txtInstUser.getText(), new String(txtInstPass.getPassword()), txtDept.getText()
            );
            
            if(res.isSuccess()) { 
                // Show the ID in the message
                JOptionPane.showMessageDialog(this, res.getMessage() + "\nNew User ID: " + res.getData());
                txtInstUser.setText(""); txtInstPass.setText(""); 
            } else {
                handleResult(res, "");
            }
        });
        pnlInst.add(btnAddInst, "span 2, center, gaptop 15");

        panel.add(pnlStudent, "grow");
        panel.add(pnlInst, "grow");
        return panel;
    }

    // ==========================================
    // TAB 2: ACADEMIC MANAGEMENT
    // ==========================================
    private JPanel createAcademicPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[]20[]"));

        // --- 1. Create Course ---
        JPanel pnlCourse = new JPanel(new MigLayout("insets 10", "[][grow][][grow][][50!] []"));
        pnlCourse.setBorder(BorderFactory.createTitledBorder("Create Course"));
        
        JTextField txtCode = new JTextField(10);
        JTextField txtTitle = new JTextField(20);
        JSpinner spnCredits = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        JButton btnAddCourse = new JButton("Add Course");

        pnlCourse.add(new JLabel("Code:")); pnlCourse.add(txtCode, "growx");
        pnlCourse.add(new JLabel("Title:")); pnlCourse.add(txtTitle, "growx");
        pnlCourse.add(new JLabel("Credits:")); pnlCourse.add(spnCredits);
        pnlCourse.add(btnAddCourse);

        btnAddCourse.addActionListener(e -> {
            // FIX: Changed <Void> to <Integer>
            Result<Integer> res = adminService.createCourse(adminId, txtCode.getText(), txtTitle.getText(), (int)spnCredits.getValue());
            
            if (res.isSuccess()) {
                // Show the ID in the popup
                JOptionPane.showMessageDialog(this, res.getMessage() + "\nNew Course ID: " + res.getData());
            } else {
                handleResult(res, ""); // Show error
            }
        });

        // --- 2. Create Section ---
        JPanel pnlSection = new JPanel(new MigLayout("wrap 4, insets 10", "[][grow][][grow]", "[]10[]"));
        pnlSection.setBorder(BorderFactory.createTitledBorder("Create Section"));

        JTextField txtCourseId = new JTextField();
        JTextField txtDayTime = new JTextField("Mon/Wed 10:00");
        JTextField txtRoom = new JTextField("C-101");
        JSpinner spnCap = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
        JTextField txtSem = new JTextField("Fall");
        JSpinner spnYear = new JSpinner(new SpinnerNumberModel(2025, 2020, 2030, 1));

        pnlSection.add(new JLabel("Course ID:")); pnlSection.add(txtCourseId, "growx");
        pnlSection.add(new JLabel("Day/Time:")); pnlSection.add(txtDayTime, "growx");
        pnlSection.add(new JLabel("Room:"));     pnlSection.add(txtRoom, "growx");
        pnlSection.add(new JLabel("Capacity:")); pnlSection.add(spnCap, "growx");
        pnlSection.add(new JLabel("Semester:")); pnlSection.add(txtSem, "growx");
        pnlSection.add(new JLabel("Year:"));     pnlSection.add(spnYear, "growx");

        JButton btnAddSection = new JButton("Create Section");
        btnAddSection.addActionListener(e -> {
            try {
                int cid = Integer.parseInt(txtCourseId.getText());
                // FIX: Changed <Void> to <Integer>
                Result<Integer> res = adminService.createSection(
                    adminId, cid, txtDayTime.getText(), txtRoom.getText(),
                    (int)spnCap.getValue(), txtSem.getText(), (int)spnYear.getValue()
                );
                
                if (res.isSuccess()) {
                    // Show the ID in the popup
                    JOptionPane.showMessageDialog(this, res.getMessage() + "\nNew Section ID: " + res.getData());
                } else {
                    handleResult(res, "");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Course ID must be a number.");
            }
        });
        pnlSection.add(btnAddSection, "span 4, center, gaptop 10");

        // --- 3. Assign Instructor ---
        JPanel pnlAssign = new JPanel(new MigLayout("insets 10", "[][grow][][grow] []"));
        pnlAssign.setBorder(BorderFactory.createTitledBorder("Assign Instructor to Section"));

        JTextField txtSecId = new JTextField();
        JTextField txtInstId = new JTextField();
        JButton btnAssign = new JButton("Assign");

        pnlAssign.add(new JLabel("Section ID:")); pnlAssign.add(txtSecId, "width 80!");
        pnlAssign.add(new JLabel("Instructor User ID:")); pnlAssign.add(txtInstId, "width 80!");
        pnlAssign.add(btnAssign);

        btnAssign.addActionListener(e -> {
            try {
                int sid = Integer.parseInt(txtSecId.getText());
                int iid = Integer.parseInt(txtInstId.getText());
                Result<Void> res = adminService.assignInstructor(adminId, sid, iid);
                handleResult(res, "Instructor Assigned");
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "IDs must be numbers.");
            }
        });

        panel.add(pnlCourse, "growx, wrap");
        panel.add(pnlSection, "growx, wrap");
        panel.add(pnlAssign, "growx");

        return panel;
    }

    // ==========================================
    // TAB 3: SYSTEM CONTROL
    // ==========================================
    private JPanel createSystemPanel() {
        JPanel panel = new JPanel(new MigLayout("fill, center, insets 50"));

        JLabel lblInfo = new JLabel("<html><center><h1>System Maintenance Mode</h1>" +
                "When ON, Students and Instructors will be in <b>Read-Only</b> mode.<br>" +
                "They cannot register, drop, or edit grades.</center></html>");
        
        JToggleButton tglMaintenance = new JToggleButton("Maintenance is OFF");
        tglMaintenance.setPreferredSize(new Dimension(200, 60));
        tglMaintenance.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        // Load initial state
        boolean isCurrentReadOnly = MaintenanceChecker.isReadOnly();
        tglMaintenance.setSelected(isCurrentReadOnly);
        updateToggleVisuals(tglMaintenance, isCurrentReadOnly);

        tglMaintenance.addActionListener(e -> {
            boolean enable = tglMaintenance.isSelected();
            Result<Void> res = adminService.setMaintenanceMode(adminId, enable);
            
            if (res.isSuccess()) {
                updateToggleVisuals(tglMaintenance, enable);
                JOptionPane.showMessageDialog(this, "Maintenance Mode updated successfully.");
            } else {
                // Revert if failed
                tglMaintenance.setSelected(!enable);
                JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(lblInfo, "wrap, center, gapbottom 30");
        panel.add(tglMaintenance, "center");

        return panel;
    }

    private void updateToggleVisuals(JToggleButton btn, boolean isOn) {
        if (isOn) {
            btn.setText("Maintenance is ON");
            btn.setBackground(new Color(220, 53, 69)); // Red
            btn.setForeground(Color.WHITE);
        } else {
            btn.setText("Maintenance is OFF");
            btn.setBackground(new Color(40, 167, 69)); // Green
            btn.setForeground(Color.WHITE);
        }
    }

    private void handleResult(Result<?> res, String successMsg) {
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, successMsg);
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}