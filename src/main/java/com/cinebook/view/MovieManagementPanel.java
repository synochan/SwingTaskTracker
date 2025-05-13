package com.cinebook.view;

import com.cinebook.controller.AdminController;
import com.cinebook.controller.MovieController;
import com.cinebook.controller.ScreeningController;
import com.cinebook.model.Cinema;
import com.cinebook.model.Movie;
import com.cinebook.model.Screening;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing movies in the admin dashboard.
 */
public class MovieManagementPanel extends JPanel {
    private MainFrame mainFrame;
    private AdminController adminController;
    private MovieController movieController;
    private ScreeningController screeningController;
    
    // Movies list components
    private JTable moviesTable;
    private DefaultTableModel moviesTableModel;
    private JButton addMovieButton;
    private JButton editMovieButton;
    private JButton deleteMovieButton;
    
    // Screenings components
    private JTable screeningsTable;
    private DefaultTableModel screeningsTableModel;
    private JButton addScreeningButton;
    private JButton editScreeningButton;
    private JButton deleteScreeningButton;
    
    // Selected items
    private Movie selectedMovie;
    private Screening selectedScreening;
    
    /**
     * Constructor for MovieManagementPanel.
     *
     * @param mainFrame The parent MainFrame
     * @param adminController The AdminController instance
     */
    public MovieManagementPanel(MainFrame mainFrame, AdminController adminController) {
        this.mainFrame = mainFrame;
        this.adminController = adminController;
        this.movieController = new MovieController();
        this.screeningController = new ScreeningController();
        
        // Setup panel properties
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create the components
        createMoviesPanel();
        createScreeningsPanel();
        
        // Refresh data
        refreshData();
    }
    
    /**
     * Creates the movies panel.
     */
    private void createMoviesPanel() {
        JPanel moviesPanel = new JPanel(new BorderLayout());
        moviesPanel.setBorder(BorderFactory.createTitledBorder("Movies"));
        
        // Table model
        String[] movieColumns = {"ID", "Title", "Director", "Genre", "Duration", "Rating", "Release Date", "Status"};
        moviesTableModel = new DefaultTableModel(movieColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Table
        moviesTable = new JTable(moviesTableModel);
        moviesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moviesTable.getTableHeader().setReorderingAllowed(false);
        
        // Hide ID column
        moviesTable.getColumnModel().getColumn(0).setMinWidth(0);
        moviesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        moviesTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Selection listener
        moviesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = moviesTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int movieId = Integer.parseInt(moviesTable.getValueAt(selectedRow, 0).toString());
                        selectedMovie = movieController.getMovieById(movieId);
                        refreshScreeningsTable();
                        updateButtonStates();
                    } else {
                        selectedMovie = null;
                        clearScreeningsTable();
                        updateButtonStates();
                    }
                }
            }
        });
        
        JScrollPane moviesScrollPane = new JScrollPane(moviesTable);
        moviesScrollPane.setPreferredSize(new Dimension(800, 200));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addMovieButton = new JButton("Add Movie");
        editMovieButton = new JButton("Edit Movie");
        deleteMovieButton = new JButton("Delete Movie");
        
        // Add Movie button action
        addMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMovieDialog(null);
            }
        });
        
        // Edit Movie button action
        editMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovie != null) {
                    openMovieDialog(selectedMovie);
                }
            }
        });
        
        // Delete Movie button action
        deleteMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovie != null) {
                    // Check if there are any screenings for this movie
                    List<Screening> screenings = screeningController.getScreeningsByMovie(selectedMovie.getId());
                    
                    if (!screenings.isEmpty()) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Cannot delete movie. There are screenings associated with this movie.",
                            "Delete Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Are you sure you want to delete the movie '" + selectedMovie.getTitle() + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = movieController.deleteMovie(selectedMovie.getId());
                        
                        if (success) {
                            refreshMoviesTable();
                            selectedMovie = null;
                            clearScreeningsTable();
                            updateButtonStates();
                            
                            JOptionPane.showMessageDialog(mainFrame,
                                "Movie deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to delete movie. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        buttonsPanel.add(addMovieButton);
        buttonsPanel.add(editMovieButton);
        buttonsPanel.add(deleteMovieButton);
        
        // Add components to panel
        moviesPanel.add(moviesScrollPane, BorderLayout.CENTER);
        moviesPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add panel to main panel
        add(moviesPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the screenings panel.
     */
    private void createScreeningsPanel() {
        JPanel screeningsPanel = new JPanel(new BorderLayout());
        screeningsPanel.setBorder(BorderFactory.createTitledBorder("Screenings"));
        
        // Table model
        String[] screeningColumns = {"ID", "Cinema", "Date", "Time", "Standard Price", "Deluxe Price", "Status"};
        screeningsTableModel = new DefaultTableModel(screeningColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Table
        screeningsTable = new JTable(screeningsTableModel);
        screeningsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        screeningsTable.getTableHeader().setReorderingAllowed(false);
        
        // Hide ID column
        screeningsTable.getColumnModel().getColumn(0).setMinWidth(0);
        screeningsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        screeningsTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Selection listener
        screeningsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = screeningsTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        int screeningId = Integer.parseInt(screeningsTable.getValueAt(selectedRow, 0).toString());
                        selectedScreening = screeningController.getScreeningById(screeningId);
                        updateButtonStates();
                    } else {
                        selectedScreening = null;
                        updateButtonStates();
                    }
                }
            }
        });
        
        JScrollPane screeningsScrollPane = new JScrollPane(screeningsTable);
        screeningsScrollPane.setPreferredSize(new Dimension(800, 200));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addScreeningButton = new JButton("Add Screening");
        editScreeningButton = new JButton("Edit Screening");
        deleteScreeningButton = new JButton("Delete Screening");
        
        // Add Screening button action
        addScreeningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedMovie != null) {
                    openScreeningDialog(null);
                } else {
                    JOptionPane.showMessageDialog(mainFrame,
                        "Please select a movie first.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        // Edit Screening button action
        editScreeningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedScreening != null) {
                    openScreeningDialog(selectedScreening);
                }
            }
        });
        
        // Delete Screening button action
        deleteScreeningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedScreening != null) {
                    // Check if there are any reservations for this screening
                    boolean hasReservedSeats = screeningController.hasReservedSeats(selectedScreening.getId());
                    
                    if (hasReservedSeats) {
                        JOptionPane.showMessageDialog(mainFrame,
                            "Cannot delete screening. There are reserved seats for this screening.",
                            "Delete Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int confirm = JOptionPane.showConfirmDialog(mainFrame,
                        "Are you sure you want to delete this screening?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = screeningController.deleteScreening(selectedScreening.getId());
                        
                        if (success) {
                            refreshScreeningsTable();
                            selectedScreening = null;
                            updateButtonStates();
                            
                            JOptionPane.showMessageDialog(mainFrame,
                                "Screening deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(mainFrame,
                                "Failed to delete screening. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        buttonsPanel.add(addScreeningButton);
        buttonsPanel.add(editScreeningButton);
        buttonsPanel.add(deleteScreeningButton);
        
        // Add components to panel
        screeningsPanel.add(screeningsScrollPane, BorderLayout.CENTER);
        screeningsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add panel to main panel
        add(screeningsPanel, BorderLayout.CENTER);
    }
    
    /**
     * Refreshes the movies table with data from the database.
     */
    private void refreshMoviesTable() {
        // Clear table
        moviesTableModel.setRowCount(0);
        
        // Get all movies
        List<Movie> movies = movieController.getAllMovies();
        
        // Populate table
        for (Movie movie : movies) {
            Object[] row = {
                movie.getId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getGenre(),
                movie.getDurationMinutes() + " min",
                movie.getRating(),
                movie.getReleaseDate(),
                movie.isActive() ? "Active" : "Inactive"
            };
            moviesTableModel.addRow(row);
        }
    }
    
    /**
     * Refreshes the screenings table with data for the selected movie.
     */
    private void refreshScreeningsTable() {
        // Clear table
        screeningsTableModel.setRowCount(0);
        
        if (selectedMovie != null) {
            // Get screenings for the selected movie
            List<Screening> screenings = screeningController.getScreeningsByMovie(selectedMovie.getId());
            
            // Date formatter
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // Populate table
            for (Screening screening : screenings) {
                String date = screening.getScreeningTime().format(dateFormatter);
                String time = screening.getScreeningTime().format(timeFormatter);
                
                Object[] row = {
                    screening.getId(),
                    screening.getCinemaName(),
                    date,
                    time,
                    String.format("₱%.2f", screening.getStandardSeatPrice()),
                    String.format("₱%.2f", screening.getDeluxeSeatPrice()),
                    screening.isActive() ? "Active" : "Inactive"
                };
                screeningsTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Clears the screenings table.
     */
    private void clearScreeningsTable() {
        screeningsTableModel.setRowCount(0);
    }
    
    /**
     * Updates the enabled state of buttons based on selection.
     */
    private void updateButtonStates() {
        editMovieButton.setEnabled(selectedMovie != null);
        deleteMovieButton.setEnabled(selectedMovie != null);
        
        addScreeningButton.setEnabled(selectedMovie != null);
        editScreeningButton.setEnabled(selectedScreening != null);
        deleteScreeningButton.setEnabled(selectedScreening != null);
    }
    
    /**
     * Opens a dialog to add or edit a movie.
     *
     * @param movie The movie to edit, or null to add a new movie
     */
    private void openMovieDialog(Movie movie) {
        // Create dialog
        JDialog dialog = new JDialog(mainFrame, movie == null ? "Add Movie" : "Edit Movie", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create form fields
        JTextField titleField = new JTextField(20);
        JTextField directorField = new JTextField(20);
        JTextField castField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JTextArea synopsisArea = new JTextArea(5, 20);
        synopsisArea.setLineWrap(true);
        synopsisArea.setWrapStyleWord(true);
        JScrollPane synopsisScrollPane = new JScrollPane(synopsisArea);
        JTextField durationField = new JTextField(20);
        JTextField ratingField = new JTextField(20);
        JTextField releaseDateField = new JTextField(20);
        JTextField posterUrlField = new JTextField(20);
        JCheckBox isActiveCheckBox = new JCheckBox("Active");
        
        // Set existing values if editing
        if (movie != null) {
            titleField.setText(movie.getTitle());
            directorField.setText(movie.getDirector());
            castField.setText(movie.getCast());
            genreField.setText(movie.getGenre());
            synopsisArea.setText(movie.getSynopsis());
            durationField.setText(String.valueOf(movie.getDurationMinutes()));
            ratingField.setText(movie.getRating());
            releaseDateField.setText(movie.getReleaseDate());
            posterUrlField.setText(movie.getPosterUrl());
            isActiveCheckBox.setSelected(movie.isActive());
        } else {
            isActiveCheckBox.setSelected(true);
        }
        
        // Add fields to form panel
        addFormField(formPanel, "Title:", titleField);
        addFormField(formPanel, "Director:", directorField);
        addFormField(formPanel, "Cast:", castField);
        addFormField(formPanel, "Genre:", genreField);
        
        JPanel synopsisPanel = new JPanel(new BorderLayout());
        synopsisPanel.add(new JLabel("Synopsis:"), BorderLayout.NORTH);
        synopsisPanel.add(synopsisScrollPane, BorderLayout.CENTER);
        formPanel.add(synopsisPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        addFormField(formPanel, "Duration (minutes):", durationField);
        addFormField(formPanel, "Rating:", ratingField);
        addFormField(formPanel, "Release Date (YYYY-MM-DD):", releaseDateField);
        addFormField(formPanel, "Poster URL:", posterUrlField);
        
        JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        activePanel.add(isActiveCheckBox);
        formPanel.add(activePanel);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate fields
                if (titleField.getText().trim().isEmpty() ||
                    directorField.getText().trim().isEmpty() ||
                    castField.getText().trim().isEmpty() ||
                    genreField.getText().trim().isEmpty() ||
                    synopsisArea.getText().trim().isEmpty() ||
                    durationField.getText().trim().isEmpty() ||
                    ratingField.getText().trim().isEmpty() ||
                    releaseDateField.getText().trim().isEmpty()) {
                    
                    JOptionPane.showMessageDialog(dialog,
                        "Please fill in all required fields.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Parse duration
                int duration;
                try {
                    duration = Integer.parseInt(durationField.getText().trim());
                    if (duration <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Duration must be a positive integer.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate release date format
                String releaseDate = releaseDateField.getText().trim();
                if (!releaseDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(dialog,
                        "Release date must be in format YYYY-MM-DD.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Create Movie object
                Movie movieData = new Movie(
                    titleField.getText().trim(),
                    directorField.getText().trim(),
                    castField.getText().trim(),
                    genreField.getText().trim(),
                    synopsisArea.getText().trim(),
                    duration,
                    ratingField.getText().trim(),
                    releaseDate,
                    posterUrlField.getText().trim(),
                    isActiveCheckBox.isSelected()
                );
                
                boolean success;
                if (movie != null) {
                    // Update existing movie
                    movieData.setId(movie.getId());
                    success = movieController.updateMovie(movieData);
                } else {
                    // Add new movie
                    success = movieController.addMovie(movieData) > 0;
                }
                
                if (success) {
                    refreshMoviesTable();
                    // Select the added/updated movie
                    if (movie == null) {
                        // Find the row with the new movie title
                        for (int i = 0; i < moviesTable.getRowCount(); i++) {
                            if (moviesTable.getValueAt(i, 1).equals(movieData.getTitle())) {
                                moviesTable.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    } else {
                        // Find the row with the movie ID
                        for (int i = 0; i < moviesTable.getRowCount(); i++) {
                            if (Integer.parseInt(moviesTable.getValueAt(i, 0).toString()) == movie.getId()) {
                                moviesTable.setRowSelectionInterval(i, i);
                                break;
                            }
                        }
                    }
                    
                    dialog.dispose();
                    
                    JOptionPane.showMessageDialog(mainFrame,
                        movie == null ? "Movie added successfully!" : "Movie updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        movie == null ? "Failed to add movie." : "Failed to update movie.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        // Add panels to dialog
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        dialog.add(formScrollPane, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Opens a dialog to add or edit a screening.
     *
     * @param screening The screening to edit, or null to add a new screening
     */
    private void openScreeningDialog(Screening screening) {
        // Create dialog
        JDialog dialog = new JDialog(mainFrame, screening == null ? "Add Screening" : "Edit Screening", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create form fields
        JComboBox<Cinema> cinemaComboBox = new JComboBox<>();
        
        // Get all cinemas
        List<Cinema> cinemas = screeningController.getAllActiveCinemas();
        for (Cinema cinema : cinemas) {
            cinemaComboBox.addItem(cinema);
        }
        
        // Date picker
        JTextField dateField = new JTextField(10);
        dateField.setToolTipText("YYYY-MM-DD");
        
        // Time picker components
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time:"));
        
        JComboBox<Integer> hourComboBox = new JComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourComboBox.addItem(i);
        }
        
        JComboBox<Integer> minuteComboBox = new JComboBox<>();
        for (int i = 0; i < 60; i += 5) {
            minuteComboBox.addItem(i);
        }
        
        timePanel.add(hourComboBox);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteComboBox);
        
        JTextField standardPriceField = new JTextField(10);
        JTextField deluxePriceField = new JTextField(10);
        JCheckBox isActiveCheckBox = new JCheckBox("Active");
        
        // Set existing values if editing
        if (screening != null) {
            // Set selected cinema
            for (int i = 0; i < cinemaComboBox.getItemCount(); i++) {
                if (cinemaComboBox.getItemAt(i).getId() == screening.getCinemaId()) {
                    cinemaComboBox.setSelectedIndex(i);
                    break;
                }
            }
            
            // Set date and time
            dateField.setText(screening.getScreeningTime().toLocalDate().toString());
            hourComboBox.setSelectedItem(screening.getScreeningTime().getHour());
            // Find closest 5-minute interval
            int minute = screening.getScreeningTime().getMinute();
            int closestMinute = (minute / 5) * 5;
            minuteComboBox.setSelectedItem(closestMinute);
            
            standardPriceField.setText(String.format("%.2f", screening.getStandardSeatPrice()));
            deluxePriceField.setText(String.format("%.2f", screening.getDeluxeSeatPrice()));
            isActiveCheckBox.setSelected(screening.isActive());
        } else {
            // Default values for new screening
            dateField.setText(LocalDate.now().plusDays(1).toString());
            hourComboBox.setSelectedItem(18); // 6 PM
            minuteComboBox.setSelectedItem(0); // 00 minutes
            standardPriceField.setText("180.00");
            deluxePriceField.setText("280.00");
            isActiveCheckBox.setSelected(true);
        }
        
        // Add fields to form panel
        addFormField(formPanel, "Cinema:", cinemaComboBox);
        addFormField(formPanel, "Date (YYYY-MM-DD):", dateField);
        formPanel.add(timePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        addFormField(formPanel, "Standard Seat Price:", standardPriceField);
        addFormField(formPanel, "Deluxe Seat Price:", deluxePriceField);
        
        JPanel activePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        activePanel.add(isActiveCheckBox);
        formPanel.add(activePanel);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validate fields
                if (cinemaComboBox.getSelectedItem() == null ||
                    dateField.getText().trim().isEmpty() ||
                    standardPriceField.getText().trim().isEmpty() ||
                    deluxePriceField.getText().trim().isEmpty()) {
                    
                    JOptionPane.showMessageDialog(dialog,
                        "Please fill in all required fields.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Parse prices
                double standardPrice, deluxePrice;
                try {
                    standardPrice = Double.parseDouble(standardPriceField.getText().trim());
                    deluxePrice = Double.parseDouble(deluxePriceField.getText().trim());
                    
                    if (standardPrice <= 0 || deluxePrice <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Prices must be positive numbers.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Parse date and time
                LocalDateTime screeningDateTime;
                try {
                    LocalDate date = LocalDate.parse(dateField.getText().trim());
                    LocalTime time = LocalTime.of(
                        (Integer) hourComboBox.getSelectedItem(),
                        (Integer) minuteComboBox.getSelectedItem()
                    );
                    screeningDateTime = LocalDateTime.of(date, time);
                    
                    // Check if date is in the future
                    if (screeningDateTime.isBefore(LocalDateTime.now())) {
                        JOptionPane.showMessageDialog(dialog,
                            "Screening time must be in the future.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Invalid date format. Please use YYYY-MM-DD.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get selected cinema
                Cinema selectedCinema = (Cinema) cinemaComboBox.getSelectedItem();
                
                boolean success;
                if (screening != null) {
                    // Update existing screening
                    Screening updatedScreening = new Screening(
                        screening.getId(),
                        selectedMovie.getId(),
                        selectedCinema.getId(),
                        screeningDateTime,
                        standardPrice,
                        deluxePrice,
                        isActiveCheckBox.isSelected()
                    );
                    
                    success = screeningController.updateScreening(updatedScreening);
                } else {
                    // Add new screening
                    success = screeningController.createScreening(
                        selectedMovie.getId(),
                        selectedCinema.getId(),
                        screeningDateTime,
                        standardPrice,
                        deluxePrice
                    ) > 0;
                }
                
                if (success) {
                    refreshScreeningsTable();
                    dialog.dispose();
                    
                    JOptionPane.showMessageDialog(mainFrame,
                        screening == null ? "Screening added successfully!" : "Screening updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        screening == null ? "Failed to add screening." : "Failed to update screening.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Show dialog
        dialog.setVisible(true);
    }
    
    /**
     * Helper method to add a form field with label to a panel.
     *
     * @param panel The panel to add the field to
     * @param label The label text
     * @param component The component to add
     */
    private void addFormField(JPanel panel, String label, JComponent component) {
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.add(new JLabel(label), BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);
        panel.add(fieldPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Refreshes the data displayed in the panel.
     */
    public void refreshData() {
        refreshMoviesTable();
        clearScreeningsTable();
        selectedMovie = null;
        selectedScreening = null;
        updateButtonStates();
    }
}