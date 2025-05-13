package com.cinebook.view;

import com.cinebook.controller.ReservationController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Concession;
import com.cinebook.model.Reservation;
import com.cinebook.model.Seat;
import com.cinebook.model.Ticket;
import com.cinebook.util.EmailService;
import com.cinebook.util.PDFGenerator;
import com.cinebook.util.QRCodeGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Panel for displaying booking confirmation and tickets.
 */
public class ConfirmationPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private ReservationController reservationController;
    private EmailService emailService;
    private PDFGenerator pdfGenerator;
    
    // UI Components
    private JLabel pageTitle;
    private JLabel bookingIdLabel;
    private JLabel movieLabel;
    private JLabel cinemaLabel;
    private JLabel dateTimeLabel;
    private JLabel seatsLabel;
    private JTable ticketsTable;
    private DefaultTableModel ticketsTableModel;
    private JButton viewTicketsButton;
    private JButton emailTicketsButton;
    private JButton printTicketsButton;
    private JButton returnToHomeButton;
    
    private Reservation currentReservation;
    private List<Ticket> tickets;
    
    /**
     * Constructor for ConfirmationPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public ConfirmationPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.reservationController = new ReservationController();
        this.emailService = new EmailService();
        this.pdfGenerator = new PDFGenerator();
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create components
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
    }
    
    /**
     * Creates the top panel with title and reservation information.
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Title
        pageTitle = new JLabel("Booking Confirmation", JLabel.CENTER);
        pageTitle.setFont(new Font("Serif", Font.BOLD, 24));
        
        // Success icon/message
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel successIcon = new JLabel("✓"); // Checkmark symbol
        successIcon.setFont(new Font("SansSerif", Font.BOLD, 24));
        successIcon.setForeground(new Color(0, 150, 0)); // Green color
        
        JLabel successMessage = new JLabel("Your booking is confirmed!");
        successMessage.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        messagePanel.add(successIcon);
        messagePanel.add(successMessage);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(pageTitle, BorderLayout.NORTH);
        titlePanel.add(messagePanel, BorderLayout.CENTER);
        
        topPanel.add(titlePanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the center panel with booking details and tickets.
     */
    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Booking details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Booking Details"),
            new EmptyBorder(10, 10, 10, 10)));
        
        bookingIdLabel = new JLabel("Booking ID: ");
        movieLabel = new JLabel("Movie: ");
        cinemaLabel = new JLabel("Cinema: ");
        dateTimeLabel = new JLabel("Date & Time: ");
        seatsLabel = new JLabel("Seats: ");
        
        detailsPanel.add(bookingIdLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(movieLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(cinemaLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(dateTimeLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsPanel.add(seatsLabel);
        
        // Tickets table panel
        JPanel ticketsPanel = new JPanel(new BorderLayout());
        ticketsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Your Tickets"),
            new EmptyBorder(10, 10, 10, 10)));
        
        String[] columns = {"Ticket Code", "Seat", "Type", "Status"};
        ticketsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ticketsTable = new JTable(ticketsTableModel);
        JScrollPane scrollPane = new JScrollPane(ticketsTable);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        
        ticketsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Combine panels
        centerPanel.add(detailsPanel, BorderLayout.NORTH);
        centerPanel.add(ticketsPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the bottom panel with action buttons.
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // User instructions
        JTextArea instructionsArea = new JTextArea(
            "Your tickets have been generated and are ready for use. " +
            "You can view, email, or print your tickets using the options below. " +
            "Please arrive at the cinema at least 15 minutes before the show time. " +
            "Thank you for booking with CineBook CDO!"
        );
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(bottomPanel.getBackground());
        instructionsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        
        bottomPanel.add(instructionsArea, BorderLayout.NORTH);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        viewTicketsButton = new JButton("View Tickets");
        emailTicketsButton = new JButton("Email Tickets");
        printTicketsButton = new JButton("Print Tickets");
        returnToHomeButton = new JButton("Return to Home");
        
        viewTicketsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewTickets();
            }
        });
        
        emailTicketsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emailTickets();
            }
        });
        
        printTicketsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printTickets();
            }
        });
        
        returnToHomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
            }
        });
        
        buttonPanel.add(viewTicketsButton);
        buttonPanel.add(emailTicketsButton);
        buttonPanel.add(printTicketsButton);
        buttonPanel.add(returnToHomeButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Initializes the panel with reservation and ticket information.
     *
     * @param reservationId The ID of the reservation
     */
    public void initialize(int reservationId) {
        // Clear table
        ticketsTableModel.setRowCount(0);
        
        // Get reservation details
        currentReservation = reservationController.getReservationById(reservationId);
        
        if (currentReservation == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "Failed to load reservation details.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
            return;
        }
        
        // Get tickets for this reservation
        tickets = reservationController.getTicketsByReservation(reservationId);
        
        // Update UI with reservation details
        updateReservationDetails();
        
        // Update tickets table
        updateTicketsTable();
    }
    
    /**
     * Updates the reservation details display.
     */
    private void updateReservationDetails() {
        if (currentReservation != null) {
            bookingIdLabel.setText("Booking ID: " + currentReservation.getId());
            movieLabel.setText("Movie: " + currentReservation.getMovieTitle());
            cinemaLabel.setText("Cinema: " + currentReservation.getCinemaName());
            dateTimeLabel.setText("Date & Time: " + currentReservation.getFormattedScreeningTime());
            
            // Format seats as comma-separated list
            StringBuilder seatsText = new StringBuilder("Seats: ");
            List<Seat> seats = currentReservation.getSelectedSeats();
            for (int i = 0; i < seats.size(); i++) {
                if (i > 0) {
                    seatsText.append(", ");
                }
                seatsText.append(seats.get(i).getSeatNumber());
            }
            seatsLabel.setText(seatsText.toString());
        } else {
            bookingIdLabel.setText("Booking ID: ");
            movieLabel.setText("Movie: ");
            cinemaLabel.setText("Cinema: ");
            dateTimeLabel.setText("Date & Time: ");
            seatsLabel.setText("Seats: ");
        }
    }
    
    /**
     * Updates the tickets table.
     */
    private void updateTicketsTable() {
        ticketsTableModel.setRowCount(0);
        
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                Object[] row = {
                    ticket.getTicketCode(),
                    ticket.getSeatNumber(),
                    ticket.getSeatType().toString(),
                    ticket.isUsed() ? "Used" : "Valid"
                };
                ticketsTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Opens a dialog to view ticket details.
     */
    private void viewTickets() {
        if (tickets == null || tickets.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                "No tickets available to view.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Select a ticket to view if multiple tickets
        Ticket ticketToView;
        if (tickets.size() == 1) {
            ticketToView = tickets.get(0);
        } else {
            int selectedRow = ticketsTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < tickets.size()) {
                ticketToView = tickets.get(selectedRow);
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                    "Please select a ticket from the table to view.",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        // Create ticket view dialog
        JDialog ticketDialog = new JDialog(mainFrame, "Ticket Details", true);
        ticketDialog.setLayout(new BorderLayout());
        ticketDialog.setSize(500, 500);
        ticketDialog.setLocationRelativeTo(mainFrame);
        
        // Create ticket panel
        JPanel ticketPanel = new JPanel();
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));
        ticketPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Ticket header
        JLabel titleLabel = new JLabel("CineBook CDO - Movie Ticket");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Ticket code and QR code
        JPanel codePanel = new JPanel(new BorderLayout(10, 0));
        codePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel ticketCodeLabel = new JLabel("Ticket Code: " + ticketToView.getTicketCode());
        ticketCodeLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        // Generate QR code image
        Image qrImage = QRCodeGenerator.generateQRCodeImage(ticketToView.getTicketCode(), 150, 150);
        JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
        
        codePanel.add(ticketCodeLabel, BorderLayout.NORTH);
        codePanel.add(qrLabel, BorderLayout.CENTER);
        
        // Ticket details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Ticket Information"),
            new EmptyBorder(10, 10, 10, 10)));
        
        detailsPanel.add(new JLabel("Movie: " + ticketToView.getMovieTitle()));
        detailsPanel.add(new JLabel("Cinema: " + ticketToView.getCinemaName()));
        detailsPanel.add(new JLabel("Date: " + ticketToView.getFormattedScreeningDate()));
        detailsPanel.add(new JLabel("Time: " + ticketToView.getFormattedScreeningTime()));
        detailsPanel.add(new JLabel("Seat: " + ticketToView.getSeatNumber() + " (" + ticketToView.getSeatType() + ")"));
        detailsPanel.add(new JLabel("Customer: " + ticketToView.getCustomerName()));
        
        // Add components to ticket panel
        ticketPanel.add(titleLabel);
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        ticketPanel.add(codePanel);
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        ticketPanel.add(detailsPanel);
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ticketDialog.dispose();
            }
        });
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        ticketPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        ticketPanel.add(closeButton);
        
        ticketDialog.add(ticketPanel);
        ticketDialog.setVisible(true);
    }
    
    /**
     * Sends tickets via email to the customer.
     */
    private void emailTickets() {
        if (currentReservation == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "No reservation information available.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get email address
        String email = currentReservation.getCustomerEmail();
        
        // For registered users, the email might be stored in the user object
        if (email == null || email.isEmpty()) {
            if (userController.isUserLoggedIn()) {
                email = userController.getCurrentUser().getEmail();
            }
        }
        
        // If still no email, ask for it
        if (email == null || email.isEmpty()) {
            email = JOptionPane.showInputDialog(mainFrame,
                "Please enter the email address to send tickets to:",
                "Email Address",
                JOptionPane.QUESTION_MESSAGE);
            
            if (email == null || email.isEmpty()) {
                return; // User cancelled
            }
            
            // Validate email
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Invalid email address format.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        final String finalEmail = email;
        
        // Generate PDF in a background thread
        JDialog progressDialog = new JDialog(mainFrame, "Sending Email", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(new JLabel("Generating and sending your tickets, please wait...", JLabel.CENTER), 
                           BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(mainFrame);
        
        new Thread(() -> {
            try {
                // Generate PDF file
                File pdfFile = pdfGenerator.generateTicketsPDF(currentReservation, tickets);
                
                // Send email with PDF attachment
                boolean success = emailService.sendTicketsEmail(
                    finalEmail, 
                    currentReservation, 
                    pdfFile
                );
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Tickets have been sent to " + finalEmail + ".",
                            "Email Sent",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Failed to send tickets via email. Please try again later.",
                            "Email Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                // Delete temporary PDF file
                if (pdfFile != null && pdfFile.exists()) {
                    pdfFile.delete();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(mainFrame,
                        "An error occurred while sending the tickets: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
        
        progressDialog.setVisible(true);
    }
    
    /**
     * Prints the tickets.
     */
    private void printTickets() {
        if (currentReservation == null || tickets == null || tickets.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                "No tickets available to print.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Generate PDF in a background thread
        JDialog progressDialog = new JDialog(mainFrame, "Preparing Print", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(new JLabel("Generating tickets for printing, please wait...", JLabel.CENTER), 
                           BorderLayout.CENTER);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(mainFrame);
        
        new Thread(() -> {
            try {
                // Generate PDF file
                File pdfFile = pdfGenerator.generateTicketsPDF(currentReservation, tickets);
                
                // Open PDF for printing
                boolean success = pdfGenerator.openPDFForPrinting(pdfFile);
                
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    
                    if (!success) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Could not open the PDF for printing. The file has been saved to: " + 
                            pdfFile.getAbsolutePath(),
                            "Print Error",
                            JOptionPane.WARNING_MESSAGE);
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(mainFrame,
                        "An error occurred while generating the tickets: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
        
        progressDialog.setVisible(true);
    }
}
