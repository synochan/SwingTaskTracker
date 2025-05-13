package com.cinebook.view;

import com.cinebook.util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

/**
 * A modern splash screen for the application.
 */
public class SplashScreen extends JWindow {
    private static final int SPLASH_WIDTH = 500;
    private static final int SPLASH_HEIGHT = 300;
    private static final int PROGRESS_HEIGHT = 5;
    private static final int BORDER_RADIUS = 15;

    private JProgressBar progressBar;
    
    /**
     * Constructor for the splash screen.
     */
    public SplashScreen() {
        setSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        setLocationRelativeTo(null); // Center on screen
        
        // Create content panel with custom painting
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background with gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, UIStyle.PRIMARY_DARK,
                    0, getHeight(), UIStyle.PRIMARY_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS));
                
                // Draw application name
                g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
                g2d.setColor(Color.WHITE);
                
                String appName = "CineBook CDO";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(appName);
                g2d.drawString(appName, (getWidth() - textWidth) / 2, getHeight() / 3);
                
                // Draw tagline
                g2d.setFont(new Font("SansSerif", Font.ITALIC, 20));
                String tagline = "Online Movie Ticket Booking";
                textWidth = fm.stringWidth(tagline);
                g2d.drawString(tagline, (getWidth() - textWidth) / 2, getHeight() / 2);
                
                // Try to draw app icon
                try {
                    File iconFile = new File("resources/icons/app_icon.svg");
                    if (iconFile.exists()) {
                        ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                        Image img = icon.getImage();
                        int iconSize = 80;
                        g2d.drawImage(img, (getWidth() - iconSize) / 2, getHeight() / 5 - iconSize, iconSize, iconSize, this);
                    }
                } catch (Exception e) {
                    // Ignore if icon cannot be loaded
                }
                
                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        
        // Create progress bar
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setForeground(Color.WHITE);
        progressBar.setBackground(UIStyle.PRIMARY_DARK);
        progressBar.setPreferredSize(new Dimension(SPLASH_WIDTH, PROGRESS_HEIGHT));
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        
        // Add version information
        JLabel versionLabel = new JLabel("Version 1.0");
        versionLabel.setForeground(Color.WHITE);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add copyright information
        JLabel copyrightLabel = new JLabel("© 2025 CineBook CDO");
        copyrightLabel.setForeground(Color.WHITE);
        copyrightLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(versionLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(copyrightLabel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(contentPanel);
        
        // Make the window shape match the rounded corners
        setShape(new RoundRectangle2D.Double(0, 0, SPLASH_WIDTH, SPLASH_HEIGHT, BORDER_RADIUS, BORDER_RADIUS));
    }
    
    /**
     * Updates the progress bar.
     *
     * @param progress The progress value (0-100)
     */
    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }
    
    /**
     * Shows the splash screen.
     */
    public void showSplash() {
        setVisible(true);
    }
    
    /**
     * Closes the splash screen.
     */
    public void closeSplash() {
        setVisible(false);
        dispose();
    }
}