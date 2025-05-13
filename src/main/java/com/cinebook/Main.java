package com.cinebook;

import com.cinebook.dao.DBConnection;
import com.cinebook.view.MainFrame;

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
        // Set the look and feel to the system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
        
        // Initialize database connection
        try {
            Connection connection = DBConnection.getConnection();
            System.out.println("Connected to the database successfully.");
            
            // Launch the application on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
            
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
