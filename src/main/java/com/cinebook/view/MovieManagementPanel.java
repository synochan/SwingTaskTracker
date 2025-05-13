package com.cinebook.view;

import com.cinebook.controller.AdminController;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for managing movies in the admin dashboard.
 */
public class MovieManagementPanel extends JPanel {
    private MainFrame mainFrame;
    private AdminController adminController;
    
    /**
     * Constructor for MovieManagementPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param adminController The AdminController instance
     */
    public MovieManagementPanel(MainFrame mainFrame, AdminController adminController) {
        this.mainFrame = mainFrame;
        this.adminController = adminController;
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Placeholder implementation - to be expanded
        JLabel placeholderLabel = new JLabel("Movie Management Panel - Under Construction", JLabel.CENTER);
        placeholderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(placeholderLabel, BorderLayout.CENTER);
    }
    
    /**
     * Refreshes the data displayed in the panel.
     */
    public void refreshData() {
        // To be implemented
    }
}