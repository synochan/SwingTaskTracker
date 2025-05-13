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

    /**
     * Constructor for the seat selection panel.
     * 
     * @param mainFrame The main application frame
     * @param userController The user controller
     * @param screeningController The screening controller
     * @param reservationController The reservation controller
     */
    public SeatSelectionPanel(MainFrame mainFrame, UserController userController, 
                              ScreeningController screeningController, 
                              ReservationController reservationController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.screeningController = screeningController;
        this.reservationController = reservationController;
        
        this.seatButtons = new ArrayList<>();
        this.selectedSeatIds = new ArrayList<>();
        
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
            System.out.println("Attempting to add seats to reservation: " + selectedSeatIds);
            
            // Guard against empty or null selected seat IDs
            if (selectedSeatIds == null || selectedSeatIds.isEmpty()) {
                System.err.println("ERROR: Selected seat IDs is empty or null");
                DialogManager.showErrorDialog(
                    mainFrame,
                    "Please select at least one seat before continuing.",
                    "No Seats Selected"
                );
                return;
            }
            
            boolean success = reservationController.addSeatsToReservation(selectedSeatIds);
            System.out.println("Reservation success: " + success);
            
            if (success) {
                mainFrame.getConcessionPanel().initialize();
                mainFrame.navigateTo(MainFrame.CONCESSION_PANEL);
            } else {
                System.err.println("FAILED: Could not add seats to reservation");
                // Use the modern dialog manager for error display
                DialogManager.showErrorDialog(
                    mainFrame,
                    "Unable to add seats to your reservation. This might happen if seats have been reserved by another user. Please try selecting different seats.",
                    "Reservation Error"
                );
                
                // Refresh the seat map to show the latest seat statuses
                refreshSeatMap();
            }
        } else {
            DialogManager.showInfoDialog(
                mainFrame,
                "Please select at least one seat before continuing.",
                "No Seats Selected"
            );
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
        for (ActionListener al : arPreviewButton.getActionListeners()) {
            arPreviewButton.removeActionListener(al);
        }
        
        // Add our consolidated action handlers
        continueButton.addActionListener(e -> handleContinueButtonAction());
        cancelButton.addActionListener(e -> handleCancelButtonAction());
        arPreviewButton.addActionListener(e -> showARPreview());
        
        System.out.println("All button listeners registered properly");
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
        
        // Title label
        JLabel titleLabel = new JLabel("Select Your Seats");
        titleLabel.setFont(UIStyle.HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        leftPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Create seat map container with padding
        JPanel seatMapContainer = new JPanel(new BorderLayout());
        seatMapContainer.setBackground(UIStyle.BACKGROUND_COLOR);
        seatMapContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create seat map panel - this will be populated in loadSeatMap()
        seatMapPanel = new JPanel();
        seatMapPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        seatMapContainer.add(seatMapPanel, BorderLayout.CENTER);
        
        // Create screen label
        JPanel screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        screenPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));
        
        screenLabel = new JLabel("SCREEN");
        screenLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        screenLabel.setForeground(UIStyle.TEXT_SECONDARY);
        screenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(UIStyle.ACCENT_COLOR);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        screenPanel.add(screenLabel, BorderLayout.NORTH);
        seatMapContainer.add(screenPanel, BorderLayout.NORTH);
        
        leftPanel.add(seatMapContainer, BorderLayout.CENTER);
        
        // 2. RIGHT AREA (20% width) - INFO SIDEBAR
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(UIStyle.SURFACE_COLOR);
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sidePanel.setPreferredSize(new Dimension(280, getHeight()));
        
        // Movie info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(UIStyle.SURFACE_COLOR);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title
        movieLabel = new JLabel();
        movieLabel.setFont(UIStyle.SUBHEADER_FONT);
        movieLabel.setForeground(UIStyle.TEXT_PRIMARY);
        infoPanel.add(movieLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Cinema
        cinemaLabel = new JLabel();
        cinemaLabel.setFont(UIStyle.BODY_FONT);
        cinemaLabel.setForeground(UIStyle.TEXT_SECONDARY);
        infoPanel.add(cinemaLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        
        // Date & Time
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(UIStyle.BODY_FONT);
        dateTimeLabel.setForeground(UIStyle.TEXT_SECONDARY);
        infoPanel.add(dateTimeLabel);
        
        sidePanel.add(infoPanel);
        
        // Create AR preview button
        JPanel arPreviewContainer = new JPanel(new BorderLayout());
        arPreviewContainer.setBackground(UIStyle.SURFACE_COLOR);
        arPreviewContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        arPreviewButton = new JButton("View in AR");
        arPreviewButton.setFont(UIStyle.BUTTON_FONT);
        arPreviewButton.setBackground(UIStyle.SECONDARY_COLOR);
        arPreviewButton.setForeground(Color.WHITE);
        arPreviewButton.setFocusPainted(false);
        arPreviewButton.setBorderPainted(false);
        arPreviewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        arPreviewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Try to add an icon for AR view
        try {
            // Modern SVG icon
            File iconFile = new File("resources/icons/ar_view.svg");
            if (iconFile.exists()) {
                ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                arPreviewButton.setIcon(icon);
                arPreviewButton.setText("View seat in AR");
            } else {
                System.err.println("AR icon file not found");
            }
        } catch (Exception e) {
            System.err.println("Error loading AR icon: " + e.getMessage());
        }
        
        // Action listener will be added in registerButtonListeners()
        
        arPreviewContainer.add(arPreviewButton, BorderLayout.CENTER);
        sidePanel.add(arPreviewContainer);
        
        // Selection summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(UIStyle.SURFACE_COLOR);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Label for section
        JLabel summaryLabel = new JLabel("Your Selection");
        summaryLabel.setFont(UIStyle.SUBHEADER_FONT);
        summaryLabel.setForeground(UIStyle.TEXT_PRIMARY);
        summaryPanel.add(summaryLabel);
        summaryPanel.add(Box.createVerticalStrut(15));
        
        // Selected seats
        JPanel seatsPanel = new JPanel(new BorderLayout());
        seatsPanel.setBackground(UIStyle.SURFACE_COLOR);
        JLabel seatsHeaderLabel = new JLabel("Seats:");
        seatsHeaderLabel.setFont(UIStyle.BODY_FONT);
        seatsHeaderLabel.setForeground(UIStyle.TEXT_SECONDARY);
        seatsPanel.add(seatsHeaderLabel, BorderLayout.WEST);
        
        selectedSeatsLabel = new JLabel("None selected");
        selectedSeatsLabel.setFont(UIStyle.BODY_FONT);
        selectedSeatsLabel.setForeground(UIStyle.TEXT_PRIMARY);
        selectedSeatsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        seatsPanel.add(selectedSeatsLabel, BorderLayout.EAST);
        
        summaryPanel.add(seatsPanel);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        // Total price
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setBackground(UIStyle.SURFACE_COLOR);
        JLabel priceHeaderLabel = new JLabel("Total Price:");
        priceHeaderLabel.setFont(UIStyle.BODY_FONT.deriveFont(Font.BOLD));
        priceHeaderLabel.setForeground(UIStyle.TEXT_SECONDARY);
        pricePanel.add(priceHeaderLabel, BorderLayout.WEST);
        
        totalPriceLabel = new JLabel("₱0.00");
        totalPriceLabel.setFont(UIStyle.BODY_FONT.deriveFont(Font.BOLD));
        totalPriceLabel.setForeground(UIStyle.PRIMARY_COLOR);
        totalPriceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pricePanel.add(totalPriceLabel, BorderLayout.EAST);
        
        summaryPanel.add(pricePanel);
        
        sidePanel.add(summaryPanel);
        
        // Legend panel
        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(UIStyle.SURFACE_COLOR);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Label for section
        JLabel legendLabel = new JLabel("Seat Legend");
        legendLabel.setFont(UIStyle.SUBHEADER_FONT);
        legendLabel.setForeground(UIStyle.TEXT_PRIMARY);
        legendPanel.add(legendLabel);
        legendPanel.add(Box.createVerticalStrut(15));
        
        // Legend items will be added in updateLegend()
        
        sidePanel.add(legendPanel);
        
        // Bottom button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(UIStyle.SURFACE_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(UIStyle.BUTTON_FONT);
        
        // Continue button
        continueButton = new JButton("Continue");
        continueButton.setFont(UIStyle.BUTTON_FONT);
        
        // Try to add icons to buttons
        try {
            // Modern SVG icon for continue
            File continueIconFile = new File("resources/icons/continue.svg");
            if (continueIconFile.exists()) {
                ImageIcon icon = new ImageIcon(continueIconFile.getAbsolutePath());
                continueButton.setIcon(icon);
            } else {
                System.err.println("Continue icon not found");
            }
        } catch (Exception e) {
            System.err.println("Continue icon not found: " + e.getMessage());
        }
        
        try {
            // Modern SVG icon for cancel
            File cancelIconFile = new File("resources/icons/cancel.svg");
            if (cancelIconFile.exists()) {
                ImageIcon icon = new ImageIcon(cancelIconFile.getAbsolutePath());
                cancelButton.setIcon(icon);
            } else {
                System.err.println("Cancel icon not found");
            }
        } catch (Exception e) {
            System.err.println("Cancel icon not found: " + e.getMessage());
        }
        
        // Apply modern styling to buttons
        UIStyle.styleButton(continueButton, true);
        UIStyle.styleButton(cancelButton, false);
        
        // Button listeners are centralized in registerButtonListeners() method
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(continueButton);
        
        sidePanel.add(buttonPanel);
        
        // Add to main container
        mainContainer.add(leftPanel, BorderLayout.CENTER);
        mainContainer.add(sidePanel, BorderLayout.EAST);
        
        // Add main container to this panel
        add(mainContainer, BorderLayout.CENTER);
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
            
            // Clear any previous selection
            selectedSeatIds.clear();
            
            // Update information labels
            movieLabel.setText(currentScreening.getMovieTitle());
            cinemaLabel.setText(currentScreening.getCinemaName());
            dateTimeLabel.setText(currentScreening.getFormattedDateTime());
            
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
                
                // Get a fresh copy of the screening to ensure we have the latest seat status
                currentScreening = screeningController.getScreeningById(currentScreening.getId());
                
                // Reload the seat map
                loadSeatMap();
                
                // Update the selection summary
                updateSelectionSummary();
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
     * Loads and displays the seat map for the current screening.
     */
    private void loadSeatMap() {
        // Clear any existing components
        seatMapPanel.removeAll();
        seatButtons.clear();
        
        if (currentScreening == null) {
            return;
        }
        
        // Use grid layout based on the cinema configuration
        int rows = 0;
        int cols = 0;
        
        // Map seats to determine the grid dimensions
        List<Seat> seats = screeningController.getSeatsByScreening(currentScreening.getId());
        for (Seat seat : seats) {
            rows = Math.max(rows, seat.getRowNumber());
            cols = Math.max(cols, seat.getColumnNumber());
        }
        
        // Add 1 because row/column numbers are 1-based
        rows++;
        cols++;
        
        // Create the grid layout for the seat map
        seatMapPanel.setLayout(new GridLayout(rows, cols, 5, 5));
        seatMapPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // Create a 2D array to store the seat buttons
        SeatButton[][] seatButtonGrid = new SeatButton[rows][cols];
        
        // Add the seats to the grid - reusing the seats list we already fetched
        for (Seat seat : seats) {
            SeatButton seatButton = new SeatButton(seat);
            seatButtonGrid[seat.getRowNumber()][seat.getColumnNumber()] = seatButton;
            seatButtons.add(seatButton);
        }
        
        // Add the seat buttons to the panel
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seatButtonGrid[i][j] != null) {
                    seatMapPanel.add(seatButtonGrid[i][j]);
                } else {
                    // Empty space
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setOpaque(false);
                    seatMapPanel.add(emptyPanel);
                }
            }
        }
        
        // Update the UI
        seatMapPanel.revalidate();
        seatMapPanel.repaint();
    }
    
    /**
     * Updates the legend panel with seat types and pricing.
     */
    private void updateLegend() {
        // Clear any existing components
        legendPanel.removeAll();
        
        // Legend title
        JLabel legendLabel = new JLabel("Seat Legend");
        legendLabel.setFont(UIStyle.SUBHEADER_FONT);
        legendLabel.setForeground(UIStyle.TEXT_PRIMARY);
        legendPanel.add(legendLabel);
        legendPanel.add(Box.createVerticalStrut(15));
        
        // Create legend items for each seat type
        if (currentScreening != null) {
            // Get unique seat types and their prices
            for (SeatType seatType : SeatType.values()) {
                // Get price for this seat type
                double price = (seatType == SeatType.STANDARD) ? 
                            currentScreening.getStandardSeatPrice() : 
                            currentScreening.getDeluxeSeatPrice();
                
                // Create a legend item
                JPanel legendItem = new JPanel(new BorderLayout(10, 0));
                legendItem.setBackground(UIStyle.SURFACE_COLOR);
                legendItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                
                // Seat indicator
                SeatIndicator indicator = new SeatIndicator(seatType);
                indicator.setPreferredSize(new Dimension(30, 30));
                legendItem.add(indicator, BorderLayout.WEST);
                
                // Seat type name
                JLabel nameLabel = new JLabel(seatType.toString());
                nameLabel.setFont(UIStyle.BODY_FONT);
                nameLabel.setForeground(UIStyle.TEXT_PRIMARY);
                legendItem.add(nameLabel, BorderLayout.CENTER);
                
                // Price
                JLabel priceLabel = new JLabel(String.format("₱%.2f", price));
                priceLabel.setFont(UIStyle.BODY_FONT);
                priceLabel.setForeground(UIStyle.TEXT_SECONDARY);
                priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                legendItem.add(priceLabel, BorderLayout.EAST);
                
                legendPanel.add(legendItem);
                legendPanel.add(Box.createVerticalStrut(10));
            }
            
            // Add reserved seat indicator
            JPanel reservedItem = new JPanel(new BorderLayout(10, 0));
            reservedItem.setBackground(UIStyle.SURFACE_COLOR);
            reservedItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            SeatIndicator reservedIndicator = new SeatIndicator(null); // null for reserved
            reservedIndicator.setPreferredSize(new Dimension(30, 30));
            reservedItem.add(reservedIndicator, BorderLayout.WEST);
            
            JLabel reservedLabel = new JLabel("Reserved");
            reservedLabel.setFont(UIStyle.BODY_FONT);
            reservedLabel.setForeground(UIStyle.TEXT_PRIMARY);
            reservedItem.add(reservedLabel, BorderLayout.CENTER);
            
            legendPanel.add(reservedItem);
            legendPanel.add(Box.createVerticalStrut(10));
            
            // Add selected seat indicator
            JPanel selectedItem = new JPanel(new BorderLayout(10, 0));
            selectedItem.setBackground(UIStyle.SURFACE_COLOR);
            selectedItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            SeatIndicator selectedIndicator = new SeatIndicator(SeatType.STANDARD); // using STANDARD as placeholder
            selectedIndicator.setSelected(true); // Mark as selected for the indicator
            selectedIndicator.setPreferredSize(new Dimension(30, 30));
            selectedItem.add(selectedIndicator, BorderLayout.WEST);
            
            JLabel selectedLabel = new JLabel("Selected");
            selectedLabel.setFont(UIStyle.BODY_FONT);
            selectedLabel.setForeground(UIStyle.TEXT_PRIMARY);
            selectedItem.add(selectedLabel, BorderLayout.CENTER);
            
            legendPanel.add(selectedItem);
        }
        
        // Update the UI
        legendPanel.revalidate();
        legendPanel.repaint();
    }
    
    /**
     * Updates the selection summary with the selected seats and total price.
     */
    private void updateSelectionSummary() {
        if (selectedSeatIds.isEmpty()) {
            selectedSeatsLabel.setText("None selected");
            totalPriceLabel.setText("₱0.00");
        } else {
            // Build the selected seats text
            StringBuilder seatsText = new StringBuilder();
            double totalPrice = 0.0;
            
            // Get the selected seats
            for (SeatButton seatButton : seatButtons) {
                if (selectedSeatIds.contains(seatButton.getSeat().getId())) {
                    if (seatsText.length() > 0) {
                        seatsText.append(", ");
                    }
                    seatsText.append(seatButton.getSeat().getSeatNumber());
                    
                    // Add to total price
                    SeatType seatType = seatButton.getSeat().getSeatType();
                    totalPrice += (seatType == SeatType.STANDARD) ? 
                                currentScreening.getStandardSeatPrice() : 
                                currentScreening.getDeluxeSeatPrice();
                }
            }
            
            selectedSeatsLabel.setText(seatsText.toString());
            totalPriceLabel.setText(String.format("₱%.2f", totalPrice));
        }
    }

    /**
     * Shows the AR preview for the selected seat.
     */
    private void showARPreview() {
        if (selectedSeatIds.isEmpty()) {
            DialogManager.showInfoDialog(
                mainFrame,
                "Please select a seat to preview in AR.",
                "No Seat Selected"
            );
            return;
        }
        
        try {
            // Get the first selected seat for now
            Seat selectedSeat = null;
            for (SeatButton seatButton : seatButtons) {
                if (selectedSeatIds.contains(seatButton.getSeat().getId())) {
                    selectedSeat = seatButton.getSeat();
                    break;
                }
            }
            
            if (selectedSeat == null) {
                return;
            }
            
            // Create and show the AR preview panel
            List<Seat> selectedSeats = new ArrayList<>();
            selectedSeats.add(selectedSeat);
            
            // Get cinema from the controller
            Cinema cinema = screeningController.getAllCinemas().stream()
                .filter(c -> c.getId() == currentScreening.getCinemaId())
                .findFirst()
                .orElse(null);
                
            if (cinema == null) {
                System.err.println("Could not find cinema for AR preview");
                return;
            }
            
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
    
    /**
     * Inner class for seat buttons.
     */
    private class SeatButton extends JButton {
        private Seat seat;
        private boolean isSelected;
        
        public SeatButton(Seat seat) {
            this.seat = seat;
            this.isSelected = false;
            
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            
            // Set the text to the seat number
            setText(seat.getSeatNumber());
            setFont(new Font("SansSerif", Font.BOLD, 11));
            
            // Set background and foreground colors based on seat type and status
            updateAppearance();
            
            // Add mouse listener for hover effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!seat.isReserved()) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!seat.isReserved()) {
                        toggleSelection();
                    }
                }
            });
        }
        
        /**
         * Toggles the selection state of this seat.
         */
        public void toggleSelection() {
            isSelected = !isSelected;
            
            // Update the selected seats list
            if (isSelected) {
                selectedSeatIds.add(seat.getId());
            } else {
                selectedSeatIds.remove(Integer.valueOf(seat.getId()));
            }
            
            // Update the appearance
            updateAppearance();
            
            // Update the selection summary
            updateSelectionSummary();
        }
        
        /**
         * Updates the appearance based on the seat type and selection state.
         */
        private void updateAppearance() {
            Color bgColor;
            Color fgColor;
            
            if (seat.isReserved()) {
                // Reserved seats
                bgColor = UIStyle.DISABLED_COLOR;
                fgColor = UIStyle.TEXT_DISABLED;
            } else if (isSelected) {
                // Selected seats
                bgColor = UIStyle.ACCENT_COLOR;
                fgColor = Color.WHITE;
            } else {
                // Available seats - color based on type
                SeatType seatType = seat.getSeatType();
                if (seatType == SeatType.STANDARD) {
                    bgColor = UIStyle.REGULAR_SEAT_COLOR;
                } else if (seatType == SeatType.DELUXE) {
                    bgColor = UIStyle.PREMIUM_SEAT_COLOR;
                } else {
                    bgColor = UIStyle.REGULAR_SEAT_COLOR;
                }
                fgColor = Color.WHITE;
            }
            
            setBackground(bgColor);
            setForeground(fgColor);
            
            // Enable/disable based on reservation status
            setEnabled(!seat.isReserved());
            
            repaint();
        }
        
        /**
         * Gets the seat associated with this button.
         *
         * @return The seat
         */
        public Seat getSeat() {
            return seat;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Draw the seat
            RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 10, 10);
            
            Color baseColor = getBackground();
            
            // Create a gradient effect
            GradientPaint gp;
            if (seat.isReserved()) {
                // Reserved seats have a flat color
                gp = new GradientPaint(0, 0, baseColor, 0, height, baseColor);
            } else if (isSelected) {
                // Selected seats have a brighter gradient
                gp = new GradientPaint(0, 0, baseColor, 0, height, UIStyle.darkenColor(baseColor, 0.2f));
            } else {
                // Regular seats have a normal gradient
                gp = new GradientPaint(0, 0, baseColor, 0, height, UIStyle.darkenColor(baseColor, 0.4f));
            }
            
            g2.setPaint(gp);
            g2.fill(roundedRectangle);
            
            // Draw the border
            g2.setColor(UIStyle.darkenColor(baseColor, 0.5f));
            g2.draw(roundedRectangle);
            
            // Draw the text
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D textBounds = fm.getStringBounds(getText(), g2);
            
            int textX = (int) ((width - textBounds.getWidth()) / 2);
            int textY = (int) ((height - textBounds.getHeight()) / 2 + fm.getAscent());
            
            g2.setColor(getForeground());
            g2.setFont(getFont());
            g2.drawString(getText(), textX, textY);
            
            g2.dispose();
        }
    }
    
    /**
     * Inner class for seat indicators in the legend.
     */
    private class SeatIndicator extends JPanel {
        private SeatType seatType;
        private boolean isSelected;
        
        public SeatIndicator(SeatType seatType) {
            this.seatType = seatType;
            this.isSelected = false;
            setPreferredSize(new Dimension(20, 20));
        }
        
        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Draw the seat indicator
            RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, 6, 6);
            
            Color baseColor;
            
            if (seatType == null) {
                // Reserved seat
                baseColor = UIStyle.DISABLED_COLOR;
            } else if (isSelected) {
                // Selected seat
                baseColor = UIStyle.ACCENT_COLOR;
            } else {
                // Regular seat types
                if (seatType == SeatType.STANDARD) {
                    baseColor = UIStyle.REGULAR_SEAT_COLOR;
                } else if (seatType == SeatType.DELUXE) {
                    baseColor = UIStyle.PREMIUM_SEAT_COLOR;
                } else {
                    baseColor = UIStyle.REGULAR_SEAT_COLOR;
                }
            }
            
            // Create a gradient effect
            GradientPaint gp = new GradientPaint(0, 0, baseColor, 0, height, UIStyle.darkenColor(baseColor, 0.4f));
            g2.setPaint(gp);
            g2.fill(roundedRectangle);
            
            // Draw the border
            g2.setColor(UIStyle.darkenColor(baseColor, 0.5f));
            g2.draw(roundedRectangle);
            
            g2.dispose();
        }
    }
}