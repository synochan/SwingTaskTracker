package com.cinebook.view;

import com.cinebook.controller.PaymentController;
import com.cinebook.controller.ReservationController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Concession;
import com.cinebook.model.PaymentMethod;
import com.cinebook.model.Reservation;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel for processing payment for a reservation.
 */
public class PaymentPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private ReservationController reservationController;
    private PaymentController paymentController;
    
    // UI Components
    private JPanel orderSummaryPanel;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JComboBox<PaymentMethod> paymentMethodComboBox;
    private JPanel paymentDetailsPanel;
    private JPanel creditCardPanel;
    private JPanel gcashPanel;
    private JPanel paymayaPanel;
    private JButton payNowButton;
    private JButton backButton;
    
    private Reservation currentReservation;
    
    /**
     * Constructor for PaymentPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public PaymentPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.reservationController = new ReservationController();
        this.paymentController = new PaymentController();
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create components
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
    }
    
    /**
     * Creates the top panel with title.
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Title
        JLabel pageTitle = new JLabel("Payment", JLabel.CENTER);
        pageTitle.setFont(new Font("Serif", Font.BOLD, 24));
        
        topPanel.add(pageTitle, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the center panel with order summary and payment details.
     */
    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        // Order Summary Panel
        orderSummaryPanel = new JPanel(new BorderLayout());
        orderSummaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Order Summary"),
            new EmptyBorder(5, 5, 5, 5)));
        
        // Create table for items
        String[] columns = {"Item", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 300));
        
        // Total panel
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        subtotalLabel = new JLabel("Subtotal: ₱0.00");
        taxLabel = new JLabel("Tax (12%): ₱0.00");
        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        totalPanel.add(subtotalLabel);
        totalPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        totalPanel.add(taxLabel);
        totalPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        totalPanel.add(totalLabel);
        
        orderSummaryPanel.add(tableScrollPane, BorderLayout.CENTER);
        orderSummaryPanel.add(totalPanel, BorderLayout.SOUTH);
        
        // Payment Details Panel
        JPanel paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Payment Details"),
            new EmptyBorder(5, 5, 5, 5)));
        
        // Payment method selection
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.add(new JLabel("Payment Method:"));
        
        paymentMethodComboBox = new JComboBox<>(paymentController.getAvailablePaymentMethods());
        methodPanel.add(paymentMethodComboBox);
        
        // Payment details card layout
        paymentDetailsPanel = new JPanel(new CardLayout());
        
        // Credit Card Panel
        creditCardPanel = createCreditCardPanel();
        
        // GCash Panel
        gcashPanel = createGCashPanel();
        
        // PayMaya Panel
        paymayaPanel = createPayMayaPanel();
        
        // Add panels to card layout
        paymentDetailsPanel.add(creditCardPanel, PaymentMethod.CREDIT_CARD.name());
        paymentDetailsPanel.add(gcashPanel, PaymentMethod.GCASH.name());
        paymentDetailsPanel.add(paymayaPanel, PaymentMethod.PAYMAYA.name());
        
        // Add listener to switch panels
        paymentMethodComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) paymentDetailsPanel.getLayout();
                cl.show(paymentDetailsPanel, ((PaymentMethod) paymentMethodComboBox.getSelectedItem()).name());
            }
        });
        
        paymentPanel.add(methodPanel, BorderLayout.NORTH);
        paymentPanel.add(paymentDetailsPanel, BorderLayout.CENTER);
        
        // Add panels to center panel
        centerPanel.add(orderSummaryPanel);
        centerPanel.add(paymentPanel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the credit card payment panel.
     *
     * @return The created JPanel
     */
    private JPanel createCreditCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Card Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Card Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField cardNumberField = new JTextField(16);
        panel.add(cardNumberField, gbc);
        
        // Card Holder Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Cardholder Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField();
        panel.add(nameField, gbc);
        
        // Expiration Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Expiration Date:"), gbc);
        
        JPanel expiryPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JComboBox<String> monthComboBox = new JComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(String.format("%02d", i));
        }
        
        JComboBox<String> yearComboBox = new JComboBox<>();
        int currentYear = java.time.Year.now().getValue();
        for (int i = 0; i < 10; i++) {
            yearComboBox.addItem(String.valueOf(currentYear + i));
        }
        
        expiryPanel.add(monthComboBox);
        expiryPanel.add(yearComboBox);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        panel.add(expiryPanel, gbc);
        
        // CVV
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("CVV:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        JPasswordField cvvField = new JPasswordField(3);
        panel.add(cvvField, gbc);
        
        // Add filler to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the GCash payment panel.
     *
     * @return The created JPanel
     */
    private JPanel createGCashPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Mobile Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("GCash Mobile Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField mobileField = new JTextField();
        panel.add(mobileField, gbc);
        
        // Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Account Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField();
        panel.add(nameField, gbc);
        
        // Instructions
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JTextArea instructionsArea = new JTextArea(
            "Note: This is a simulated payment. In a real system, you would be redirected to the GCash payment page."
        );
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(panel.getBackground());
        panel.add(instructionsArea, gbc);
        
        // Add filler to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the PayMaya payment panel.
     *
     * @return The created JPanel
     */
    private JPanel createPayMayaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Mobile Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("PayMaya Mobile Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField mobileField = new JTextField();
        panel.add(mobileField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JTextField emailField = new JTextField();
        panel.add(emailField, gbc);
        
        // Instructions
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JTextArea instructionsArea = new JTextArea(
            "Note: This is a simulated payment. In a real system, you would be redirected to the PayMaya payment page."
        );
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setEditable(false);
        instructionsArea.setBackground(panel.getBackground());
        panel.add(instructionsArea, gbc);
        
        // Add filler to push everything to the top
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * Creates the bottom panel with navigation buttons.
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        backButton = new JButton("Back");
        payNowButton = new JButton("Pay Now");
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.navigateTo(MainFrame.CONCESSION_PANEL);
            }
        });
        
        payNowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processPayment();
            }
        });
        
        bottomPanel.add(backButton);
        bottomPanel.add(payNowButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Initializes the panel with the current reservation details.
     */
    public void initialize() {
        // Get current reservation from controller
        currentReservation = reservationController.getCurrentReservation();
        
        if (currentReservation == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "No active reservation found. Please start a new booking.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
            return;
        }
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Add seats to table
        List<Seat> seats = currentReservation.getSelectedSeats();
        for (Seat seat : seats) {
            double price = seat.getPrice(currentReservation);
            Object[] row = {
                "Seat " + seat.getSeatNumber() + " (" + seat.getSeatType() + ")",
                "1",
                String.format("₱%.2f", price)
            };
            tableModel.addRow(row);
        }
        
        // Add concessions to table
        List<Concession> concessions = currentReservation.getSelectedConcessions();
        for (Concession concession : concessions) {
            if (concession.getQuantity() > 0) {
                Object[] row = {
                    concession.getName(),
                    concession.getQuantity(),
                    String.format("₱%.2f", concession.getTotalPrice())
                };
                tableModel.addRow(row);
            }
        }
        
        // Calculate totals
        double subtotal = currentReservation.getTotalAmount();
        double tax = subtotal * 0.12; // 12% tax
        double total = subtotal + tax;
        
        // Update total labels
        subtotalLabel.setText(String.format("Subtotal: ₱%.2f", subtotal));
        taxLabel.setText(String.format("Tax (12%%): ₱%.2f", tax));
        totalLabel.setText(String.format("Total: ₱%.2f", total));
        
        // Select default payment method
        paymentMethodComboBox.setSelectedItem(PaymentMethod.CREDIT_CARD);
        CardLayout cl = (CardLayout) paymentDetailsPanel.getLayout();
        cl.show(paymentDetailsPanel, PaymentMethod.CREDIT_CARD.name());
    }
    
    /**
     * Processes the payment for the current reservation.
     */
    private void processPayment() {
        if (currentReservation == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "No active reservation found. Please start a new booking.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        int response = JOptionPane.showConfirmDialog(
            mainFrame,
            "Confirm payment of " + totalLabel.getText() + "?",
            "Payment Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            // Get selected payment method
            PaymentMethod paymentMethod = (PaymentMethod) paymentMethodComboBox.getSelectedItem();
            
            // Process payment
            JDialog progressDialog = new JDialog(mainFrame, "Processing Payment", true);
            progressDialog.setLayout(new BorderLayout());
            progressDialog.add(new JLabel("Processing your payment, please wait...", JLabel.CENTER), BorderLayout.CENTER);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(mainFrame);
            
            // Process payment in a separate thread to allow progress dialog to show
            new Thread(() -> {
                try {
                    // Simulate payment processing delay
                    Thread.sleep(1500);
                    
                    // Finalize the reservation
                    int reservationId = reservationController.completeReservation();
                    
                    if (reservationId > 0) {
                        // Process payment
                        int paymentId = paymentController.processPayment(reservationId, paymentMethod);
                        
                        if (paymentId > 0) {
                            // Generate tickets
                            int ticketsGenerated = reservationController.generateTickets(reservationId);
                            
                            SwingUtilities.invokeLater(() -> {
                                progressDialog.dispose();
                                
                                if (ticketsGenerated > 0) {
                                    // Show success message
                                    JOptionPane.showMessageDialog(mainFrame,
                                        "Payment successful! " + ticketsGenerated + " ticket(s) have been generated.",
                                        "Payment Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                    
                                    // Navigate to confirmation screen
                                    mainFrame.getConfirmationPanel().initialize(reservationId);
                                    mainFrame.navigateTo(MainFrame.CONFIRMATION_PANEL);
                                } else {
                                    JOptionPane.showMessageDialog(mainFrame,
                                        "Payment successful but failed to generate tickets. Please contact support.",
                                        "Partial Success",
                                        JOptionPane.WARNING_MESSAGE);
                                    
                                    mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
                                }
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                progressDialog.dispose();
                                JOptionPane.showMessageDialog(mainFrame,
                                    "Payment processing failed. Please try again.",
                                    "Payment Error",
                                    JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to complete the reservation. Please try again.",
                                "Reservation Error",
                                JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(mainFrame,
                            "Payment processing was interrupted. Please try again.",
                            "Payment Error",
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
            
            progressDialog.setVisible(true);
        }
    }
}
