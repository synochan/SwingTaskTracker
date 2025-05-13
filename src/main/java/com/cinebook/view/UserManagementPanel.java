package com.cinebook.view;

import com.cinebook.controller.AdminController;
import com.cinebook.controller.UserController;
import com.cinebook.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing users in the admin dashboard.
 */
public class UserManagementPanel extends JPanel {
    private MainFrame mainFrame;
    private AdminController adminController;
    private UserController userController;
    
    // UI Components
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton makeAdminButton;
    private JButton resetPasswordButton;
    
    // Selected user
    private User selectedUser;
    
    /**
     * Constructor for UserManagementPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param adminController The AdminController instance
     */
    public UserManagementPanel(MainFrame mainFrame, AdminController adminController) {
        this.mainFrame = mainFrame;
        this.adminController = adminController;
        this.userController = new UserController();
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create the user management panel
        createUsersPanel();
        
        // Refresh data
        refreshData();
    }
    
    /**
     * Creates the users panel with table and action buttons.
     */
    private void createUsersPanel() {
        // Create panel
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setBorder(BorderFactory.createTitledBorder("User Management"));
        
        // Table model
        String[] columns = {"ID", "Username", "Email", "Full Name", "Phone", "Admin", "Registration Date"};
        usersTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Table
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setReorderingAllowed(false);
        
        // Hide ID column
        usersTable.getColumnModel().getColumn(0).setMinWidth(0);
        usersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        usersTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Selection listener
        usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = usersTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int userId = Integer.parseInt(usersTable.getValueAt(selectedRow, 0).toString());
                        selectedUser = userController.getUserById(userId);
                        updateButtonStates();
                    } else {
                        selectedUser = null;
                        updateButtonStates();
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
        makeAdminButton = new JButton("Toggle Admin Status");
        resetPasswordButton = new JButton("Reset Password");
        
        // Add User button action
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUserDialog(null);
            }
        });
        
        // Edit User button action
        editUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedUser != null) {
                    openUserDialog(selectedUser);
                }
            }
        });
        
        // Delete User button action
        deleteUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedUser != null) {
                    // Check if trying to delete the current admin
                    if (selectedUser.isAdmin() && selectedUser.getId() == userController.getCurrentUser().getId()) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "You cannot delete your own admin account while logged in.",
                            "Delete Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Are you sure you want to delete the user '" + selectedUser.getUsername() + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = userController.deleteUser(selectedUser.getId());
                        
                        if (success) {
                            refreshData();
                            selectedUser = null;
                            updateButtonStates();
                            
                            JOptionPane.showMessageDialog(mainFrame,
                                "User deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to delete user. The user may have associated reservations.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        // Make Admin button action
        makeAdminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedUser != null) {
                    boolean newAdminStatus = !selectedUser.isAdmin();
                    String message = newAdminStatus
                        ? "Are you sure you want to make '" + selectedUser.getUsername() + "' an admin?"
                        : "Are you sure you want to remove admin rights from '" + selectedUser.getUsername() + "'?";
                    
                    int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        message,
                        "Confirm Admin Status Change",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = adminController.setUserAdminStatus(selectedUser.getId(), newAdminStatus);
                        
                        if (success) {
                            refreshData();
                            // Try to keep the same user selected
                            for (int i = 0; i < usersTable.getRowCount(); i++) {
                                if (Integer.parseInt(usersTable.getValueAt(i, 0).toString()) == selectedUser.getId()) {
                                    usersTable.setRowSelectionInterval(i, i);
                                    break;
                                }
                            }
                            
                            JOptionPane.showMessageDialog(mainFrame,
                                "Admin status updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to update admin status.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        // Reset Password button action
        resetPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedUser != null) {
                    // Show reset password dialog
                    JDialog resetDialog = new JDialog(mainFrame, "Reset Password", true);
                    resetDialog.setSize(400, 200);
                    resetDialog.setLocationRelativeTo(mainFrame);
                    resetDialog.setLayout(new BorderLayout());
                    
                    JPanel formPanel = new JPanel(new GridBagLayout());
                    formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                    
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.insets = new Insets(5, 5, 5, 5);
                    
                    // New password field
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    formPanel.add(new JLabel("New Password:"), gbc);
                    
                    gbc.gridx = 1;
                    gbc.gridy = 0;
                    gbc.weightx = 1.0;
                    JPasswordField passwordField = new JPasswordField(20);
                    formPanel.add(passwordField, gbc);
                    
                    // Confirm password field
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    gbc.weightx = 0.0;
                    formPanel.add(new JLabel("Confirm Password:"), gbc);
                    
                    gbc.gridx = 1;
                    gbc.gridy = 1;
                    gbc.weightx = 1.0;
                    JPasswordField confirmPasswordField = new JPasswordField(20);
                    formPanel.add(confirmPasswordField, gbc);
                    
                    // Buttons panel
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JButton cancelButton = new JButton("Cancel");
                    JButton resetButton = new JButton("Reset Password");
                    
                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            resetDialog.dispose();
                        }
                    });
                    
                    resetButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String password = new String(passwordField.getPassword());
                            String confirmPassword = new String(confirmPasswordField.getPassword());
                            
                            if (password.isEmpty()) {
                                JOptionPane.showMessageDialog(resetDialog,
                                    "Password cannot be empty.",
                                    "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            if (!password.equals(confirmPassword)) {
                                JOptionPane.showMessageDialog(resetDialog,
                                    "Passwords do not match.",
                                    "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            boolean success = adminController.resetUserPassword(selectedUser.getId(), password);
                            
                            if (success) {
                                resetDialog.dispose();
                                JOptionPane.showMessageDialog(mainFrame,
                                    "Password reset successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(resetDialog,
                                    "Failed to reset password.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    
                    buttonPanel.add(cancelButton);
                    buttonPanel.add(resetButton);
                    
                    resetDialog.add(formPanel, BorderLayout.CENTER);
                    resetDialog.add(buttonPanel, BorderLayout.SOUTH);
                    resetDialog.setVisible(true);
                }
            }
        });
        
        buttonsPanel.add(addUserButton);
        buttonsPanel.add(editUserButton);
        buttonsPanel.add(deleteUserButton);
        buttonsPanel.add(makeAdminButton);
        buttonsPanel.add(resetPasswordButton);
        
        // Add components to panel
        usersPanel.add(scrollPane, BorderLayout.CENTER);
        usersPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add panel to main panel
        add(usersPanel, BorderLayout.CENTER);
    }
    
    /**
     * Updates the enabled state of buttons based on selection.
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedUser != null;
        editUserButton.setEnabled(hasSelection);
        deleteUserButton.setEnabled(hasSelection);
        makeAdminButton.setEnabled(hasSelection);
        resetPasswordButton.setEnabled(hasSelection);
    }
    
    /**
     * Opens a dialog to add or edit a user.
     *
     * @param user The user to edit, or null to add a new user
     */
    private void openUserDialog(User user) {
        // Create dialog
        JDialog dialog = new JDialog(mainFrame, user == null ? "Add User" : "Edit User", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password field (only for new users)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JPasswordField passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        JTextField emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        
        // Full name field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        JTextField fullNameField = new JTextField(20);
        formPanel.add(fullNameField, gbc);
        
        // Phone number field
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Phone Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        JTextField phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);
        
        // Admin checkbox
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JCheckBox adminCheckBox = new JCheckBox("Admin");
        formPanel.add(adminCheckBox, gbc);
        
        // Set existing values if editing
        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setEditable(false); // Username cannot be changed
            passwordField.setEnabled(false); // Password is handled separately
            passwordLabel.setEnabled(false);
            emailField.setText(user.getEmail());
            fullNameField.setText(user.getFullName());
            phoneField.setText(user.getPhoneNumber());
            adminCheckBox.setSelected(user.isAdmin());
        }
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate fields
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText().trim();
                String fullName = fullNameField.getText().trim();
                String phone = phoneField.getText().trim();
                boolean isAdmin = adminCheckBox.isSelected();
                
                if (username.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Username, email, and full name are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (user == null && password.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Password is required for new users.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success;
                if (user == null) {
                    // Create new user
                    User newUser = new User(username, password, email, phone, fullName, isAdmin);
                    success = adminController.addUser(newUser) > 0;
                } else {
                    // Update existing user
                    user.setEmail(email);
                    user.setPhoneNumber(phone);
                    user.setFullName(fullName);
                    user.setAdmin(isAdmin);
                    success = adminController.updateUser(user);
                }
                
                if (success) {
                    refreshData();
                    dialog.dispose();
                    
                    JOptionPane.showMessageDialog(mainFrame,
                        user == null ? "User added successfully!" : "User updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        user == null ? "Failed to add user. Username may already exist." : "Failed to update user.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        // Add components to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Refreshes the data displayed in the panel.
     */
    public void refreshData() {
        // Clear table
        usersTableModel.setRowCount(0);
        
        // Get all users
        List<User> users = adminController.getAllUsers();
        
        // Populate table
        for (User user : users) {
            String formattedDate = user.getRegistrationDate() != null ? user.getRegistrationDate() : "";
            
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.isAdmin() ? "Yes" : "No",
                formattedDate
            };
            usersTableModel.addRow(row);
        }
        
        // Update button states
        selectedUser = null;
        updateButtonStates();
    }
}