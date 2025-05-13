package com.cinebook.view;

import com.cinebook.controller.UserController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for user login.
 */
public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    
    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton guestButton;
    private JButton adminButton;
    
    /**
     * Constructor for LoginPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public LoginPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        // Create title
        JLabel titleLabel = new JLabel("CineBook CDO", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        
        JLabel subtitleLabel = new JLabel("Online Movie Ticket Booking System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Serif", Font.ITALIC, 18));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Create login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        guestButton = new JButton("Continue as Guest");
        adminButton = new JButton("Admin Login");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(guestButton);
        buttonPanel.add(adminButton);
        
        // Add components to main panel
        add(titlePanel, BorderLayout.NORTH);
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
                    JOptionPane.showMessageDialog(mainFrame,
                        "Login successful! Welcome, " + userController.getCurrentUser().getFullName(),
                        "Login Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    clearFields();
                    mainFrame.updateUserUI(userController.getCurrentUser());
                } else {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Invalid username or password. Please try again.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                mainFrame.navigateTo(MainFrame.REGISTER_PANEL);
            }
        });
        
        // Guest button action
        guestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
            }
        });
        
        // Admin button action
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
                mainFrame.navigateTo(MainFrame.ADMIN_LOGIN_PANEL);
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
