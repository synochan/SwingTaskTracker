package com.cinebook.view;

import com.cinebook.controller.ReservationController;
import com.cinebook.controller.ScreeningController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        
        // Screen label
        screenLabel = new JLabel("SCREEN", JLabel.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.LIGHT_GRAY);
        screenLabel.setForeground(Color.BLACK);
        screenLabel.setPreferredSize(new Dimension(0, 30));
        screenLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        centerPanel.add(screenLabel, BorderLayout.NORTH);
        
        // Seat map panel
        seatMapPanel = new JPanel();
        seatMapPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(seatMapPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Legend panel
        legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));
        
        JPanel availablePanel = createLegendItem("Available", Color.WHITE);
        JPanel selectedPanel = createLegendItem("Selected", Color.GREEN);
        JPanel reservedPanel = createLegendItem("Reserved", Color.RED);
        JPanel standardPanel = createLegendItem("Standard (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getStandardSeatPrice()) : "0.00") + 
            ")", Color.WHITE);
        JPanel deluxePanel = createLegendItem("Deluxe (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getDeluxeSeatPrice()) : "0.00") + 
            ")", new Color(255, 220, 220)); // Light pink for deluxe
        
        legendPanel.add(availablePanel);
        legendPanel.add(selectedPanel);
        legendPanel.add(reservedPanel);
        legendPanel.add(standardPanel);
        legendPanel.add(deluxePanel);
        
        centerPanel.add(legendPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a legend item with a color box and label.
     *
     * @param text The text for the legend item
     * @param color The color for the legend item
     * @return The created JPanel
     */
    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JLabel label = new JLabel(text);
        
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
        
        // Add AR Preview button to the selection panel
        arPreviewButton = new JButton("AR Seat Preview");
        // Create a simple icon if the image is not available
        try {
            arPreviewButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ar_icon.png")));
        } catch (Exception e) {
            // Use text only if icon is not available
        }
        arPreviewButton.setEnabled(false); // Initially disabled until seats are selected
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
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        continueButton = new JButton("Continue to Concessions");
        continueButton.setEnabled(false);
        cancelButton = new JButton("Cancel");
        
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!selectedSeatIds.isEmpty()) {
                    boolean success = reservationController.addSeatsToReservation(selectedSeatIds);
                    
                    if (success) {
                        mainFrame.getConcessionPanel().initialize();
                        mainFrame.navigateTo(MainFrame.CONCESSION_PANEL);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Failed to add seats to the reservation. Please try again.",
                            "Reservation Error",
                            JOptionPane.ERROR_MESSAGE);
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
     * Initializes the panel with screening information and seat map.
     *
     * @param screening The screening for which seats are being selected
     */
    public void initialize(Screening screening) {
        this.currentScreening = screening;
        this.selectedSeatIds.clear();
        this.seatButtons.clear();
        
        // Update movie and screening info
        updateScreeningInfo();
        
        // Load and display seat map
        loadSeatMap();
        
        // Update pricing in legend
        updateLegend();
        
        // Update selection summary
        updateSelectionSummary();
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
     * Updates the legend with current pricing.
     */
    private void updateLegend() {
        // Remove all components
        legendPanel.removeAll();
        
        // Add updated legend items
        JPanel availablePanel = createLegendItem("Available", Color.WHITE);
        JPanel selectedPanel = createLegendItem("Selected", Color.GREEN);
        JPanel reservedPanel = createLegendItem("Reserved", Color.RED);
        JPanel standardPanel = createLegendItem("Standard (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getStandardSeatPrice()) : "0.00") + 
            ")", Color.WHITE);
        JPanel deluxePanel = createLegendItem("Deluxe (₱" + 
            (currentScreening != null ? String.format("%.2f", currentScreening.getDeluxeSeatPrice()) : "0.00") + 
            ")", new Color(255, 220, 220)); // Light pink for deluxe
        
        legendPanel.add(availablePanel);
        legendPanel.add(selectedPanel);
        legendPanel.add(reservedPanel);
        legendPanel.add(standardPanel);
        legendPanel.add(deluxePanel);
        
        // Update the UI
        legendPanel.revalidate();
        legendPanel.repaint();
    }
    
    /**
     * Loads and displays the seat map for the current screening.
     */
    private void loadSeatMap() {
        // Clear existing seat map
        seatMapPanel.removeAll();
        
        if (currentScreening != null) {
            List<Seat> seats = screeningController.getSeatsByScreening(currentScreening.getId());
            
            if (seats.isEmpty()) {
                seatMapPanel.add(new JLabel("No seats available for this screening."));
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
                
                // Create grid layout for seat map
                seatMapPanel.setLayout(new GridLayout(totalRows + 1, seatsPerRow + 1, 5, 5));
                
                // Add row labels and empty top-left corner
                seatMapPanel.add(new JLabel()); // Empty top-left corner
                
                // Add column numbers
                for (int col = 1; col <= seatsPerRow; col++) {
                    JLabel colLabel = new JLabel(String.valueOf(col), JLabel.CENTER);
                    seatMapPanel.add(colLabel);
                }
                
                // Create a 2D array to store seats by position
                Seat[][] seatGrid = new Seat[totalRows + 1][seatsPerRow + 1];
                for (Seat seat : seats) {
                    seatGrid[seat.getRowNumber()][seat.getColumnNumber()] = seat;
                }
                
                // Add rows with row labels and seat buttons
                for (int row = 1; row <= totalRows; row++) {
                    // Add row label
                    JLabel rowLabel = new JLabel(getRowLabel(row), JLabel.CENTER);
                    seatMapPanel.add(rowLabel);
                    
                    // Add seats for this row
                    for (int col = 1; col <= seatsPerRow; col++) {
                        Seat seat = seatGrid[row][col];
                        
                        if (seat != null) {
                            SeatButton seatButton = new SeatButton(seat);
                            seatButton.addActionListener(new SeatButtonListener(seatButton));
                            seatButtons.add(seatButton);
                            seatMapPanel.add(seatButton);
                        } else {
                            // Add empty space if no seat
                            seatMapPanel.add(new JLabel());
                        }
                    }
                }
            }
        } else {
            seatMapPanel.add(new JLabel("No screening selected."));
        }
        
        // Update the UI
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
     * Custom JButton class for representing a seat.
     */
    private class SeatButton extends JButton {
        private Seat seat;
        private boolean selected;
        
        /**
         * Constructor for SeatButton.
         *
         * @param seat The seat this button represents
         */
        public SeatButton(Seat seat) {
            this.seat = seat;
            this.selected = false;
            
            setText(seat.getSeatNumber());
            setMargin(new Insets(2, 2, 2, 2));
            setFocusPainted(false);
            updateAppearance();
        }
        
        /**
         * Updates the button appearance based on reservation and selection status.
         */
        public void updateAppearance() {
            if (seat.isReserved()) {
                setBackground(Color.RED);
                setForeground(Color.WHITE);
                setEnabled(false);
                setBorderPainted(false);
                setToolTipText("This seat is already reserved");
            } else if (selected) {
                setBackground(Color.GREEN);
                setForeground(Color.BLACK);
                setBorderPainted(true);
                setToolTipText("Selected seat");
            } else {
                // Set background based on seat type
                if (seat.getSeatType() == SeatType.DELUXE) {
                    setBackground(new Color(255, 220, 220)); // Light pink for deluxe
                } else {
                    setBackground(Color.WHITE);
                }
                setForeground(Color.BLACK);
                setBorderPainted(true);
                setToolTipText(seat.getSeatType() + " seat");
            }
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
        
        // Create and show the AR preview
        ARSeatPreviewPanel arPreviewPanel = new ARSeatPreviewPanel(
            mainFrame, 
            selectedSeats, 
            currentScreening.getCinema(),
            currentScreening.getMovieTitle()
        );
        
        arPreviewPanel.showPreview();
    }
}
