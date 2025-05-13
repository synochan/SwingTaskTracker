package com.cinebook.dao;

import com.cinebook.model.Concession;
import com.cinebook.model.Reservation;
import com.cinebook.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reservation operations.
 * Handles CRUD operations for reservations in the database.
 */
public class ReservationDAO {
    
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private SeatDAO seatDAO = new SeatDAO();
    
    /**
     * Adds a new reservation to the database.
     *
     * @param reservation The Reservation object to add
     * @return The ID of the newly created reservation, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int addReservation(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservations (user_id, guest_name, guest_email, guest_phone, " +
                      "screening_id, reservation_time, total_amount, is_paid) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            // Set user information (either user ID for registered users or guest info for guest checkouts)
            if (reservation.isGuestReservation()) {
                stmt.setNull(1, java.sql.Types.INTEGER);
                stmt.setString(2, reservation.getGuestName());
                stmt.setString(3, reservation.getGuestEmail());
                stmt.setString(4, reservation.getGuestPhone());
            } else {
                stmt.setInt(1, reservation.getUserId());
                stmt.setNull(2, java.sql.Types.VARCHAR);
                stmt.setNull(3, java.sql.Types.VARCHAR);
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }
            
            stmt.setInt(5, reservation.getScreeningId());
            stmt.setString(6, reservation.getReservationTime().format(DB_FORMATTER));
            stmt.setDouble(7, reservation.getTotalAmount());
            stmt.setInt(8, reservation.isPaid() ? 1 : 0);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return -1; // No rows affected, insertion failed
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return -1; // No ID generated, insertion failed
            }
            
            int reservationId = generatedKeys.getInt(1);
            
            // Insert the selected seats
            if (!insertReservationSeats(conn, reservationId, reservation.getSelectedSeats())) {
                conn.rollback();
                return -1;
            }
            
            // Insert the selected concessions
            if (!insertReservationConcessions(conn, reservationId, reservation.getSelectedConcessions())) {
                conn.rollback();
                return -1;
            }
            
            // Update seats to be reserved
            List<Integer> seatIds = new ArrayList<>();
            for (Seat seat : reservation.getSelectedSeats()) {
                seatIds.add(seat.getId());
            }
            
            if (!seatIds.isEmpty() && !seatDAO.updateMultipleSeatReservations(seatIds, true)) {
                conn.rollback();
                return -1;
            }
            
            conn.commit();
            return reservationId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                // Don't close the connection here, as it's managed by DBConnection
            }
        }
    }
    
    /**
     * Helper method to insert reservation-seat associations.
     *
     * @param conn The database connection
     * @param reservationId The ID of the reservation
     * @param seats The list of seats to associate with the reservation
     * @return true if the insertion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean insertReservationSeats(Connection conn, int reservationId, List<Seat> seats) throws SQLException {
        String query = "INSERT INTO reservation_seats (reservation_id, seat_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Seat seat : seats) {
                stmt.setInt(1, reservationId);
                stmt.setInt(2, seat.getId());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            
            // Check if all insertions were successful
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    /**
     * Helper method to insert reservation-concession associations.
     *
     * @param conn The database connection
     * @param reservationId The ID of the reservation
     * @param concessions The list of concessions to associate with the reservation
     * @return true if the insertion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean insertReservationConcessions(Connection conn, int reservationId, List<Concession> concessions) throws SQLException {
        if (concessions.isEmpty()) {
            return true; // No concessions to insert
        }
        
        String query = "INSERT INTO reservation_concessions (reservation_id, concession_id, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Concession concession : concessions) {
                if (concession.getQuantity() > 0) {
                    stmt.setInt(1, reservationId);
                    stmt.setInt(2, concession.getId());
                    stmt.setInt(3, concession.getQuantity());
                    stmt.addBatch();
                }
            }
            
            int[] results = stmt.executeBatch();
            
            // Check if all insertions were successful
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    /**
     * Retrieves a reservation by its ID.
     *
     * @param id The ID of the reservation to retrieve
     * @return The Reservation object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Reservation getReservationById(int id) throws SQLException {
        String query = "SELECT r.*, s.screening_time, m.title as movie_title, c.name as cinema_name " +
                      "FROM reservations r " +
                      "JOIN screenings s ON r.screening_id = s.id " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE r.id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = extractReservationFromResultSet(rs);
                    
                    // Load the selected seats
                    reservation.setSelectedSeats(seatDAO.getSeatsByReservation(id));
                    
                    // Load the selected concessions
                    reservation.setSelectedConcessions(getReservationConcessions(id));
                    
                    return reservation;
                }
            }
        }
        
        return null; // Reservation not found
    }
    
    /**
     * Updates the payment status of a reservation.
     *
     * @param id The ID of the reservation to update
     * @param isPaid The new payment status
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateReservationPaymentStatus(int id, boolean isPaid) throws SQLException {
        String query = "UPDATE reservations SET is_paid = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, isPaid ? 1 : 0);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Cancels a reservation and releases the reserved seats.
     *
     * @param id The ID of the reservation to cancel
     * @return true if the cancellation was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean cancelReservation(int id) throws SQLException {
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Get the seat IDs for this reservation
            List<Integer> seatIds = new ArrayList<>();
            List<Seat> seats = seatDAO.getSeatsByReservation(id);
            for (Seat seat : seats) {
                seatIds.add(seat.getId());
            }
            
            // Release the seats
            if (!seatIds.isEmpty() && !seatDAO.updateMultipleSeatReservations(seatIds, false)) {
                conn.rollback();
                return false;
            }
            
            // Delete the reservation-seat associations
            String deleteSeatsQuery = "DELETE FROM reservation_seats WHERE reservation_id = ?";
            try (PreparedStatement deleteSeatsStmt = conn.prepareStatement(deleteSeatsQuery)) {
                deleteSeatsStmt.setInt(1, id);
                deleteSeatsStmt.executeUpdate();
            }
            
            // Delete the reservation-concession associations
            String deleteConcessionsQuery = "DELETE FROM reservation_concessions WHERE reservation_id = ?";
            try (PreparedStatement deleteConcessionsStmt = conn.prepareStatement(deleteConcessionsQuery)) {
                deleteConcessionsStmt.setInt(1, id);
                deleteConcessionsStmt.executeUpdate();
            }
            
            // Delete the tickets associated with the reservation
            String deleteTicketsQuery = "DELETE FROM tickets WHERE reservation_id = ?";
            try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(deleteTicketsQuery)) {
                deleteTicketsStmt.setInt(1, id);
                deleteTicketsStmt.executeUpdate();
            }
            
            // Delete the payments associated with the reservation
            String deletePaymentsQuery = "DELETE FROM payments WHERE reservation_id = ?";
            try (PreparedStatement deletePaymentsStmt = conn.prepareStatement(deletePaymentsQuery)) {
                deletePaymentsStmt.setInt(1, id);
                deletePaymentsStmt.executeUpdate();
            }
            
            // Delete the reservation
            String deleteReservationQuery = "DELETE FROM reservations WHERE id = ?";
            try (PreparedStatement deleteReservationStmt = conn.prepareStatement(deleteReservationQuery)) {
                deleteReservationStmt.setInt(1, id);
                int affectedRows = deleteReservationStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                // Don't close the connection here, as it's managed by DBConnection
            }
        }
    }
    
    /**
     * Retrieves all reservations for a specific user.
     *
     * @param userId The ID of the user
     * @return A list of reservations for the user
     * @throws SQLException If a database error occurs
     */
    public List<Reservation> getReservationsByUser(int userId) throws SQLException {
        String query = "SELECT r.*, s.screening_time, m.title as movie_title, c.name as cinema_name " +
                      "FROM reservations r " +
                      "JOIN screenings s ON r.screening_id = s.id " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE r.user_id = ? " +
                      "ORDER BY r.reservation_time DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            List<Reservation> reservations = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservationFromResultSet(rs));
                }
            }
            
            return reservations;
        }
    }
    
    /**
     * Retrieves all reservations for a specific screening.
     *
     * @param screeningId The ID of the screening
     * @return A list of reservations for the screening
     * @throws SQLException If a database error occurs
     */
    public List<Reservation> getReservationsByScreening(int screeningId) throws SQLException {
        String query = "SELECT r.*, s.screening_time, m.title as movie_title, c.name as cinema_name " +
                      "FROM reservations r " +
                      "JOIN screenings s ON r.screening_id = s.id " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE r.screening_id = ? " +
                      "ORDER BY r.reservation_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, screeningId);
            
            List<Reservation> reservations = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservationFromResultSet(rs));
                }
            }
            
            return reservations;
        }
    }
    
    /**
     * Retrieves all reservations for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A list of reservations within the date range
     * @throws SQLException If a database error occurs
     */
    public List<Reservation> getReservationsByDateRange(String startDate, String endDate) throws SQLException {
        String query = "SELECT r.*, s.screening_time, m.title as movie_title, c.name as cinema_name " +
                      "FROM reservations r " +
                      "JOIN screenings s ON r.screening_id = s.id " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE date(r.reservation_time) BETWEEN ? AND ? " +
                      "ORDER BY r.reservation_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            List<Reservation> reservations = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(extractReservationFromResultSet(rs));
                }
            }
            
            return reservations;
        }
    }
    
    /**
     * Helper method to get concessions for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @return A list of concessions associated with the reservation
     * @throws SQLException If a database error occurs
     */
    private List<Concession> getReservationConcessions(int reservationId) throws SQLException {
        String query = "SELECT c.*, rc.quantity " +
                      "FROM concessions c " +
                      "JOIN reservation_concessions rc ON c.id = rc.concession_id " +
                      "WHERE rc.reservation_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            
            List<Concession> concessions = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Concession concession = new Concession(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("is_available") == 1,
                        rs.getString("category")
                    );
                    concession.setQuantity(rs.getInt("quantity"));
                    concessions.add(concession);
                }
            }
            
            return concessions;
        }
    }
    
    /**
     * Helper method to extract a Reservation object from a ResultSet.
     *
     * @param rs The ResultSet containing reservation data
     * @return A Reservation object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation;
        
        LocalDateTime reservationTime = LocalDateTime.parse(
            rs.getString("reservation_time"), DB_FORMATTER);
        
        LocalDateTime screeningTime = LocalDateTime.parse(
            rs.getString("screening_time"), DB_FORMATTER);
        
        // Check if it's a guest reservation
        if (rs.getObject("user_id") == null) {
            reservation = new Reservation(
                rs.getString("guest_name"),
                rs.getString("guest_email"),
                rs.getString("guest_phone"),
                rs.getInt("screening_id")
            );
        } else {
            reservation = new Reservation(
                rs.getInt("user_id"),
                rs.getInt("screening_id")
            );
        }
        
        reservation.setId(rs.getInt("id"));
        reservation.setReservationTime(reservationTime);
        reservation.setTotalAmount(rs.getDouble("total_amount"));
        reservation.setPaid(rs.getInt("is_paid") == 1);
        
        // Set additional display fields
        reservation.setMovieTitle(rs.getString("movie_title"));
        reservation.setCinemaName(rs.getString("cinema_name"));
        reservation.setScreeningTime(screeningTime);
        
        return reservation;
    }
}
