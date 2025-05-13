package com.cinebook.view;

import com.cinebook.controller.ReservationController;
import com.cinebook.controller.ScreeningController;
import com.cinebook.controller.UserController;
import com.cinebook.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The main JFrame for the application.
 * Acts as the container for all panels and manages navigation between them.
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Controllers
    private UserController userController;
    private ScreeningController screeningController;
    private ReservationController reservationController;
    
    // Panels
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private MovieListingPanel movieListingPanel;
    private SeatSelectionPanel seatSelectionPanel;
    private ConcessionPanel concessionPanel;
    private PaymentPanel paymentPanel;
    private ConfirmationPanel confirmationPanel;
    private AdminLoginPanel adminLoginPanel;
    private AdminDashboardPanel adminDashboardPanel;
    
    // Panel names for CardLayout
    public static final String LOGIN_PANEL = "LOGIN_PANEL";
    public static final String REGISTER_PANEL = "REGISTER_PANEL";
    public static final String MOVIE_LISTING_PANEL = "MOVIE_LISTING_PANEL";
    public static final String SEAT_SELECTION_PANEL = "SEAT_SELECTION_PANEL";
    public static final String CONCESSION_PANEL = "CONCESSION_PANEL";
    public static final String PAYMENT_PANEL = "PAYMENT_PANEL";
    public static final String CONFIRMATION_PANEL = "CONFIRMATION_PANEL";
    public static final String ADMIN_LOGIN_PANEL = "ADMIN_LOGIN_PANEL";
    public static final String ADMIN_DASHBOARD_PANEL = "ADMIN_DASHBOARD_PANEL";
    
    /**
     * Constructor for MainFrame.
     * Initializes the frame and sets up the UI components.
     */
    public MainFrame() {
        // Initialize controllers
        userController = new UserController();
        screeningController = new ScreeningController();
        reservationController = new ReservationController();
        
        // Setup frame properties
        setTitle("CineBook CDO - Online Movie Ticket Booking");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        
        // Add a confirmation dialog when closing the application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (response == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
        
        // Setup main panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize panels
        initializePanels();
        
        // Add panels to main panel
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registerPanel, REGISTER_PANEL);
        mainPanel.add(movieListingPanel, MOVIE_LISTING_PANEL);
        mainPanel.add(seatSelectionPanel, SEAT_SELECTION_PANEL);
        mainPanel.add(concessionPanel, CONCESSION_PANEL);
        mainPanel.add(paymentPanel, PAYMENT_PANEL);
        mainPanel.add(confirmationPanel, CONFIRMATION_PANEL);
        mainPanel.add(adminLoginPanel, ADMIN_LOGIN_PANEL);
        mainPanel.add(adminDashboardPanel, ADMIN_DASHBOARD_PANEL);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Show login panel initially
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }
    
    /**
     * Initializes all panels for the application.
     */
    private void initializePanels() {
        loginPanel = new LoginPanel(this, userController);
        registerPanel = new RegisterPanel(this, userController);
        movieListingPanel = new MovieListingPanel(this, userController);
        seatSelectionPanel = new SeatSelectionPanel(this, userController, screeningController, reservationController);
        concessionPanel = new ConcessionPanel(this, userController);
        paymentPanel = new PaymentPanel(this, userController);
        confirmationPanel = new ConfirmationPanel(this, userController);
        adminLoginPanel = new AdminLoginPanel(this, userController);
        adminDashboardPanel = new AdminDashboardPanel(this, userController);
    }
    
    /**
     * Navigates to a specific panel.
     *
     * @param panelName The name of the panel to navigate to
     */
    public void navigateTo(String panelName) {
        cardLayout.show(mainPanel, panelName);
        
        // Update UI based on the destination panel
        if (panelName.equals(MOVIE_LISTING_PANEL)) {
            movieListingPanel.refreshMovies();
        } else if (panelName.equals(ADMIN_DASHBOARD_PANEL)) {
            adminDashboardPanel.refreshData();
        }
    }
    
    /**
     * Updates the user interface after login/logout.
     *
     * @param user The currently logged-in user, or null if logged out
     */
    public void updateUserUI(User user) {
        if (user != null) {
            // User logged in
            if (user.isAdmin()) {
                // Admin user
                navigateTo(ADMIN_DASHBOARD_PANEL);
            } else {
                // Regular user
                navigateTo(MOVIE_LISTING_PANEL);
            }
        } else {
            // User logged out
            navigateTo(LOGIN_PANEL);
        }
    }
    
    /**
     * Gets the seat selection panel.
     *
     * @return The SeatSelectionPanel instance
     */
    public SeatSelectionPanel getSeatSelectionPanel() {
        return seatSelectionPanel;
    }
    
    /**
     * Gets the concession panel.
     *
     * @return The ConcessionPanel instance
     */
    public ConcessionPanel getConcessionPanel() {
        return concessionPanel;
    }
    
    /**
     * Gets the payment panel.
     *
     * @return The PaymentPanel instance
     */
    public PaymentPanel getPaymentPanel() {
        return paymentPanel;
    }
    
    /**
     * Gets the confirmation panel.
     *
     * @return The ConfirmationPanel instance
     */
    public ConfirmationPanel getConfirmationPanel() {
        return confirmationPanel;
    }
    
    /**
     * Gets the admin dashboard panel.
     *
     * @return The AdminDashboardPanel instance
     */
    public AdminDashboardPanel getAdminDashboardPanel() {
        return adminDashboardPanel;
    }
}
