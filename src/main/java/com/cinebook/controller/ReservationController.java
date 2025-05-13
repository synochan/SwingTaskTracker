package com.cinebook.controller;

import com.cinebook.dao.ConcessionDAO;
import com.cinebook.dao.ReservationDAO;
import com.cinebook.dao.ScreeningDAO;
import com.cinebook.dao.SeatDAO;
import com.cinebook.dao.TicketDAO;
import com.cinebook.model.Concession;
import com.cinebook.model.Reservation;
import com.cinebook.model.Screening;
import com.cinebook.model.Seat;
import com.cinebook.model.Ticket;
import com.cinebook.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for reservation-related operations.
 * Handles business logic for booking and reservation management.
 */
public class ReservationController {
    private ReservationDAO reservationDAO;
    private ScreeningDAO screeningDAO;
    private SeatDAO seatDAO;
    private ConcessionDAO concessionDAO;
    private TicketDAO ticketDAO;
    
    private Reservation currentReservation; // Store the current reservation being created
    
    /**
     * Constructor for ReservationController.
     */
    public ReservationController() {
        this.reservationDAO = new ReservationDAO();
        this.screeningDAO = new ScreeningDAO();
        this.seatDAO = new SeatDAO();
        this.concessionDAO = new ConcessionDAO();
        this.ticketDAO = new TicketDAO();
        this.currentReservation = null;
    }
    
    /**
     * Starts a new reservation process for a registered user.
     *
     * @param user The user making the reservation
     * @param screeningId The ID of the screening
     * @return true if the reservation was started successfully
     */
    public boolean startReservationForUser(User user, int screeningId) {
        if (user == null) {
            return false;
        }
        
        try {
            Screening screening = screeningDAO.getScreeningById(screeningId);
            if (screening == null) {
                return false;
            }
            
            // Create a new reservation for the user
            currentReservation = new Reservation(user.getId(), screeningId);
            currentReservation.setMovieTitle(screening.getMovieTitle());
            currentReservation.setCinemaName(screening.getCinemaName());
            currentReservation.setScreeningTime(screening.getScreeningTime());
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Starts a new reservation process for a guest.
     *
     * @param guestName The name of the guest
     * @param guestEmail The email of the guest
     * @param guestPhone The phone number of the guest
     * @param screeningId The ID of the screening
     * @return true if the reservation was started successfully
     */
    public boolean startReservationForGuest(String guestName, String guestEmail, String guestPhone, int screeningId) {
        if (guestName == null || guestName.isEmpty() ||
            guestEmail == null || guestEmail.isEmpty() ||
            guestPhone == null || guestPhone.isEmpty()) {
            return false;
        }
        
        try {
            Screening screening = screeningDAO.getScreeningById(screeningId);
            if (screening == null) {
                return false;
            }
            
            // Create a new reservation for the guest
            currentReservation = new Reservation(guestName, guestEmail, guestPhone, screeningId);
            currentReservation.setMovieTitle(screening.getMovieTitle());
            currentReservation.setCinemaName(screening.getCinemaName());
            currentReservation.setScreeningTime(screening.getScreeningTime());
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adds seats to the current reservation.
     *
     * @param selectedSeatIds The IDs of the selected seats
     * @return true if the seats were added successfully
     */
    public boolean addSeatsToReservation(List<Integer> selectedSeatIds) {
        if (currentReservation == null || selectedSeatIds == null || selectedSeatIds.isEmpty()) {
            return false;
        }
        
        try {
            // Clear any previously selected seats
            currentReservation.setSelectedSeats(new ArrayList<>());
            
            // Validation - check if any of the seats are already reserved
            boolean anyReserved = false;
            List<Seat> allSeats = new ArrayList<>();
            
            for (int seatId : selectedSeatIds) {
                Seat seat = seatDAO.getSeatById(seatId);
                if (seat != null) {
                    allSeats.add(seat);
                    if (seat.isReserved()) {
                        anyReserved = true;
                    }
                }
            }
            
            // If any seat is already reserved, cannot proceed
            if (anyReserved) {
                return false;
            }
            
            // Add all the seats to the reservation
            for (Seat seat : allSeats) {
                currentReservation.addSeat(seat);
            }
            
            // Calculate the total amount
            Screening screening = screeningDAO.getScreeningById(currentReservation.getScreeningId());
            currentReservation.calculateTotalAmount(screening);
            
            // Ensure we have at least one seat
            return !currentReservation.getSelectedSeats().isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adds concessions to the current reservation.
     *
     * @param selectedConcessions The list of selected concessions with quantities
     * @return true if the concessions were added successfully
     */
    public boolean addConcessionsToReservation(List<Concession> selectedConcessions) {
        if (currentReservation == null || selectedConcessions == null) {
            return false;
        }
        
        try {
            // Clear any previously selected concessions
            currentReservation.setSelectedConcessions(new ArrayList<>());
            
            // Add each selected concession to the reservation
            for (Concession concession : selectedConcessions) {
                if (concession.getQuantity() > 0) {
                    currentReservation.addConcession(concession);
                }
            }
            
            // Recalculate the total amount
            Screening screening = screeningDAO.getScreeningById(currentReservation.getScreeningId());
            currentReservation.calculateTotalAmount(screening);
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Completes the reservation process.
     *
     * @return The ID of the newly created reservation, or -1 if creation failed
     */
    public int completeReservation() {
        if (currentReservation == null ||
            currentReservation.getSelectedSeats().isEmpty()) {
            return -1;
        }
        
        try {
            // Set the reservation time to now
            currentReservation.setReservationTime(LocalDateTime.now());
            
            // Save the reservation to the database
            int reservationId = reservationDAO.addReservation(currentReservation);
            
            // Clear the current reservation
            currentReservation = null;
            
            return reservationId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Cancels the current reservation process.
     */
    public void cancelReservationProcess() {
        currentReservation = null;
    }
    
    /**
     * Gets the current reservation being created.
     *
     * @return The current Reservation object
     */
    public Reservation getCurrentReservation() {
        return currentReservation;
    }
    
    /**
     * Retrieves a reservation by its ID.
     *
     * @param id The ID of the reservation to retrieve
     * @return The Reservation object if found, null otherwise
     */
    public Reservation getReservationById(int id) {
        try {
            return reservationDAO.getReservationById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all reservations for a user.
     *
     * @param userId The ID of the user
     * @return A list of reservations for the user
     */
    public List<Reservation> getReservationsByUser(int userId) {
        try {
            return reservationDAO.getReservationsByUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Generates tickets for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @return The number of tickets generated
     */
    public int generateTickets(int reservationId) {
        try {
            return ticketDAO.generateTicketsForReservation(reservationId);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Retrieves all tickets for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @return A list of tickets for the reservation
     */
    public List<Ticket> getTicketsByReservation(int reservationId) {
        try {
            return ticketDAO.getTicketsByReservation(reservationId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Cancels a reservation.
     *
     * @param reservationId The ID of the reservation to cancel
     * @return true if the cancellation was successful, false otherwise
     */
    public boolean cancelReservation(int reservationId) {
        try {
            return reservationDAO.cancelReservation(reservationId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves all available concessions.
     *
     * @return A list of all available concessions
     */
    public List<Concession> getAllAvailableConcessions() {
        try {
            return concessionDAO.getAllAvailableConcessions();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Retrieves all concessions of a specific category.
     *
     * @param category The category of concessions to retrieve
     * @return A list of concessions in the specified category
     */
    public List<Concession> getConcessionsByCategory(String category) {
        try {
            return concessionDAO.getConcessionsByCategory(category);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Gets all available seat categories.
     *
     * @return A list of all seat categories
     */
    public List<String> getAvailableConcessionCategories() {
        try {
            List<Concession> allConcessions = concessionDAO.getAllAvailableConcessions();
            List<String> categories = new ArrayList<>();
            
            for (Concession concession : allConcessions) {
                String category = concession.getCategory();
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
            
            return categories;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
