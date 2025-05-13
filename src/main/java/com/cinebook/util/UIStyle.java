package com.cinebook.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Utility class for consistent modern UI styling across the application.
 */
public class UIStyle {
    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(63, 81, 181);    // Indigo
    public static final Color PRIMARY_DARK = new Color(48, 63, 159);     // Dark Indigo
    public static final Color PRIMARY_LIGHT = new Color(121, 134, 203);  // Light Indigo
    public static final Color ACCENT_COLOR = new Color(255, 64, 129);    // Pink
    
    public static final Color ERROR_COLOR = new Color(244, 67, 54);      // Red
    public static final Color WARNING_COLOR = new Color(255, 152, 0);    // Orange
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);    // Green
    public static final Color INFO_COLOR = new Color(3, 169, 244);       // Light Blue
    
    public static final Color BACKGROUND_COLOR = new Color(250, 250, 250); // Off-white
    public static final Color SURFACE_COLOR = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);      // Dark gray
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117); // Medium gray
    
    // Font settings
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    
    // Border settings
    public static final int BORDER_RADIUS = 8;
    public static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
        new LineBorder(new Color(220, 220, 220), 1, true),
        new EmptyBorder(10, 10, 10, 10)
    );
    
    /**
     * Applies modern styling to a button.
     *
     * @param button The button to style
     * @param isPrimary Whether this is a primary button
     * @return The styled button
     */
    public static JButton styleButton(JButton button, boolean isPrimary) {
        if (isPrimary) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(SURFACE_COLOR);
            button.setForeground(PRIMARY_COLOR);
            button.setBorder(new LineBorder(PRIMARY_COLOR, 1, true));
        }
        
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        
        return button;
    }
    
    /**
     * Applies modern styling to a label.
     *
     * @param label The label to style
     * @param isTitle Whether this is a title label
     * @return The styled label
     */
    public static JLabel styleLabel(JLabel label, boolean isTitle) {
        if (isTitle) {
            label.setFont(TITLE_FONT);
        } else {
            label.setFont(BODY_FONT);
        }
        
        label.setForeground(TEXT_PRIMARY);
        
        return label;
    }
    
    /**
     * Applies modern styling to a panel.
     *
     * @param panel The panel to style
     * @return The styled panel
     */
    public static JPanel stylePanel(JPanel panel) {
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(PANEL_BORDER);
        
        return panel;
    }
    
    /**
     * Creates a styled header panel with title.
     *
     * @param title The title text
     * @return The styled header panel
     */
    public static JPanel createHeaderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        panel.add(titleLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a styled card panel for content.
     *
     * @return The styled card panel
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        return panel;
    }
    
    /**
     * Darkens a color by the specified factor.
     *
     * @param color The color to darken
     * @param factor The darkening factor (0.0 to 1.0)
     * @return The darkened color
     */
    public static Color darkenColor(Color color, float factor) {
        if (factor < 0 || factor > 1) {
            throw new IllegalArgumentException("Factor must be between 0.0 and 1.0");
        }
        
        int r = Math.max(0, (int)(color.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - factor)));
        
        return new Color(r, g, b, color.getAlpha());
    }
    
    /**
     * Lightens a color by the specified factor.
     *
     * @param color The color to lighten
     * @param factor The lightening factor (0.0 to 1.0)
     * @return The lightened color
     */
    public static Color lightenColor(Color color, float factor) {
        if (factor < 0 || factor > 1) {
            throw new IllegalArgumentException("Factor must be between 0.0 and 1.0");
        }
        
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        
        return new Color(r, g, b, color.getAlpha());
    }
}