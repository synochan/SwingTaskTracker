package com.cinebook.view;

import com.cinebook.controller.ReservationController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Concession;
import com.cinebook.model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for selecting concession items during the booking process.
 */
public class ConcessionPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private ReservationController reservationController;
    
    // UI Components
    private JLabel pageTitle;
    private JPanel categoriesPanel;
    private JScrollPane concessionScrollPane;
    private JPanel concessionItemsPanel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;
    private JButton continueButton;
    private JButton skipButton;
    private JButton backButton;
    
    private List<Concession> selectedConcessions;
    private double totalPrice;
    
    /**
     * Constructor for ConcessionPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public ConcessionPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.reservationController = new ReservationController();
        this.selectedConcessions = new ArrayList<>();
        this.totalPrice = 0.0;
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create components
        createTopPanel();
        createCenterPanel();
        createBottomPanel();
    }
    
    /**
     * Creates the top panel with title and reservation info.
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Title
        pageTitle = new JLabel("Add Concession Items", JLabel.CENTER);
        pageTitle.setFont(new Font("Serif", Font.BOLD, 24));
        
        topPanel.add(pageTitle, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the center panel with concession categories, items, and cart.
     */
    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Left side - Categories & Items
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Concession Items"),
            new EmptyBorder(5, 5, 5, 5)));
        
        // Categories panel
        categoriesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(categoriesPanel, BorderLayout.NORTH);
        
        // Concession items panel
        concessionItemsPanel = new JPanel();
        concessionItemsPanel.setLayout(new BoxLayout(concessionItemsPanel, BoxLayout.Y_AXIS));
        concessionScrollPane = new JScrollPane(concessionItemsPanel);
        concessionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        concessionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftPanel.add(concessionScrollPane, BorderLayout.CENTER);
        
        // Right side - Cart
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Your Order"),
            new EmptyBorder(5, 5, 5, 5)));
        
        // Cart table
        String[] columnNames = {"Item", "Quantity", "Price", "Total"};
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Item name
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50);  // Quantity
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // Price
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Total
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(400, 300));
        rightPanel.add(cartScrollPane, BorderLayout.CENTER);
        
        // Total price panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: ₱0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalPanel.add(totalLabel);
        rightPanel.add(totalPanel, BorderLayout.SOUTH);
        
        // Add panels to center panel
        centerPanel.add(leftPanel, BorderLayout.CENTER);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the bottom panel with navigation buttons.
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        backButton = new JButton("Back to Seats");
        skipButton = new JButton("Skip");
        continueButton = new JButton("Continue to Payment");
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.navigateTo(MainFrame.SEAT_SELECTION_PANEL);
            }
        });
        
        skipButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Skip concessions - continue with empty concession list
                boolean success = reservationController.addConcessionsToReservation(new ArrayList<>());
                
                if (success) {
                    mainFrame.getPaymentPanel().initialize();
                    mainFrame.navigateTo(MainFrame.PAYMENT_PANEL);
                } else {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Failed to proceed to payment. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Continue with selected concessions
                boolean success = reservationController.addConcessionsToReservation(selectedConcessions);
                
                if (success) {
                    mainFrame.getPaymentPanel().initialize();
                    mainFrame.navigateTo(MainFrame.PAYMENT_PANEL);
                } else {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Failed to add concessions to the reservation. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        bottomPanel.add(backButton);
        bottomPanel.add(skipButton);
        bottomPanel.add(continueButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Initializes the panel with concession categories and items.
     */
    public void initialize() {
        // Get current reservation from controller
        Reservation reservation = reservationController.getCurrentReservation();
        
        if (reservation == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "No active reservation found. Please start a new booking.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            mainFrame.navigateTo(MainFrame.MOVIE_LISTING_PANEL);
            return;
        }
        
        // Clear previous concessions
        selectedConcessions.clear();
        totalPrice = 0.0;
        
        // Get available concession categories
        List<String> categories = reservationController.getAvailableConcessionCategories();
        
        // Clear and update UI components
        categoriesPanel.removeAll();
        concessionItemsPanel.removeAll();
        cartTableModel.setRowCount(0);
        
        // Add category buttons
        for (String category : categories) {
            JButton categoryButton = new JButton(category);
            categoryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayConcessionsByCategory(category);
                }
            });
            categoriesPanel.add(categoryButton);
        }
        
        // Display first category by default if available
        if (!categories.isEmpty()) {
            displayConcessionsByCategory(categories.get(0));
        }
        
        // Update totals
        updateCart();
        
        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Displays concession items for a specific category.
     *
     * @param category The category to display items for
     */
    private void displayConcessionsByCategory(String category) {
        // Clear items panel
        concessionItemsPanel.removeAll();
        
        // Get concessions for this category
        List<Concession> concessions = reservationController.getConcessionsByCategory(category);
        
        if (concessions.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No items available in this category.");
            noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            concessionItemsPanel.add(noItemsLabel);
        } else {
            // Create a panel for each concession item
            for (Concession concession : concessions) {
                // Create a copy of the concession to preserve quantities
                Concession displayConcession = findConcessionInList(concession.getId());
                if (displayConcession == null) {
                    displayConcession = concession.copy();
                }
                
                concessionItemsPanel.add(createConcessionItemPanel(displayConcession));
                concessionItemsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        // Update UI
        concessionItemsPanel.revalidate();
        concessionItemsPanel.repaint();
    }
    
    /**
     * Creates a panel for a concession item with controls.
     *
     * @param concession The concession item
     * @return The created JPanel
     */
    private JPanel createConcessionItemPanel(Concession concession) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 5, 5, 5)));
        panel.setMaximumSize(new Dimension(500, 80));
        panel.setPreferredSize(new Dimension(500, 80));
        
        // Item info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        
        JLabel nameLabel = new JLabel(concession.getName() + " - " + concession.getFormattedPrice());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel descLabel = new JLabel(concession.getDescription());
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        infoPanel.add(nameLabel);
        infoPanel.add(descLabel);
        
        // Quantity control
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JButton decreaseButton = new JButton("-");
        JLabel quantityLabel = new JLabel(String.valueOf(concession.getQuantity()), JLabel.CENTER);
        quantityLabel.setPreferredSize(new Dimension(30, 20));
        JButton increaseButton = new JButton("+");
        
        final Concession finalConcession = concession;
        
        decreaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (finalConcession.getQuantity() > 0) {
                    finalConcession.decrementQuantity();
                    quantityLabel.setText(String.valueOf(finalConcession.getQuantity()));
                    
                    // Update selected concessions
                    updateSelectedConcessions(finalConcession);
                    
                    // Update cart
                    updateCart();
                }
            }
        });
        
        increaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finalConcession.incrementQuantity();
                quantityLabel.setText(String.valueOf(finalConcession.getQuantity()));
                
                // Update selected concessions
                updateSelectedConcessions(finalConcession);
                
                // Update cart
                updateCart();
            }
        });
        
        quantityPanel.add(decreaseButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(increaseButton);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(quantityPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Updates the selected concessions list with a modified concession.
     *
     * @param concession The modified concession
     */
    private void updateSelectedConcessions(Concession concession) {
        // Remove existing concession with same ID if present
        Concession existingConcession = findConcessionInList(concession.getId());
        if (existingConcession != null) {
            selectedConcessions.remove(existingConcession);
        }
        
        // Add the concession to the list if quantity > 0
        if (concession.getQuantity() > 0) {
            selectedConcessions.add(concession.copy());
        }
    }
    
    /**
     * Finds a concession in the selected concessions list by ID.
     *
     * @param id The ID to search for
     * @return The concession if found, null otherwise
     */
    private Concession findConcessionInList(int id) {
        for (Concession c : selectedConcessions) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Updates the cart display with the current selected concessions.
     */
    private void updateCart() {
        // Clear cart table
        cartTableModel.setRowCount(0);
        
        // Calculate total price
        totalPrice = 0.0;
        
        // Add selected concessions to cart
        for (Concession concession : selectedConcessions) {
            if (concession.getQuantity() > 0) {
                Object[] rowData = {
                    concession.getName(),
                    concession.getQuantity(),
                    String.format("₱%.2f", concession.getPrice()),
                    String.format("₱%.2f", concession.getTotalPrice())
                };
                cartTableModel.addRow(rowData);
                
                totalPrice += concession.getTotalPrice();
            }
        }
        
        // Update total label
        totalLabel.setText(String.format("Total: ₱%.2f", totalPrice));
    }
}
