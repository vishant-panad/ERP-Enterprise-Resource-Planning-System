package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.data.DatabaseManager;
import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // 1. Setup Theme (FlatLaf) for a modern look
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // 2. Initialize Database Pools early (Fail fast if DB is down)
        System.out.println("Connecting to databases...");
        try {
            DatabaseManager.initialize();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Critical Error: Cannot connect to databases.\n" + e.getMessage(), 
                "Startup Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch UI
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginScreen = new LoginFrame();
            loginScreen.setVisible(true);
        });
        
        // Add shutdown hook to close DB connections cleanly
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::shutdown));
    }
}