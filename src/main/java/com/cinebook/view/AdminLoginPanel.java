package com.cinebook.view;

import com.cinebook.controller.UserController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for admin login.
 */
public class AdminLoginPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    
    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;
    
    /**
     * Constructor for AdminLoginPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public AdminLoginPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        // Create title
        JLabel titleLabel = new JLabel("Admin Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Create login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Admin Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Admin Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        backButton = new JButton("Back");
        loginButton = new JButton("Login as Admin");
        
        buttonPanel.add(backButton);
        buttonPanel.add(loginButton);
        
        // Add components to main panel
        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        setupActionListeners();
    }
    
    /**
     * Sets up action listeners for the buttons.
     */
    private void setupActionListeners() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Please enter both username and password.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = userController.login(username, password);
                
                if (success) {
                    if (userController.isCurrentUserAdmin()) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Admin login successful!",
                            "Login Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        clearFields();
                        mainFrame.updateUserUI(userController.getCurrentUser());
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Login successful, but you do not have admin privileges.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                        
                        userController.logout();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Invalid admin credentials. Please try again.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Back button action
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                mainFrame.navigateTo(MainFrame.LOGIN_PANEL);
            }
        });
    }
    
    /**
     * Clears all input fields.
     */
    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}
