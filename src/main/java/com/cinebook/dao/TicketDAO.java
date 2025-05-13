package com.cinebook.dao;

import com.cinebook.model.SeatType;
import com.cinebook.model.Ticket;

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
 * Data Access Object for Ticket operations.
 * Handles CRUD operations for tickets in the database.
 */
public class TicketDAO {
    
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generates tickets for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @return The number of tickets generated
     * @throws SQLException If a database error occurs
     */
    public int generateTicketsForReservation(int reservationId) throws SQLException {
        String query = "INSERT INTO tickets (reservation_id, seat_id, ticket_code, is_used, generation_time) " +
                      "SELECT rs.reservation_id, rs.seat_id, ?, 0, ? " +
                      "FROM reservation_seats rs " +
                      "WHERE rs.reservation_id = ?";
        
        int ticketsGenerated = 0;
        
        try (Connection conn = DBConnection.getConnection()) {
            // Get seats for this reservation
            String seatQuery = "SELECT seat_id FROM reservation_seats WHERE reservation_id = ?";
            List<Integer> seatIds = new ArrayList<>();
            
            try (PreparedStatement seatStmt = conn.prepareStatement(seatQuery)) {
                seatStmt.setInt(1, reservationId);
                
                try (ResultSet rs = seatStmt.executeQuery()) {
                    while (rs.next()) {
                        seatIds.add(rs.getInt("seat_id"));
                    }
                }
            }
            
            // Generate a ticket for each seat
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                for (int seatId : seatIds) {
                    // Generate a unique ticket code
                    String ticketCode = generateUniqueTicketCode();
                    
                    stmt.setString(1, ticketCode);
                    stmt.setString(2, LocalDateTime.now().format(DB_FORMATTER));
                    stmt.setInt(3, reservationId);
                    
                    int affectedRows = stmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        ticketsGenerated++;
                    }
                }
            }
        }
        
        return ticketsGenerated;
    }
    
    /**
     * Generates a unique ticket code.
     *
     * @return A unique ticket code
     */
    private String generateUniqueTicketCode() {
        // Format: TICK-yyyyMMdd-RandomDigits
        String prefix = "TICK";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomDigits = String.format("%06d", (int) (Math.random() * 1000000));
        return prefix + "-" + dateStr + "-" + randomDigits;
    }
    
    /**
     * Retrieves a ticket by its ID.
     *
     * @param id The ID of the ticket to retrieve
     * @return The Ticket object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Ticket getTicketById(int id) throws SQLException {
        String query = "SELECT t.*, s.seat_number, s.seat_type, " +
                      "sc.screening_time, m.title as movie_title, c.name as cinema_name, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM tickets t " +
                      "JOIN seats s ON t.seat_id = s.id " +
                      "JOIN reservations r ON t.reservation_id = r.id " +
                      "JOIN screenings sc ON r.screening_id = sc.id " +
                      "JOIN movies m ON sc.movie_id = m.id " +
                      "JOIN cinemas c ON sc.cinema_id = c.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE t.id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTicketFromResultSet(rs);
                }
            }
        }
        
        return null; // Ticket not found
    }
    
    /**
     * Retrieves a ticket by its code.
     *
     * @param ticketCode The code of the ticket to retrieve
     * @return The Ticket object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Ticket getTicketByCode(String ticketCode) throws SQLException {
        String query = "SELECT t.*, s.seat_number, s.seat_type, " +
                      "sc.screening_time, m.title as movie_title, c.name as cinema_name, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM tickets t " +
                      "JOIN seats s ON t.seat_id = s.id " +
                      "JOIN reservations r ON t.reservation_id = r.id " +
                      "JOIN screenings sc ON r.screening_id = sc.id " +
                      "JOIN movies m ON sc.movie_id = m.id " +
                      "JOIN cinemas c ON sc.cinema_id = c.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE t.ticket_code = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, ticketCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractTicketFromResultSet(rs);
                }
            }
        }
        
        return null; // Ticket not found
    }
    
    /**
     * Updates the used status of a ticket.
     *
     * @param id The ID of the ticket to update
     * @param isUsed The new used status
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateTicketUsedStatus(int id, boolean isUsed) throws SQLException {
        String query = "UPDATE tickets SET is_used = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, isUsed ? 1 : 0);
            stmt.setInt(2, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Retrieves all tickets for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @return A list of tickets for the reservation
     * @throws SQLException If a database error occurs
     */
    public List<Ticket> getTicketsByReservation(int reservationId) throws SQLException {
        String query = "SELECT t.*, s.seat_number, s.seat_type, " +
                      "sc.screening_time, m.title as movie_title, c.name as cinema_name, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM tickets t " +
                      "JOIN seats s ON t.seat_id = s.id " +
                      "JOIN reservations r ON t.reservation_id = r.id " +
                      "JOIN screenings sc ON r.screening_id = sc.id " +
                      "JOIN movies m ON sc.movie_id = m.id " +
                      "JOIN cinemas c ON sc.cinema_id = c.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE t.reservation_id = ? " +
                      "ORDER BY s.seat_number";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            
            List<Ticket> tickets = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(extractTicketFromResultSet(rs));
                }
            }
            
            return tickets;
        }
    }
    
    /**
     * Helper method to extract a Ticket object from a ResultSet.
     *
     * @param rs The ResultSet containing ticket data
     * @return A Ticket object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        
        ticket.setId(rs.getInt("id"));
        ticket.setReservationId(rs.getInt("reservation_id"));
        ticket.setSeatId(rs.getInt("seat_id"));
        ticket.setTicketCode(rs.getString("ticket_code"));
        ticket.setUsed(rs.getInt("is_used") == 1);
        
        LocalDateTime generationTime = LocalDateTime.parse(
            rs.getString("generation_time"), DB_FORMATTER);
        ticket.setGenerationTime(generationTime);
        
        // Set additional display fields
        ticket.setSeatNumber(rs.getString("seat_number"));
        ticket.setSeatType(SeatType.valueOf(rs.getString("seat_type")));
        
        LocalDateTime screeningTime = LocalDateTime.parse(
            rs.getString("screening_time"), DB_FORMATTER);
        ticket.setScreeningTime(screeningTime);
        
        ticket.setMovieTitle(rs.getString("movie_title"));
        ticket.setCinemaName(rs.getString("cinema_name"));
        ticket.setCustomerName(rs.getString("customer_name"));
        
        return ticket;
    }
}
