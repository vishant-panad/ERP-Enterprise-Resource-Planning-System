package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserAuth;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.MainFrame;
import edu.univ.erp.util.Result;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center on screen


        JPanel panel = new JPanel(new MigLayout("wrap 2, insets 30", "[][grow]", "[]20[]20[]"));

        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(lblTitle, "span 2, center, wrap");

        
        panel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        panel.add(txtUsername, "growx"); 

        // Password
        panel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        panel.add(txtPassword, "growx");

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(0, 120, 215)); // Nice blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(this::handleLogin);
        
        panel.add(btnLogin, "span 2, center, growx, gaptop 20");
        
        getRootPane().setDefaultButton(btnLogin);

        add(panel);
    }

    private void handleLogin(ActionEvent e) {
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());

        btnLogin.setEnabled(false); // Prevent double-clicks

        // Run logic on background thread to keep UI responsive
        SwingWorker<Result<UserAuth>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<UserAuth> doInBackground() {
                return authService.login(user, pass);
            }

            @Override
            protected void done() {
                try {
                    Result<UserAuth> result = get();
                    if (result.isSuccess()) {
                        onLoginSuccess(result.getData());
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, 
                                result.getMessage(), 
                                "Login Failed", 
                                JOptionPane.ERROR_MESSAGE);
                        btnLogin.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void onLoginSuccess(UserAuth user) {
        // 1. Store session globally
        SessionManager.login(user);

        // 2. Open Main Dashboard
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);

        // 3. Close Login Screen
        this.dispose();
    }
}