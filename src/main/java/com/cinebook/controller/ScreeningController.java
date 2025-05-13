package com.cinebook.controller;

import com.cinebook.dao.CinemaDAO;
import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.ScreeningDAO;
import com.cinebook.dao.SeatDAO;
import com.cinebook.model.Cinema;
import com.cinebook.model.Movie;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for screening-related operations.
 * Handles business logic for screening management.
 */
public class ScreeningController {
    private ScreeningDAO screeningDAO;
    private MovieDAO movieDAO;
    private CinemaDAO cinemaDAO;
    private SeatDAO seatDAO;
    
    /**
     * Constructor for ScreeningController.
     */
    public ScreeningController() {
        this.screeningDAO = new ScreeningDAO();
        this.movieDAO = new MovieDAO();
        this.cinemaDAO = new CinemaDAO();
        this.seatDAO = new SeatDAO();
    }
    
    /**
     * Adds a new screening to the database.
     *
     * @param screening The Screening object to add
     * @return The ID of the newly created screening, or -1 if creation failed
     */
    public int addScreening(Screening screening) {
        try {
            return screeningDAO.addScreening(screening);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Updates an existing screening in the database.
     *
     * @param screening The Screening object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateScreening(Screening screening) {
        try {
            return screeningDAO.updateScreening(screening);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a screening from the database.
     *
     * @param id The ID of the screening to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteScreening(int id) {
        try {
            return screeningDAO.deleteScreening(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves a screening by its ID.
     *
     * @param id The ID of the screening to retrieve
     * @return The Screening object if found, null otherwise
     */
    public Screening getScreeningById(int id) {
        try {
            return screeningDAO.getScreeningById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all screenings from the database.
     *
     * @return A list of all screenings
     */
    public List<Screening> getAllScreenings() {
        try {
            return screeningDAO.getAllScreenings();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all future screenings.
     *
     * @return A list of future screenings
     */
    public List<Screening> getFutureScreenings() {
        try {
            return screeningDAO.getFutureScreenings();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all screenings for a specific date.
     *
     * @param date The date to retrieve screenings for (format: YYYY-MM-DD)
     * @return A list of screenings on the specified date
     */
    public List<Screening> getScreeningsByDate(String date) {
        try {
            return screeningDAO.getScreeningsByDate(date);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all screenings for the next 7 days.
     *
     * @return A list of screenings for the next 7 days
     */
    public List<Screening> getScreeningsForNext7Days() {
        try {
            List<Screening> allScreenings = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.plusDays(i);
                String dateStr = date.format(formatter);
                allScreenings.addAll(screeningDAO.getScreeningsByDate(dateStr));
            }
            
            return allScreenings;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all screenings for a specific cinema.
     *
     * @param cinemaId The ID of the cinema
     * @return A list of screenings for the cinema
     */
    public List<Screening> getScreeningsByCinema(int cinemaId) {
        try {
            return screeningDAO.getScreeningsByCinema(cinemaId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all seats for a specific screening.
     *
     * @param screeningId The ID of the screening
     * @return A list of seats for the screening
     */
    public List<Seat> getSeatsByScreening(int screeningId) {
        try {
            return seatDAO.getSeatsByScreening(screeningId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all movies from the database.
     *
     * @return A list of all movies
     */
    public List<Movie> getAllMovies() {
        try {
            return movieDAO.getAllMovies();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all active movies from the database.
     *
     * @return A list of all active movies
     */
    public List<Movie> getAllActiveMovies() {
        try {
            return movieDAO.getAllActiveMovies();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
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
     * Retrieves all active cinemas from the database.
     *
     * @return A list of all active cinemas
     */
    public List<Cinema> getAllActiveCinemas() {
        try {
            return cinemaDAO.getAllActiveCinemas();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks if a screening has any reserved seats.
     *
     * @param screeningId The ID of the screening to check
     * @return true if the screening has reserved seats, false otherwise
     */
    public boolean hasReservedSeats(int screeningId) {
        try {
            List<Seat> seats = seatDAO.getSeatsByScreening(screeningId);
            for (Seat seat : seats) {
                if (seat.isReserved()) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a new screening with the specified parameters.
     *
     * @param movieId The ID of the movie
     * @param cinemaId The ID of the cinema
     * @param screeningDateTime The date and time of the screening
     * @param standardSeatPrice The price of standard seats
     * @param deluxeSeatPrice The price of deluxe seats
     * @return The ID of the newly created screening, or -1 if creation failed
     */
    public int createScreening(int movieId, int cinemaId, LocalDateTime screeningDateTime, 
                             double standardSeatPrice, double deluxeSeatPrice) {
        try {
            Screening newScreening = new Screening(
                movieId, cinemaId, screeningDateTime, standardSeatPrice, deluxeSeatPrice, true);
            return screeningDAO.addScreening(newScreening);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
