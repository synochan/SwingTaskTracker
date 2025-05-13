package com.cinebook.view;

import com.cinebook.controller.AdminController;
import com.cinebook.controller.UserController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for admin dashboard with management features.
 */
public class AdminDashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private AdminController adminController;
    
    // Child panels
    private MovieManagementPanel movieManagementPanel;
    private UserManagementPanel userManagementPanel;
    private PromoCodeManagementPanel promoCodeManagementPanel;
    private ReportPanel reportPanel;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JButton logoutButton;
    private JLabel welcomeLabel;
    
    /**
     * Constructor for AdminDashboardPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public AdminDashboardPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.adminController = new AdminController(userController);
        
        // Create child panels
        this.movieManagementPanel = new MovieManagementPanel(mainFrame, adminController);
        this.userManagementPanel = new UserManagementPanel(mainFrame, adminController);
        this.promoCodeManagementPanel = new PromoCodeManagementPanel(mainFrame);
        this.reportPanel = new ReportPanel(mainFrame, adminController);
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create components
        createTopPanel();
        createTabbedPane();
    }
    
    /**
     * Creates the top panel with welcome message and logout button.
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Welcome message
        welcomeLabel = new JLabel("Welcome, Admin");
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 16));
        
        // Logout button
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userController.logout();
                JOptionPane.showMessageDialog(mainFrame,
                    "You have been logged out successfully.",
                    "Logout Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                mainFrame.navigateTo(MainFrame.LOGIN_PANEL);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(logoutButton);
        
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the tabbed pane with management panels.
     */
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        
        // Add tabs
        tabbedPane.addTab("Movie Management", null, movieManagementPanel, "Manage movies and screenings");
        tabbedPane.addTab("User Management", null, userManagementPanel, "Manage user accounts");
        tabbedPane.addTab("Promo Codes", null, promoCodeManagementPanel, "Manage promotional codes");
        tabbedPane.addTab("Reports & Analytics", null, reportPanel, "View sales reports and analytics");
        
        // Add listener to refresh data when tab is selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            
            switch (selectedIndex) {
                case 0:
                    movieManagementPanel.refreshData();
                    break;
                case 1:
                    userManagementPanel.refreshData();
                    break;
                case 2:
                    // PromoCodeManagementPanel refreshes itself when loaded
                    break;
                case 3:
                    reportPanel.refreshData();
                    break;
            }
        });
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Refreshes the data displayed in all panels.
     */
    public void refreshData() {
        // Update welcome message
        if (userController.isUserLoggedIn()) {
            welcomeLabel.setText("Welcome, " + userController.getCurrentUser().getFullName());
        } else {
            welcomeLabel.setText("Welcome, Admin");
        }
        
        // Refresh the currently visible panel
        int selectedIndex = tabbedPane.getSelectedIndex();
        
        switch (selectedIndex) {
            case 0:
                movieManagementPanel.refreshData();
                break;
            case 1:
                userManagementPanel.refreshData();
                break;
            case 2:
                // PromoCodeManagementPanel refreshes itself when loaded
                break;
            case 3:
                reportPanel.refreshData();
                break;
        }
    }
}
