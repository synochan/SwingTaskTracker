package com.cinebook;

import com.cinebook.dao.DBConnection;
import com.cinebook.view.MainFrame;
import com.cinebook.view.SplashScreen;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main entry point for the CineBook CDO application.
 * This class initializes the database connection and launches the main GUI.
 */
public class Main {
    
    public static void main(String[] args) {
        // Set the look and feel to the system default or Nimbus for a more modern look
        try {
            // Try to use Nimbus look and feel for a more modern appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // Customize Nimbus colors to match our app theme
                    UIManager.put("nimbusBase", new java.awt.Color(63, 81, 181));
                    UIManager.put("nimbusBlueGrey", new java.awt.Color(120, 130, 200));
                    UIManager.put("control", new java.awt.Color(240, 240, 240));
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus isn't available, fall back to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Failed to set look and feel: " + ex.getMessage());
            }
        }
        
        // Initialize database connection
        try {
            Connection connection = DBConnection.getConnection();
            System.out.println("Connected to the database successfully.");
            
            // Launch the application with a splash screen
            SplashScreen splash = new SplashScreen();
            splash.showSplash();
            
            // Create a background task to initialize the application
            new Thread(() -> {
                try {
                    // Simulate loading steps (could be real initialization steps)
                    for (int i = 0; i <= 100; i += 10) {
                        splash.setProgress(i);
                        Thread.sleep(150); // Small delay to show progress
                    }
                    
                    // Start the main application on the EDT when ready
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                        splash.closeSplash();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error during application initialization: " + e.getMessage());
                    
                    // If error occurs, close splash and show error
                    SwingUtilities.invokeLater(() -> {
                        splash.closeSplash();
                        JOptionPane.showMessageDialog(null, 
                            "Error initializing application: " + e.getMessage(),
                            "Initialization Error",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    });
                }
            }).start();
            
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
