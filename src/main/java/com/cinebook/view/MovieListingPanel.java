package com.cinebook.view;

import com.cinebook.controller.MovieController;
import com.cinebook.controller.ReservationController;
import com.cinebook.controller.ScreeningController;
import com.cinebook.controller.UserController;
import com.cinebook.model.Movie;
import com.cinebook.model.Screening;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying movie listings and allowing users to select a movie and screening.
 */
public class MovieListingPanel extends JPanel {
    private MainFrame mainFrame;
    private UserController userController;
    private MovieController movieController;
    private ScreeningController screeningController;
    private ReservationController reservationController;
    
    // UI Components
    private JPanel movieListPanel;
    private JScrollPane movieScrollPane;
    private JPanel movieDetailsPanel;
    private JLabel titleLabel;
    private JTextArea synopsisArea;
    private JLabel directorLabel;
    private JLabel castLabel;
    private JLabel genreLabel;
    private JLabel durationLabel;
    private JLabel ratingLabel;
    private JComboBox<String> dateComboBox;
    private JComboBox<String> cinemaComboBox;
    private JList<Screening> screeningList;
    private DefaultListModel<Screening> screeningListModel;
    private JButton selectScreeningButton;
    private JButton loginButton;
    private JButton logoutButton;
    
    private Movie selectedMovie;
    private Screening selectedScreening;
    
    /**
     * Constructor for MovieListingPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param userController The UserController instance
     */
    public MovieListingPanel(MainFrame mainFrame, UserController userController) {
        this.mainFrame = mainFrame;
        this.userController = userController;
        this.movieController = new MovieController();
        this.screeningController = new ScreeningController();
        this.reservationController = new ReservationController();
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top panel with title and user controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Create main content panel (movie list + details)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create movie list panel
        createMovieListPanel();
        contentPanel.add(movieScrollPane, BorderLayout.WEST);
        
        // Create movie details panel
        createMovieDetailsPanel();
        contentPanel.add(movieDetailsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Create bottom panel with booking controls
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        refreshMovies();
        updateLoginButton();
    }
    
    /**
     * Creates the top panel with title and user controls.
     *
     * @return The created JPanel
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Title
        JLabel pageTitle = new JLabel("CineBook CDO - Now Showing", JLabel.CENTER);
        pageTitle.setFont(new Font("Serif", Font.BOLD, 24));
        topPanel.add(pageTitle, BorderLayout.CENTER);
        
        // User controls
        JPanel userControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loginButton = new JButton("Login");
        logoutButton = new JButton("Logout");
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.navigateTo(MainFrame.LOGIN_PANEL);
            }
        });
        
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userController.logout();
                JOptionPane.showMessageDialog(mainFrame,
                    "You have been logged out successfully.",
                    "Logout Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                updateLoginButton();
            }
        });
        
        userControlPanel.add(loginButton);
        userControlPanel.add(logoutButton);
        topPanel.add(userControlPanel, BorderLayout.EAST);
        
        return topPanel;
    }
    
    /**
     * Creates the movie list panel.
     */
    private void createMovieListPanel() {
        movieListPanel = new JPanel();
        movieListPanel.setLayout(new BoxLayout(movieListPanel, BoxLayout.Y_AXIS));
        movieScrollPane = new JScrollPane(movieListPanel);
        movieScrollPane.setPreferredSize(new Dimension(300, 500));
        movieScrollPane.setBorder(BorderFactory.createTitledBorder("Now Showing"));
    }
    
    /**
     * Creates the movie details panel.
     */
    private void createMovieDetailsPanel() {
        movieDetailsPanel = new JPanel(new BorderLayout());
        movieDetailsPanel.setBorder(BorderFactory.createTitledBorder("Movie Details"));
        
        // Movie info panel
        JPanel movieInfoPanel = new JPanel();
        movieInfoPanel.setLayout(new BoxLayout(movieInfoPanel, BoxLayout.Y_AXIS));
        movieInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        titleLabel = new JLabel("Select a movie");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        synopsisArea = new JTextArea(5, 20);
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        synopsisArea.setEditable(false);
        synopsisArea.setBackground(movieInfoPanel.getBackground());
        JScrollPane synopsisScrollPane = new JScrollPane(synopsisArea);
        synopsisScrollPane.setBorder(BorderFactory.createTitledBorder("Synopsis"));
        synopsisScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        directorLabel = new JLabel("Director: ");
        directorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        castLabel = new JLabel("Cast: ");
        castLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        genreLabel = new JLabel("Genre: ");
        genreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        durationLabel = new JLabel("Duration: ");
        durationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ratingLabel = new JLabel("Rating: ");
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        movieInfoPanel.add(titleLabel);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        movieInfoPanel.add(synopsisScrollPane);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        movieInfoPanel.add(directorLabel);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        movieInfoPanel.add(castLabel);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        movieInfoPanel.add(genreLabel);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        movieInfoPanel.add(durationLabel);
        movieInfoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        movieInfoPanel.add(ratingLabel);
        
        // Screening selection panel
        JPanel screeningPanel = new JPanel(new BorderLayout());
        screeningPanel.setBorder(BorderFactory.createTitledBorder("Screenings"));
        
        // Filter panel for dates and cinemas
        JPanel filterPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        dateComboBox = new JComboBox<>();
        populateDateComboBox();
        cinemaComboBox = new JComboBox<>();
        populateCinemaComboBox();
        
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.add(new JLabel("Date:"), BorderLayout.NORTH);
        datePanel.add(dateComboBox, BorderLayout.CENTER);
        
        JPanel cinemaPanel = new JPanel(new BorderLayout());
        cinemaPanel.add(new JLabel("Cinema:"), BorderLayout.NORTH);
        cinemaPanel.add(cinemaComboBox, BorderLayout.CENTER);
        
        filterPanel.add(datePanel);
        filterPanel.add(cinemaPanel);
        
        // Create screening list
        screeningListModel = new DefaultListModel<>();
        screeningList = new JList<>(screeningListModel);
        screeningList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane screeningScrollPane = new JScrollPane(screeningList);
        screeningScrollPane.setPreferredSize(new Dimension(200, 150));
        
        screeningPanel.add(filterPanel, BorderLayout.NORTH);
        screeningPanel.add(screeningScrollPane, BorderLayout.CENTER);
        
        // Add listeners
        dateComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScreeningList();
            }
        });
        
        cinemaComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateScreeningList();
            }
        });
        
        screeningList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    selectedScreening = screeningList.getSelectedValue();
                    selectScreeningButton.setEnabled(selectedScreening != null);
                }
            }
        });
        
        // Add components to movie details panel
        movieDetailsPanel.add(movieInfoPanel, BorderLayout.CENTER);
        movieDetailsPanel.add(screeningPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the bottom panel with booking controls.
     *
     * @return The created JPanel
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        selectScreeningButton = new JButton("Book Tickets");
        selectScreeningButton.setEnabled(false);
        
        selectScreeningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedScreening != null) {
                    if (userController.isUserLoggedIn()) {
                        // Start reservation for logged in user
                        boolean success = reservationController.startReservationForUser(
                                userController.getCurrentUser(), selectedScreening.getId());
                        
                        if (success) {
                            mainFrame.getSeatSelectionPanel().initialize(selectedScreening);
                            mainFrame.navigateTo(MainFrame.SEAT_SELECTION_PANEL);
                        } else {
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to start the reservation process. Please try again.",
                                "Reservation Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Show guest info dialog
                        showGuestInfoDialog();
                    }
                }
            }
        });
        
        bottomPanel.add(selectScreeningButton);
        return bottomPanel;
    }
    
    /**
     * Shows a dialog to collect guest information.
     */
    private void showGuestInfoDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, 
                "Guest Information", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            
            // Validate input
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Please fill out all fields.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check email format
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Please enter a valid email address.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Start reservation for guest
            boolean success = reservationController.startReservationForGuest(
                    name, email, phone, selectedScreening.getId());
            
            if (success) {
                mainFrame.getSeatSelectionPanel().initialize(selectedScreening);
                mainFrame.navigateTo(MainFrame.SEAT_SELECTION_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                    "Failed to start the reservation process. Please try again.",
                    "Reservation Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Populates the date combo box with the next 7 days.
     */
    private void populateDateComboBox() {
        dateComboBox.removeAllItems();
        dateComboBox.addItem("All Dates");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            dateComboBox.addItem(date.format(formatter));
        }
    }
    
    /**
     * Populates the cinema combo box with all active cinemas.
     */
    private void populateCinemaComboBox() {
        cinemaComboBox.removeAllItems();
        cinemaComboBox.addItem("All Cinemas");
        
        try {
            screeningController.getAllActiveCinemas().forEach(cinema -> 
                cinemaComboBox.addItem(cinema.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the screening list based on the selected movie, date, and cinema.
     */
    private void updateScreeningList() {
        screeningListModel.clear();
        
        if (selectedMovie == null) {
            return;
        }
        
        String selectedDate = (String) dateComboBox.getSelectedItem();
        String selectedCinema = (String) cinemaComboBox.getSelectedItem();
        
        List<Screening> allScreenings = screeningController.getScreeningsByMovie(selectedMovie.getId());
        List<Screening> filteredScreenings = new ArrayList<>();
        
        // Filter screenings
        for (Screening screening : allScreenings) {
            boolean dateMatch = selectedDate.equals("All Dates") || 
                               screening.getFormattedDate().equals(selectedDate);
            boolean cinemaMatch = selectedCinema.equals("All Cinemas") || 
                                 screening.getCinemaName().equals(selectedCinema);
            
            if (dateMatch && cinemaMatch) {
                filteredScreenings.add(screening);
            }
        }
        
        // Add filtered screenings to the list model
        for (Screening screening : filteredScreenings) {
            screeningListModel.addElement(screening);
        }
    }
    
    /**
     * Updates the display with the selected movie's details.
     *
     * @param movie The selected movie
     */
    private void displayMovieDetails(Movie movie) {
        if (movie != null) {
            titleLabel.setText(movie.getTitle());
            synopsisArea.setText(movie.getSynopsis());
            directorLabel.setText("Director: " + movie.getDirector());
            castLabel.setText("Cast: " + movie.getCast());
            genreLabel.setText("Genre: " + movie.getGenre());
            durationLabel.setText("Duration: " + movie.getFormattedDuration());
            ratingLabel.setText("Rating: " + movie.getRating());
            
            // Update screenings
            updateScreeningList();
        } else {
            // Clear the fields
            titleLabel.setText("Select a movie");
            synopsisArea.setText("");
            directorLabel.setText("Director: ");
            castLabel.setText("Cast: ");
            genreLabel.setText("Genre: ");
            durationLabel.setText("Duration: ");
            ratingLabel.setText("Rating: ");
            
            // Clear screenings
            screeningListModel.clear();
        }
    }
    
    /**
     * Updates the login/logout button visibility based on login status.
     */
    private void updateLoginButton() {
        boolean loggedIn = userController.isUserLoggedIn();
        loginButton.setVisible(!loggedIn);
        logoutButton.setVisible(loggedIn);
    }
    
    /**
     * Refreshes the movie list with the latest data from the database.
     */
    public void refreshMovies() {
        movieListPanel.removeAll();
        
        List<Movie> movies = movieController.getAllActiveMovies();
        
        for (Movie movie : movies) {
            JButton movieButton = new JButton(movie.getTitle());
            movieButton.setPreferredSize(new Dimension(280, 50));
            movieButton.setMaximumSize(new Dimension(280, 50));
            movieButton.setHorizontalAlignment(SwingConstants.LEFT);
            
            // Add action listener
            movieButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedMovie = movie;
                    displayMovieDetails(movie);
                }
            });
            
            movieListPanel.add(movieButton);
            movieListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // Update UI
        movieListPanel.revalidate();
        movieListPanel.repaint();
        updateLoginButton();
    }
}
