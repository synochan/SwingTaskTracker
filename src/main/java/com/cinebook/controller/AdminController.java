package com.cinebook.controller;

import com.cinebook.dao.CinemaDAO;
import com.cinebook.dao.ConcessionDAO;
import com.cinebook.dao.PaymentDAO;
import com.cinebook.dao.ReservationDAO;
import com.cinebook.dao.UserDAO;
import com.cinebook.model.Cinema;
import com.cinebook.model.Concession;
import com.cinebook.model.Payment;
import com.cinebook.model.Reservation;
import com.cinebook.model.User;
import com.cinebook.util.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin-related operations.
 * Handles business logic for administration tasks.
 */
public class AdminController {
    private UserController userController;
    private MovieController movieController;
    private ScreeningController screeningController;
    private CinemaDAO cinemaDAO;
    private ConcessionDAO concessionDAO;
    private PaymentDAO paymentDAO;
    private ReservationDAO reservationDAO;
    private UserDAO userDAO;
    
    /**
     * Constructor for AdminController.
     */
    public AdminController() {
        this.userController = new UserController();
        this.movieController = new MovieController();
        this.screeningController = new ScreeningController();
        this.cinemaDAO = new CinemaDAO();
        this.concessionDAO = new ConcessionDAO();
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Constructor for AdminController with an existing UserController.
     *
     * @param userController The UserController to use
     */
    public AdminController(UserController userController) {
        this.userController = userController;
        this.movieController = new MovieController();
        this.screeningController = new ScreeningController();
        this.cinemaDAO = new CinemaDAO();
        this.concessionDAO = new ConcessionDAO();
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Checks if the current user is an admin.
     *
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        return userController.isCurrentUserAdmin();
    }
    
    /**
     * Gets all users from the database.
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return userController.getAllUsers();
    }
    
    /**
     * Creates a new user account.
     *
     * @param username The username for the new account
     * @param password The password for the new account
     * @param email The email for the new account
     * @param phoneNumber The phone number for the new account
     * @param fullName The full name for the new account
     * @param isAdmin Whether the new account is an admin
     * @return true if account creation is successful, false otherwise
     */
    public boolean createUser(String username, String password, String email, 
                            String phoneNumber, String fullName, boolean isAdmin) {
        return userController.registerUser(username, password, email, phoneNumber, fullName, isAdmin);
    }
    
    /**
     * Adds a new user.
     *
     * @param user The User object to add
     * @return The ID of the newly created user, or -1 if creation failed
     */
    public int addUser(User user) {
        try {
            // Hash the password before storing
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            
            return userDAO.addUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Updates an existing user.
     *
     * @param user The User object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        return userController.updateUserProfile(user);
    }
    
    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteUser(int id) {
        return userController.deleteUser(id);
    }
    
    /**
     * Resets a user's password.
     *
     * @param userId The ID of the user
     * @param newPassword The new password
     * @return true if the reset was successful, false otherwise
     */
    public boolean resetUserPassword(int userId, String newPassword) {
        try {
            // Hash the new password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            // Get the user
            User user = userController.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // Update the password
            user.setPassword(hashedPassword);
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Sets a user's admin status.
     *
     * @param userId The ID of the user
     * @param isAdmin The new admin status
     * @return true if the update was successful, false otherwise
     */
    public boolean setUserAdminStatus(int userId, boolean isAdmin) {
        try {
            // Get the user
            User user = userController.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // Update the admin status
            user.setAdmin(isAdmin);
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adds a new cinema to the database.
     *
     * @param cinema The Cinema object to add
     * @return The ID of the newly created cinema, or -1 if creation failed
     */
    public int addCinema(Cinema cinema) {
        try {
            return cinemaDAO.addCinema(cinema);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Updates an existing cinema in the database.
     *
     * @param cinema The Cinema object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateCinema(Cinema cinema) {
        try {
            return cinemaDAO.updateCinema(cinema);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves a cinema by its ID.
     *
     * @param id The ID of the cinema to retrieve
     * @return The Cinema object if found, null otherwise
     */
    public Cinema getCinemaById(int id) {
        try {
            return cinemaDAO.getCinemaById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all cinemas from the database.
     *
     * @return A list of all cinemas
     */
    public List<Cinema> getAllCinemas() {
        try {
            return cinemaDAO.getAllCinemas();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Adds a new concession to the database.
     *
     * @param concession The Concession object to add
     * @return The ID of the newly created concession, or -1 if creation failed
     */
    public int addConcession(Concession concession) {
        try {
            return concessionDAO.addConcession(concession);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Updates an existing concession in the database.
     *
     * @param concession The Concession object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateConcession(Concession concession) {
        try {
            return concessionDAO.updateConcession(concession);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves a concession by its ID.
     *
     * @param id The ID of the concession to retrieve
     * @return The Concession object if found, null otherwise
     */
    public Concession getConcessionById(int id) {
        try {
            return concessionDAO.getConcessionById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all concessions from the database.
     *
     * @return A list of all concessions
     */
    public List<Concession> getAllConcessions() {
        try {
            return concessionDAO.getAllConcessions();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Generates a daily sales report.
     *
     * @param date The date for the report (format: YYYY-MM-DD)
     * @return The total sales for the day
     */
    public double getDailySalesReport(String date) {
        try {
            return paymentDAO.calculateTotalSales(date, date);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Generates a weekly sales report.
     *
     * @param startDate The start date of the week (format: YYYY-MM-DD)
     * @param endDate The end date of the week (format: YYYY-MM-DD)
     * @return The total sales for the week
     */
    public double getWeeklySalesReport(String startDate, String endDate) {
        try {
            return paymentDAO.calculateTotalSales(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Generates a monthly sales report.
     *
     * @param year The year (format: YYYY)
     * @param month The month (1-12)
     * @return The total sales for the month
     */
    public double getMonthlySalesReport(int year, int month) {
        try {
            // Create date range for the month
            LocalDate firstDay = LocalDate.of(year, month, 1);
            LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String startDate = firstDay.format(formatter);
            String endDate = lastDay.format(formatter);
            
            return paymentDAO.calculateTotalSales(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Gets all payments for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A list of payments within the date range
     */
    public List<Payment> getPaymentsByDateRange(String startDate, String endDate) {
        try {
            return paymentDAO.getPaymentsByDateRange(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets all reservations for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A list of reservations within the date range
     */
    public List<Reservation> getReservationsByDateRange(String startDate, String endDate) {
        try {
            return reservationDAO.getReservationsByDateRange(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Generates a sales report by movie.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A map of movie titles to total sales
     */
    public Map<String, Double> getSalesByMovie(String startDate, String endDate) {
        try {
            List<Reservation> reservations = reservationDAO.getReservationsByDateRange(startDate, endDate);
            Map<String, Double> salesByMovie = new HashMap<>();
            
            for (Reservation reservation : reservations) {
                if (reservation.isPaid()) {
                    String movieTitle = reservation.getMovieTitle();
                    double amount = reservation.getTotalAmount();
                    
                    if (salesByMovie.containsKey(movieTitle)) {
                        salesByMovie.put(movieTitle, salesByMovie.get(movieTitle) + amount);
                    } else {
                        salesByMovie.put(movieTitle, amount);
                    }
                }
            }
            
            return salesByMovie;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Generates a sales report by cinema.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A map of cinema names to total sales
     */
    public Map<String, Double> getSalesByCinema(String startDate, String endDate) {
        try {
            List<Reservation> reservations = reservationDAO.getReservationsByDateRange(startDate, endDate);
            Map<String, Double> salesByCinema = new HashMap<>();
            
            for (Reservation reservation : reservations) {
                if (reservation.isPaid()) {
                    String cinemaName = reservation.getCinemaName();
                    double amount = reservation.getTotalAmount();
                    
                    if (salesByCinema.containsKey(cinemaName)) {
                        salesByCinema.put(cinemaName, salesByCinema.get(cinemaName) + amount);
                    } else {
                        salesByCinema.put(cinemaName, amount);
                    }
                }
            }
            
            return salesByCinema;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    /**
     * Gets the total number of tickets sold for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return The total number of tickets sold
     */
    public int getTicketsSold(String startDate, String endDate) {
        try {
            List<Reservation> reservations = reservationDAO.getReservationsByDateRange(startDate, endDate);
            int ticketCount = 0;
            
            for (Reservation reservation : reservations) {
                if (reservation.isPaid()) {
                    ticketCount += reservation.getSelectedSeats().size();
                }
            }
            
            return ticketCount;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
