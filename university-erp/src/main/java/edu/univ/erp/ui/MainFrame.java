package edu.univ.erp.ui;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.ServiceFactory;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.ui.student.StudentPanel;
import net.miginfocom.swing.MigLayout;
import edu.univ.erp.ui.instructor.InstructorPanel;
import edu.univ.erp.ui.admin.AdminPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // Global Service Factory to be passed down to panels
    private final ServiceFactory serviceFactory;

    public MainFrame() {
        this.serviceFactory = new ServiceFactory();

        // Security Check
        if (!SessionManager.isLoggedIn()) {
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        initComponents();
    }

    private void initComponents() {
        UserAuth user = SessionManager.getCurrentUser();
        
        setTitle("University ERP - " + user.getRole() + " Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // --- Top Bar ---
        JPanel topBar = new JPanel(new MigLayout("insets 10, fillx", "[]push[]"));
        topBar.setBackground(new Color(245, 245, 245));

        JLabel lblWelcome = new JLabel("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());

        topBar.add(lblWelcome);
        topBar.add(btnLogout);

        // --- Content Area (Role Based) ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        switch (user.getRole()) {
            case "STUDENT":
                contentPanel.add(new StudentPanel(serviceFactory), BorderLayout.CENTER);
                break;
            case "INSTRUCTOR":
                // Replaced placeholder with actual panel
                contentPanel.add(new InstructorPanel(serviceFactory), BorderLayout.CENTER);
                break;
            case "ADMIN":
                // Replaced placeholder with actual panel
                contentPanel.add(new AdminPanel(serviceFactory), BorderLayout.CENTER);
                break;
            default:
                contentPanel.add(new JLabel("Unknown Role"), BorderLayout.CENTER);
        }

        // --- Layout ---
        add(topBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}