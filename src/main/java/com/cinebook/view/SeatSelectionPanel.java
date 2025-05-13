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
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create and add components
        createTopPanel();
        createSeatMapPanel();
        createBottomPanel();
    }
    
    /**
     * Creates the top panel with movie and screening information.
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Title
        JLabel pageTitle = new JLabel("Select Your Seats", JLabel.CENTER);
        pageTitle.setFont(new Font("Serif", Font.BOLD, 24));
        topPanel.add(pageTitle, BorderLayout.NORTH);
        
        // Movie and screening info
        infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Screening Information"),
            new EmptyBorder(5, 5, 5, 5)));
        
        movieLabel = new JLabel("Movie: ");
        cinemaLabel = new JLabel("Cinema: ");
        dateTimeLabel = new JLabel("Date & Time: ");
        
        infoPanel.add(movieLabel);
        infoPanel.add(cinemaLabel);
        infoPanel.add(dateTimeLabel);
        
        topPanel.add(infoPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the seat map panel.
     */
    private void createSeatMapPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Screen label with modern styling
        JPanel screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        screenPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        screenLabel = new JLabel("SCREEN", JLabel.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(UIStyle.PRIMARY_DARK);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        screenLabel.setPreferredSize(new Dimension(0, 40));
        screenLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));
        
        // Add a subtle shadow effect
        JPanel shadowPanel = new JPanel();
        shadowPanel.setBackground(UIStyle.PRIMARY_LIGHT);
        shadowPanel.setPreferredSize(new Dimension(0, 4));
        
        screenPanel.add(screenLabel, BorderLayout.NORTH);
        screenPanel.add(shadowPanel, BorderLayout.CENTER);
        
        centerPanel.add(screenPanel, BorderLayout.NORTH);
        
        // Seat map panel with adaptive sizing and no scrolling
        seatMapPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                if (isVisible()) {
                    // Get the parent container size
                    Container parent = getParent();
                    if (parent != null) {
                        Insets insets = parent.getInsets();
                        int width = parent.getWidth() - insets.left - insets.right - 20;
                        int height = parent.getHeight() - insets.top - insets.bottom - 20;
                        return new Dimension(width, height);
                    }
                }
                return super.getPreferredSize();
            }
        };
        seatMapPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        seatMapPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        
        // Add directly to center panel for full-screen layout
        centerPanel.add(seatMapPanel, BorderLayout.CENTER);
        
        // Legend panel with modern styling
        legendPanel = new JPanel();
        legendPanel.setBackground(UIStyle.SURFACE_COLOR);
        
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
        
        itemsPanel.add(availablePanel);
        itemsPanel.add(selectedPanel);
        itemsPanel.add(reservedPanel);
        itemsPanel.add(standardPanel);
        itemsPanel.add(deluxePanel);
        
        // Main panel with title and items
        legendPanel.setLayout(new BorderLayout());
        legendPanel.add(titleLabel, BorderLayout.NORTH);
        legendPanel.add(itemsPanel, BorderLayout.CENTER);
        
        // Add stylish border
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.PRIMARY_LIGHT, 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        centerPanel.add(legendPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
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
            arPreviewButton.setIcon(new ImageIcon(new File("resources/icons/ar_icon.svg").getAbsolutePath()));
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
        
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        });
        
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
