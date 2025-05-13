package com.cinebook.view;

import com.cinebook.controller.AdminController;
import com.cinebook.controller.ReservationController;
import com.cinebook.controller.ScreeningController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Cinema;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;
import com.cinebook.util.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for seat selection during the booking process.
 */
public class SeatSelectionPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private ScreeningController screeningController;
    private ReservationController reservationController;
    
    private Screening currentScreening;
    private List<SeatButton> seatButtons;
    private List<Integer> selectedSeatIds;
    
    // UI Components
    private JPanel seatMapPanel;
    private JPanel legendPanel;
    private JPanel infoPanel;
    private JLabel screenLabel;
    private JLabel movieLabel;
    private JLabel cinemaLabel;
    private JLabel dateTimeLabel;
    private JLabel selectedSeatsLabel;
    private JLabel totalPriceLabel;
    private JButton continueButton;
    private JButton cancelButton;
    private JButton arPreviewButton;
    
    // Constants
    private static final int MAX_SEATS_PER_BOOKING = 6;
    
    /**
     * Constructor for SeatSelectionPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public SeatSelectionPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.screeningController = new ScreeningController();
        this.reservationController = new ReservationController();
        this.seatButtons = new ArrayList<>();
        this.selectedSeatIds = new ArrayList<>();
        
        // Setup panel properties
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setBackground(UIStyle.BACKGROUND_COLOR);
        
        // Create the two main panels - seat map and sidebar
        createPanels();
        
        // Register button listeners once during initialization
        registerButtonListeners();
    }
    
    /**
     * Action handler for the continue button.
     * This is a consolidated method to avoid duplicate listeners.
     */
    private void handleContinueButtonAction() {
        if (!selectedSeatIds.isEmpty()) {
            boolean success = reservationController.addSeatsToReservation(selectedSeatIds);
            
            if (success) {
                mainFrame.getConcessionPanel().initialize();
                mainFrame.navigateTo(MainFrame.CONCESSION_PANEL);
            } else {
                // Use the modern dialog manager for error display
                DialogManager.showErrorDialog(
                    mainFrame,
                    "Unable to add seats to your reservation. This might happen if seats have been reserved by another user. Please try selecting different seats.",
                    "Reservation Error"
                );
                
                // Refresh the seat map to show the latest seat statuses
                refreshSeatMap();
            }
        }
    }
    
    /**
     * Action handler for the cancel button.
     * This is a consolidated method to avoid duplicate listeners.
     */
    private void handleCancelButtonAction() {
        int response = JOptionPane.showConfirmDialog(
            mainFrame,
            "Are you sure you want to cancel this reservation?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            reservationController.cancelReservationProcess();
            mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
        }
    }
    
    /**
     * Registers action listeners for all buttons to avoid duplicate listeners.
     * This should be called only once to set up all button actions.
     */
    private void registerButtonListeners() {
        // Remove any existing listeners to prevent duplicates
        for (ActionListener al : continueButton.getActionListeners()) {
            continueButton.removeActionListener(al);
        }
        for (ActionListener al : cancelButton.getActionListeners()) {
            cancelButton.removeActionListener(al);
        }
        
        // Add our consolidated action handlers
        continueButton.addActionListener(e -> handleContinueButtonAction());
        cancelButton.addActionListener(e -> handleCancelButtonAction());
    }
    
    /**
     * Creates all panel components with the new side-by-side layout
     */
    private void createPanels() {
        // MAIN CONTAINER - split into seat area and sidebar
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // 1. LEFT AREA (80% width) - SEAT MAP
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // Create title at top
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIStyle.PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("Select Your Seats", JLabel.LEFT);
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Create seat map area with screen at top
        JPanel seatArea = new JPanel(new BorderLayout(0, 0));
        seatArea.setBackground(UIStyle.BACKGROUND_COLOR);
        seatArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        
        // Screen panel
        JPanel screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        screenPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        screenLabel = new JLabel("SCREEN", JLabel.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(UIStyle.PRIMARY_DARK);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setFont(UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD));
        screenLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        
        // Screen shadow for 3D effect
        JPanel shadowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Creating a gradient from dark to transparent
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 60),
                    0, getHeight(), new Color(0, 0, 0, 0)
                );
                
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        shadowPanel.setOpaque(false);
        shadowPanel.setPreferredSize(new Dimension(0, 15));
        
        screenPanel.add(screenLabel, BorderLayout.CENTER);
        screenPanel.add(shadowPanel, BorderLayout.SOUTH);
        
        // Seat map panel - this is where seats will be displayed
        seatMapPanel = new JPanel();
        seatMapPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // Add screen and seat map to the seat area
        seatArea.add(screenPanel, BorderLayout.NORTH);
        seatArea.add(seatMapPanel, BorderLayout.CENTER);
        
        // Add title and seat area to left panel
        leftPanel.add(titlePanel, BorderLayout.NORTH);
        leftPanel.add(seatArea, BorderLayout.CENTER);
        
        // 2. RIGHT SIDEBAR (20% width) - INFO AND CONTROLS

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(UIStyle.SURFACE_COLOR);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, UIStyle.PRIMARY_LIGHT),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Fixed width for sidebar
        rightPanel.setPreferredSize(new Dimension(280, 0));
        
        // 2.1 Screening info panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBackground(UIStyle.SURFACE_COLOR);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT),
                "Screening Information", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD, 14),
                UIStyle.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setMaximumSize(new Dimension(280, 120));
        
        movieLabel = new JLabel("Movie: ");
        cinemaLabel = new JLabel("Cinema: ");
        dateTimeLabel = new JLabel("Date & Time: ");
        
        movieLabel.setFont(UIStyle.BODY_FONT);
        cinemaLabel.setFont(UIStyle.BODY_FONT);
        dateTimeLabel.setFont(UIStyle.BODY_FONT);
        
        infoPanel.add(movieLabel);
        infoPanel.add(cinemaLabel);
        infoPanel.add(dateTimeLabel);
        
        // 2.2 Selection summary panel
        JPanel selectionPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        selectionPanel.setBackground(UIStyle.SURFACE_COLOR);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT),
                "Your Selection", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD, 14),
                UIStyle.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        selectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectionPanel.setMaximumSize(new Dimension(280, 90));
        
        selectedSeatsLabel = new JLabel("Selected Seats: None");
        totalPriceLabel = new JLabel("Total Price: ₱0.00");
        
        selectedSeatsLabel.setFont(UIStyle.BODY_FONT);
        totalPriceLabel.setFont(UIStyle.BODY_FONT.deriveFont(Font.BOLD));
        
        selectionPanel.add(selectedSeatsLabel);
        selectionPanel.add(totalPriceLabel);
        
        // 2.3 Legend panel
        legendPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        legendPanel.setBackground(UIStyle.SURFACE_COLOR);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT),
                "Legend", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD, 14),
                UIStyle.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.setMaximumSize(new Dimension(280, 180));
        
        // 2.4 AR Preview button
        arPreviewButton = new JButton("AR Seat Preview");
        // Don't use icon as it might not exist
        arPreviewButton.setEnabled(false);
        arPreviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        arPreviewButton.setMaximumSize(new Dimension(280, 40));
        UIStyle.styleButton(arPreviewButton, false);
        
        arPreviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showARPreview();
            }
        });
        
        // 2.5 Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(UIStyle.SURFACE_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(280, 50));
        
        continueButton = new JButton("Continue");
        continueButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        
        UIStyle.styleButton(continueButton, true);
        UIStyle.styleButton(cancelButton, false);
        
        // Action listeners are now registered in registerButtonListeners()
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(continueButton);
        
        // Add all components to the right panel with spacing
        rightPanel.add(infoPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(selectionPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(legendPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(arPreviewButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createVerticalGlue());
        
        // Add left and right panels to main container
        mainContainer.add(leftPanel, BorderLayout.CENTER);
        mainContainer.add(rightPanel, BorderLayout.EAST);
        
        // Add main container to this panel
        add(mainContainer, BorderLayout.CENTER);
    }
    
    /**
     * Creates a modern title panel for the seat selection screen.
     *
     * @return The created title panel
     */
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIStyle.PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Modern title with icon
        JLabel pageTitle = new JLabel("Select Your Seats", JLabel.CENTER);
        pageTitle.setFont(UIStyle.TITLE_FONT);
        pageTitle.setForeground(Color.WHITE);
        // Use the SVG icon we created
        try {
            ImageIcon icon = new ImageIcon(new File("resources/icons/seat_selection.svg").getAbsolutePath());
            pageTitle.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Seat selection icon not found: " + e.getMessage());
        }
        pageTitle.setIconTextGap(15);
        pageTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        titlePanel.add(pageTitle, BorderLayout.CENTER);
        
        return titlePanel;
    }
    
    /**
     * Creates the side panel with all information and controls.
     *
     * @return The created side panel
     */
    private JPanel createSidePanel() {
        // Create a container for all the side information
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(UIStyle.SURFACE_COLOR);
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        sidePanel.setPreferredSize(new Dimension(300, 0));
        
        // Screening information panel
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        infoPanel.setBackground(UIStyle.SURFACE_COLOR);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1, true), 
                "Screening Information", TitledBorder.LEFT, TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD), UIStyle.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create info labels
        movieLabel = new JLabel("Movie: ");
        movieLabel.setFont(UIStyle.BODY_FONT);
        
        cinemaLabel = new JLabel("Cinema: ");
        cinemaLabel.setFont(UIStyle.BODY_FONT);
        
        dateTimeLabel = new JLabel("Date & Time: ");
        dateTimeLabel.setFont(UIStyle.BODY_FONT);
        
        infoPanel.add(movieLabel);
        infoPanel.add(cinemaLabel);
        infoPanel.add(dateTimeLabel);
        
        // Add to the side panel with some spacing
        sidePanel.add(infoPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Seat selection summary
        JPanel selectionPanel = new JPanel(new BorderLayout(0, 10));
        selectionPanel.setBackground(UIStyle.SURFACE_COLOR);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1, true), 
                "Your Selection", TitledBorder.LEFT, TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD), UIStyle.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Selection info
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        summaryPanel.setBackground(UIStyle.SURFACE_COLOR);
        
        selectedSeatsLabel = new JLabel("Selected Seats: None");
        selectedSeatsLabel.setFont(UIStyle.BODY_FONT);
        
        totalPriceLabel = new JLabel("Total Price: ₱0.00");
        totalPriceLabel.setFont(UIStyle.BODY_FONT);
        
        summaryPanel.add(selectedSeatsLabel);
        summaryPanel.add(totalPriceLabel);
        
        selectionPanel.add(summaryPanel, BorderLayout.CENTER);
        
        // AR Preview button
        arPreviewButton = new JButton("AR Seat Preview");
        // Use the SVG icon we created
        try {
            ImageIcon icon = new ImageIcon(new File("resources/icons/ar_view.svg").getAbsolutePath());
            arPreviewButton.setIcon(icon);
        } catch (Exception e) {
            System.err.println("AR view icon not found: " + e.getMessage());
        }
        arPreviewButton.setEnabled(false);
        UIStyle.styleButton(arPreviewButton, false);
        
        arPreviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showARPreview();
            }
        });
        
        JPanel previewButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previewButtonPanel.setBackground(UIStyle.SURFACE_COLOR);
        previewButtonPanel.add(arPreviewButton);
        
        selectionPanel.add(previewButtonPanel, BorderLayout.SOUTH);
        
        // Add to side panel
        sidePanel.add(selectionPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Legend panel
        legendPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        legendPanel.setBackground(UIStyle.SURFACE_COLOR);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1, true), 
                "Legend", TitledBorder.LEFT, TitledBorder.TOP, 
                UIStyle.SUBTITLE_FONT.deriveFont(Font.BOLD), UIStyle.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add to side panel
        sidePanel.add(legendPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(UIStyle.SURFACE_COLOR);
        
        continueButton = new JButton("Continue");
        // Use the SVG icon we created
        try {
            ImageIcon icon = new ImageIcon(new File("resources/icons/continue.svg").getAbsolutePath());
            continueButton.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Continue icon not found: " + e.getMessage());
        }
        continueButton.setEnabled(false);
        
        cancelButton = new JButton("Cancel");
        // Use the SVG icon we created
        try {
            ImageIcon icon = new ImageIcon(new File("resources/icons/cancel.svg").getAbsolutePath());
            cancelButton.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Cancel icon not found: " + e.getMessage());
        }
        
        // Apply modern styling to buttons
        UIStyle.styleButton(continueButton, true);
        UIStyle.styleButton(cancelButton, false);
        
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        // Button listeners are centralized in registerButtonListeners() method
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(continueButton);
        
        // Add button panel to side panel
        sidePanel.add(buttonPanel);
        
        return sidePanel;
    }
    
    /**
     * @deprecated This method is no longer used. Seat map creation is now handled in createPanels().
     */
    @Deprecated
    private void createSeatMapPanel() {
        // This method is kept for compatibility but is no longer used
        // Seat map creation is now handled in createPanels()
    }
    
    /**
     * Creates a modern legend item with a color box and label.
     *
     * @param text The text for the legend item
     * @param color The color for the legend item
     * @return The created JPanel
     */
    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(UIStyle.SURFACE_COLOR);
        
        // Create a rounded color box
        JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fill rounded rectangle
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
                
                // Draw border
                g2d.setColor(UIStyle.PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
                
                g2d.dispose();
            }
        };
        
        colorBox.setPreferredSize(new Dimension(24, 24));
        colorBox.setOpaque(false);
        
        // Create a stylish label
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.BODY_FONT);
        label.setForeground(UIStyle.TEXT_PRIMARY);
        
        panel.add(colorBox);
        panel.add(label);
        
        return panel;
    }
    
    /**
     * Creates the bottom panel with booking controls.
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Selection info panel
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Selection Summary"),
            new EmptyBorder(5, 5, 5, 5)));
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        selectedSeatsLabel = new JLabel("Selected Seats: None");
        totalPriceLabel = new JLabel("Total Price: ₱0.00");
        
        infoPanel.add(selectedSeatsLabel);
        infoPanel.add(totalPriceLabel);
        
        selectionPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Add AR Preview button to the selection panel with modern styling
        arPreviewButton = new JButton("AR Seat Preview");
        // Use our custom AR icon
        try {
            ImageIcon icon = new ImageIcon(new File("resources/icons/ar_view.svg").getAbsolutePath());
            arPreviewButton.setIcon(icon);
        } catch (Exception e) {
            // Use text only if icon is not available
            System.err.println("AR icon not found: " + e.getMessage());
        }
        arPreviewButton.setEnabled(false); // Initially disabled until seats are selected
        
        // Apply modern styling with accent color
        arPreviewButton.setBackground(UIStyle.ACCENT_COLOR);
        arPreviewButton.setForeground(Color.WHITE);
        arPreviewButton.setFont(UIStyle.BUTTON_FONT);
        arPreviewButton.setBorderPainted(false);
        arPreviewButton.setFocusPainted(false);
        
        arPreviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showARPreview();
            }
        });
        
        // Button panel for AR preview
        JPanel previewButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        previewButtonPanel.add(arPreviewButton);
        selectionPanel.add(previewButtonPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(selectionPanel, BorderLayout.CENTER);
        
        // Button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(UIStyle.SURFACE_COLOR);
        
        continueButton = new JButton("Continue to Concessions");
        continueButton.setEnabled(false);
        cancelButton = new JButton("Cancel");
        
        // Apply modern styling to buttons
        UIStyle.styleButton(continueButton, true);
        UIStyle.styleButton(cancelButton, false);
        
        // Add button actions - using the shared handler
        continueButton.addActionListener(e -> handleContinueButtonAction());
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to cancel this reservation?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (response == JOptionPane.YES_OPTION) {
                    reservationController.cancelReservationProcess();
                    mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
                }
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(continueButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Initializes the seat selection panel for the given screening.
     * This gets fresh data from the database to ensure we have the latest seat status.
     *
     * @param screening The screening to initialize the panel for
     */
    public void initialize(Screening screening) {
        try {
            // Get a fresh copy of the screening from the database to ensure we have latest data
            this.currentScreening = screeningController.getScreeningById(screening.getId());
            if (this.currentScreening == null) {
                // Fallback to the provided screening if database retrieval fails
                this.currentScreening = screening;
            }
            
            // Reset selections
            this.selectedSeatIds.clear();
            this.seatButtons.clear();
            
            // Update movie and screening info
            updateScreeningInfo();
            
            // Load and display seat map with fresh data
            loadSeatMap();
            
            // Update pricing in legend
            updateLegend();
            
            // Update selection summary
            updateSelectionSummary();
            
            // Register button listeners properly
            registerButtonListeners();
            
        } catch (Exception e) {
            e.printStackTrace();
            DialogManager.showErrorDialog(
                mainFrame,
                "An error occurred while initializing the seat map. Please try again.",
                "Initialization Error"
            );
            
            // Use the provided screening if there was an error
            this.currentScreening = screening;
            loadSeatMap();
        }
    }
    
    /**
     * Refreshes the seat map to reflect the latest seat status.
     * This is useful when seat reservation status might have changed.
     */
    private void refreshSeatMap() {
        if (currentScreening != null) {
            try {
                // Clear the current selection
                selectedSeatIds.clear();
                seatButtons.clear();
                
                // Reload the seats
                loadSeatMap();
                
                // Reset selection summary
                updateSelectionSummary();
                
                // Disable action buttons
                continueButton.setEnabled(false);
                arPreviewButton.setEnabled(false);
                
            } catch (Exception e) {
                e.printStackTrace();
                DialogManager.showErrorDialog(
                    mainFrame, 
                    "An error occurred while refreshing the seat map. Please try again.",
                    "Refresh Error"
                );
            }
        }
    }
    
    /**
     * Updates the screening information display.
     */
    private void updateScreeningInfo() {
        if (currentScreening != null) {
            movieLabel.setText("Movie: " + currentScreening.getMovieTitle());
            cinemaLabel.setText("Cinema: " + currentScreening.getCinemaName());
            dateTimeLabel.setText("Date & Time: " + currentScreening.getFormattedDateTime());
        } else {
            movieLabel.setText("Movie: ");
            cinemaLabel.setText("Cinema: ");
            dateTimeLabel.setText("Date & Time: ");
        }
    }
    
    /**
     * Updates the legend with current pricing and modern styling.
     */
    private void updateLegend() {
        // Remove all components
        legendPanel.removeAll();
        
        // Create a stylish title
        JLabel titleLabel = new JLabel("Legend");
        titleLabel.setFont(UIStyle.SUBTITLE_FONT);
        titleLabel.setForeground(UIStyle.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create a panel for legend items with grid layout
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 15, 8));
        itemsPanel.setBackground(UIStyle.SURFACE_COLOR);
        
        // Create legend items with improved colors
        JPanel availablePanel = createLegendItem("Available", Color.WHITE);
        JPanel selectedPanel = createLegendItem("Selected", UIStyle.ACCENT_COLOR);
        JPanel reservedPanel = createLegendItem("Reserved", UIStyle.ERROR_COLOR);
        JPanel standardPanel = createLegendItem("Standard (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getStandardSeatPrice()) : "0.00") + 
            ")", Color.WHITE);
        JPanel deluxePanel = createLegendItem("Deluxe (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getDeluxeSeatPrice()) : "0.00") + 
            ")", new Color(255, 220, 220)); // Light pink for deluxe
        
        // Add items to the items panel
        itemsPanel.add(availablePanel);
        itemsPanel.add(selectedPanel);
        itemsPanel.add(reservedPanel);
        itemsPanel.add(standardPanel);
        itemsPanel.add(deluxePanel);
        
        // Add to legend panel with proper layout
        legendPanel.setLayout(new BorderLayout());
        legendPanel.add(titleLabel, BorderLayout.NORTH);
        legendPanel.add(itemsPanel, BorderLayout.CENTER);
        
        // Add stylish border
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Ensure UI is updated
        legendPanel.revalidate();
        legendPanel.repaint();
    }
    
    /**
     * Loads and displays the seat map for the current screening with modern responsive design.
     */
    private void loadSeatMap() {
        // Clear existing seat map
        seatMapPanel.removeAll();
        seatMapPanel.setLayout(new BorderLayout());
        
        if (currentScreening != null) {
            List<Seat> seats = screeningController.getSeatsByScreening(currentScreening.getId());
            
            if (seats.isEmpty()) {
                JLabel noSeatsLabel = new JLabel("No seats available for this screening.");
                noSeatsLabel.setFont(UIStyle.SUBTITLE_FONT);
                noSeatsLabel.setForeground(UIStyle.TEXT_PRIMARY);
                noSeatsLabel.setHorizontalAlignment(JLabel.CENTER);
                seatMapPanel.add(noSeatsLabel, BorderLayout.CENTER);
            } else {
                // Get cinema dimensions
                int totalRows = 0;
                int seatsPerRow = 0;
                
                for (Seat seat : seats) {
                    if (seat.getRowNumber() > totalRows) {
                        totalRows = seat.getRowNumber();
                    }
                    if (seat.getColumnNumber() > seatsPerRow) {
                        seatsPerRow = seat.getColumnNumber();
                    }
                }
                
                // Calculate adaptive size for seats
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int maxWidth = (int)(screenSize.width * 0.8); // Use 80% of screen width as reference
                int maxHeight = (int)(screenSize.height * 0.6); // Use 60% of screen height as reference
                
                int optimalSeatSize = Math.min(
                    maxWidth / (seatsPerRow + 2), 
                    maxHeight / (totalRows + 2)
                );
                // Cap seat size between reasonable limits
                int seatSize = Math.max(30, Math.min(optimalSeatSize, 50));
                
                // Main panel for seat map with sized gap
                JPanel seatsContainer = new JPanel(new GridBagLayout());
                seatsContainer.setBackground(UIStyle.BACKGROUND_COLOR);
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(2, 2, 2, 2); // Small gap between seats
                
                // Add empty top-left corner
                gbc.gridx = 0;
                gbc.gridy = 0;
                seatsContainer.add(new JLabel(), gbc);
                
                // Add column headers with styling
                for (int col = 1; col <= seatsPerRow; col++) {
                    gbc.gridx = col;
                    gbc.gridy = 0;
                    JLabel colLabel = new JLabel(String.valueOf(col), JLabel.CENTER);
                    colLabel.setFont(UIStyle.BODY_FONT);
                    colLabel.setForeground(UIStyle.TEXT_PRIMARY);
                    colLabel.setPreferredSize(new Dimension(seatSize, 20));
                    seatsContainer.add(colLabel, gbc);
                }
                
                // Create a 2D array to store seats by position
                Seat[][] seatGrid = new Seat[totalRows + 1][seatsPerRow + 1];
                for (Seat seat : seats) {
                    seatGrid[seat.getRowNumber()][seat.getColumnNumber()] = seat;
                }
                
                // Add rows with row labels and seat buttons
                for (int row = 1; row <= totalRows; row++) {
                    // Add row label with styling
                    gbc.gridx = 0;
                    gbc.gridy = row;
                    JLabel rowLabel = new JLabel(getRowLabel(row), JLabel.CENTER);
                    rowLabel.setFont(UIStyle.BODY_FONT);
                    rowLabel.setForeground(UIStyle.TEXT_PRIMARY);
                    rowLabel.setPreferredSize(new Dimension(20, seatSize));
                    seatsContainer.add(rowLabel, gbc);
                    
                    // Add seats for this row
                    for (int col = 1; col <= seatsPerRow; col++) {
                        Seat seat = seatGrid[row][col];
                        gbc.gridx = col;
                        
                        if (seat != null) {
                            // Create modernized seat button
                            final SeatButton seatButton = new SeatButton(seat);
                            seatButton.setPreferredSize(new Dimension(seatSize, seatSize));
                            
                            // Add hover effect for better user experience
                            seatButton.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseEntered(MouseEvent e) {
                                    if (!seatButton.getSeat().isReserved()) {
                                        seatButton.setBorder(BorderFactory.createLineBorder(UIStyle.ACCENT_COLOR, 2));
                                    }
                                }
                                
                                @Override
                                public void mouseExited(MouseEvent e) {
                                    seatButton.setBorder(null);
                                }
                            });
                            
                            seatButton.addActionListener(new SeatButtonListener(seatButton));
                            seatButtons.add(seatButton);
                            seatsContainer.add(seatButton, gbc);
                        } else {
                            // Add empty space where there is no seat
                            JPanel emptySpace = new JPanel();
                            emptySpace.setPreferredSize(new Dimension(seatSize, seatSize));
                            emptySpace.setOpaque(false);
                            seatsContainer.add(emptySpace, gbc);
                        }
                    }
                }
                
                // Create a container with padding for better appearance
                JPanel paddedContainer = new JPanel(new BorderLayout());
                paddedContainer.setBackground(UIStyle.BACKGROUND_COLOR);
                paddedContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                paddedContainer.add(seatsContainer, BorderLayout.CENTER);
                
                // Add the seats to a scroll pane (scrollbars appear only if needed)
                JScrollPane scrollPane = new JScrollPane(
                    paddedContainer,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                );
                scrollPane.setBorder(null);
                scrollPane.getViewport().setBackground(UIStyle.BACKGROUND_COLOR);
                
                // Modern scrollbar appearance
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
                
                seatMapPanel.add(scrollPane, BorderLayout.CENTER);
            }
        } else {
            JLabel noScreeningLabel = new JLabel("No screening selected.");
            noScreeningLabel.setFont(UIStyle.SUBTITLE_FONT);
            noScreeningLabel.setForeground(UIStyle.TEXT_PRIMARY);
            noScreeningLabel.setHorizontalAlignment(JLabel.CENTER);
            seatMapPanel.add(noScreeningLabel, BorderLayout.CENTER);
        }
        
        seatMapPanel.revalidate();
        seatMapPanel.repaint();
    }
    
    /**
     * Converts a row number to a letter label (A-Z, then AA, AB, etc.)
     *
     * @param rowNumber The numeric row number
     * @return A letter label for the row
     */
    private String getRowLabel(int rowNumber) {
        if (rowNumber <= 26) {
            return String.valueOf((char) ('A' + rowNumber - 1));
        } else {
            int firstChar = (rowNumber - 1) / 26;
            int secondChar = (rowNumber - 1) % 26;
            return String.valueOf((char) ('A' + firstChar - 1)) + 
                   String.valueOf((char) ('A' + secondChar));
        }
    }
    
    /**
     * Updates the selection summary with selected seats and total price.
     */
    private void updateSelectionSummary() {
        if (selectedSeatIds.isEmpty()) {
            selectedSeatsLabel.setText("Selected Seats: None");
            totalPriceLabel.setText("Total Price: ₱0.00");
            continueButton.setEnabled(false);
            arPreviewButton.setEnabled(false);
        } else {
            // Build selected seats text
            StringBuilder seatsText = new StringBuilder("Selected Seats: ");
            double totalPrice = 0.0;
            
            for (SeatButton button : seatButtons) {
                if (button.isSelected()) {
                    if (seatsText.toString().endsWith(": ")) {
                        seatsText.append(button.getSeat().getSeatNumber());
                    } else {
                        seatsText.append(", ").append(button.getSeat().getSeatNumber());
                    }
                    
                    // Add price based on seat type
                    if (button.getSeat().getSeatType() == SeatType.DELUXE) {
                        totalPrice += currentScreening.getDeluxeSeatPrice();
                    } else {
                        totalPrice += currentScreening.getStandardSeatPrice();
                    }
                }
            }
            
            selectedSeatsLabel.setText(seatsText.toString());
            totalPriceLabel.setText(String.format("Total Price: ₱%.2f", totalPrice));
            continueButton.setEnabled(true);
            arPreviewButton.setEnabled(true); // Enable AR preview when seats are selected
        }
    }
    
    /**
     * Custom JButton class for representing a seat with modern styling.
     */
    private class SeatButton extends JButton {
        private Seat seat;
        private boolean selected;
        private static final int BUTTON_SIZE = 40;
        private static final int CORNER_RADIUS = 10;
        private boolean isHovered = false;
        
        /**
         * Constructor for SeatButton with modern styling.
         *
         * @param seat The seat this button represents
         */
        public SeatButton(Seat seat) {
            this.seat = seat;
            this.selected = false;
            
            setText(seat.getSeatNumber());
            setMargin(new Insets(0, 0, 0, 0));
            setFocusPainted(false);
            setContentAreaFilled(false); // We'll do custom painting
            setOpaque(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
            setFont(UIStyle.BODY_FONT.deriveFont(Font.BOLD, 11f));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Add hover effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!seat.isReserved()) {
                        isHovered = true;
                        repaint();
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
            
            updateAppearance();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int width = getWidth() - 1;
            int height = getHeight() - 1;
            
            // Create seat shape
            RoundRectangle2D seatShape = new RoundRectangle2D.Double(0, 0, width, height, CORNER_RADIUS, CORNER_RADIUS);
            
            // Add bottom shadow for 3D effect
            if (!seat.isReserved() && !selected) {
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 2, CORNER_RADIUS, CORNER_RADIUS));
            }
            
            // Fill with appropriate color
            if (seat.isReserved()) {
                // Reserved seats
                g2d.setColor(UIStyle.ERROR_COLOR);
            } else if (selected) {
                // Selected seats
                g2d.setColor(UIStyle.ACCENT_COLOR);
            } else if (seat.getSeatType() == SeatType.DELUXE) {
                // Deluxe seats - use more appealing gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0,
                    new Color(255, 150, 150),
                    0, height,
                    new Color(255, 100, 100)
                );
                g2d.setPaint(gradient);
            } else {
                // Standard seats - with gradient
                Color baseColor = isHovered ? UIStyle.PRIMARY_LIGHT : UIStyle.SURFACE_COLOR;
                GradientPaint gradient = new GradientPaint(
                    0, 0,
                    baseColor,
                    0, height,
                    UIStyle.darkenColor(baseColor, 0.1f)
                );
                g2d.setPaint(gradient);
            }
            
            g2d.fill(seatShape);
            
            // Draw border with slight glow effect if hovered
            if (isHovered && !seat.isReserved() && !selected) {
                // Glow effect
                g2d.setColor(new Color(UIStyle.ACCENT_COLOR.getRed(), 
                                       UIStyle.ACCENT_COLOR.getGreen(), 
                                       UIStyle.ACCENT_COLOR.getBlue(), 100));
                g2d.setStroke(new BasicStroke(3f));
                g2d.draw(seatShape);
            }
            
            // Standard border
            if (!seat.isReserved()) {
                g2d.setColor(selected ? UIStyle.darkenColor(UIStyle.ACCENT_COLOR, 0.2f) : UIStyle.PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.draw(seatShape);
            }
            
            // Draw text
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(getText(), g2d);
            
            int textX = (int)((width - rect.getWidth()) / 2);
            int textY = (int)((height - rect.getHeight()) / 2 + fm.getAscent());
            
            // Text color based on seat state
            if (seat.isReserved() || selected) {
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(UIStyle.TEXT_PRIMARY);
            }
            
            g2d.drawString(getText(), textX, textY);
            
            // If reserved, draw X
            if (seat.isReserved()) {
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new BasicStroke(1.5f));
                int margin = 10;
                g2d.drawLine(margin, margin, width - margin, height - margin);
                g2d.drawLine(width - margin, margin, margin, height - margin);
            }
            
            g2d.dispose();
        }
        
        /**
         * Updates the button appearance based on reservation and selection status.
         */
        public void updateAppearance() {
            if (seat.isReserved()) {
                setEnabled(false);
                setToolTipText("This seat is already reserved");
            } else if (selected) {
                setEnabled(true);
                setToolTipText("Selected seat");
            } else {
                setEnabled(true);
                setToolTipText(seat.getSeatType() + " seat - ₱" + 
                    (seat.getSeatType() == SeatType.DELUXE ? 
                        currentScreening.getDeluxeSeatPrice() : 
                        currentScreening.getStandardSeatPrice()));
            }
            repaint();
        }
        
        /**
         * Gets the seat this button represents.
         *
         * @return The seat
         */
        public Seat getSeat() {
            return seat;
        }
        
        /**
         * Checks if this seat is selected.
         *
         * @return true if selected, false otherwise
         */
        public boolean isSelected() {
            return selected;
        }
        
        /**
         * Sets the selection status of this seat.
         *
         * @param selected The new selection status
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
            updateAppearance();
        }
    }
    
    /**
     * ActionListener for seat buttons.
     */
    private class SeatButtonListener implements ActionListener {
        private SeatButton button;
        
        /**
         * Constructor for SeatButtonListener.
         *
         * @param button The button this listener is for
         */
        public SeatButtonListener(SeatButton button) {
            this.button = button;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!button.getSeat().isReserved()) {
                if (button.isSelected()) {
                    // Deselect seat
                    button.setSelected(false);
                    selectedSeatIds.remove(Integer.valueOf(button.getSeat().getId()));
                } else {
                    // Check if maximum seats reached
                    if (selectedSeatIds.size() >= MAX_SEATS_PER_BOOKING) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "You can select a maximum of " + MAX_SEATS_PER_BOOKING + " seats per booking.",
                            "Maximum Seats Reached",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    // Select seat
                    button.setSelected(true);
                    selectedSeatIds.add(button.getSeat().getId());
                }
                
                // Update selection summary
                updateSelectionSummary();
            }
        }
    }
    
    /**
     * Shows the AR seat preview dialog.
     */
    private void showARPreview() {
        if (currentScreening == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "Please select a screening first.",
                "No Screening Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedSeatIds.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                "Please select at least one seat to preview.",
                "No Seats Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected seats
        List<Seat> selectedSeats = new ArrayList<>();
        for (SeatButton button : seatButtons) {
            if (button.isSelected()) {
                selectedSeats.add(button.getSeat());
            }
        }
        
        try {
            // Get the cinema from the DAO using the cinema ID from the screening
            AdminController adminController = new AdminController(userController);
            Cinema cinema = adminController.getCinemaById(currentScreening.getCinemaId());
            
            if (cinema == null) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Could not find cinema information.",
                    "Preview Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create and show the AR preview
            ARSeatPreviewPanel arPreviewPanel = new ARSeatPreviewPanel(
                mainFrame, 
                selectedSeats, 
                cinema,
                currentScreening.getMovieTitle()
            );
            
            // Show the preview
            arPreviewPanel.showPreview();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                "Error loading AR preview: " + e.getMessage(),
                "Preview Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
