package com.cinebook.view;

import com.cinebook.util.UIStyle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

/**
 * Manages stylish, modern dialog windows for the application.
 */
public class DialogManager {
    
    /**
     * Shows an error dialog with modern styling.
     * 
     * @param parentComponent The parent component
     * @param message The error message
     * @param title The dialog title
     */
    public static void showErrorDialog(Component parentComponent, String message, String title) {
        showCustomDialog(parentComponent, message, title, loadIcon("resources/icons/error_icon.svg", UIStyle.ERROR_COLOR), UIStyle.ERROR_COLOR);
    }
    
    /**
     * Shows a success dialog with modern styling.
     * 
     * @param parentComponent The parent component
     * @param message The success message
     * @param title The dialog title
     */
    public static void showSuccessDialog(Component parentComponent, String message, String title) {
        showCustomDialog(parentComponent, message, title, loadIcon("resources/icons/success_icon.svg", UIStyle.SUCCESS_COLOR), UIStyle.SUCCESS_COLOR);
    }
    
    /**
     * Shows a warning dialog with modern styling.
     * 
     * @param parentComponent The parent component
     * @param message The warning message
     * @param title The dialog title
     */
    public static void showWarningDialog(Component parentComponent, String message, String title) {
        showCustomDialog(parentComponent, message, title, loadIcon("resources/icons/warning_icon.svg", UIStyle.WARNING_COLOR), UIStyle.WARNING_COLOR);
    }
    
    /**
     * Shows an information dialog with modern styling.
     * 
     * @param parentComponent The parent component
     * @param message The information message
     * @param title The dialog title
     */
    public static void showInfoDialog(Component parentComponent, String message, String title) {
        showCustomDialog(parentComponent, message, title, loadIcon("resources/icons/info_icon.svg", UIStyle.INFO_COLOR), UIStyle.INFO_COLOR);
    }
    
    /**
     * Displays a custom dialog with the specified icon and color scheme.
     * 
     * @param parentComponent The parent component
     * @param message The dialog message
     * @param title The dialog title
     * @param icon The icon to display
     * @param color The color scheme
     */
    private static void showCustomDialog(Component parentComponent, String message, String title, Icon icon, Color color) {
        // Create panel with border layout
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(UIStyle.SURFACE_COLOR);
        
        // Icon panel on left
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(icon);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        panel.add(iconLabel, BorderLayout.WEST);
        
        // Message in center with stylish font
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(UIStyle.BODY_FONT);
        messageArea.setWrapStyleWord(true);
        messageArea.setLineWrap(true);
        messageArea.setEditable(false);
        messageArea.setBackground(UIStyle.SURFACE_COLOR);
        messageArea.setBorder(null);
        messageArea.setForeground(UIStyle.TEXT_PRIMARY);
        
        panel.add(messageArea, BorderLayout.CENTER);
        
        // OK button with rounded corners and color
        JButton okButton = new JButton("OK");
        okButton.setBackground(color);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setFont(UIStyle.BUTTON_FONT);
        okButton.setPreferredSize(new Dimension(100, 35));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIStyle.SURFACE_COLOR);
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create and customize JDialog
        final JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentComponent), title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);
        
        // Add action for OK button
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Loads an icon from a resource path.
     * 
     * @param path The resource path
     * @param color The color for the icon if SVG is not available
     * @return The icon
     */
    private static Icon loadIcon(String path, Color color) {
        // Try to load the SVG icon
        try {
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                return icon;
            } else {
                System.err.println("Icon file not found: " + path);
            }
        } catch (Exception e) {
            // If SVG loading fails, fallback to built-in icon
            System.err.println("Error loading icon: " + e.getMessage());
        }
        
        // Determine fallback icon based on color
        if (color.equals(UIStyle.ERROR_COLOR)) {
            return UIManager.getIcon("OptionPane.errorIcon");
        } else if (color.equals(UIStyle.WARNING_COLOR)) {
            return UIManager.getIcon("OptionPane.warningIcon");
        } else if (color.equals(UIStyle.INFO_COLOR) || color.equals(UIStyle.SUCCESS_COLOR)) {
            return UIManager.getIcon("OptionPane.informationIcon");
        } else {
            return UIManager.getIcon("OptionPane.questionIcon");
        }
    }
}