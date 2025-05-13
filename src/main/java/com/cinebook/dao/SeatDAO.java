package com.cinebook.dao;

import com.cinebook.model.Seat;
import com.cinebook.model.SeatType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Seat operations.
 * Handles operations for seats in the database.
 */
public class SeatDAO {
    
    /**
     * Retrieves a seat by its ID.
     *
     * @param id The ID of the seat to retrieve
     * @return The Seat object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Seat getSeatById(int id) throws SQLException {
        String query = "SELECT * FROM seats WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSeatFromResultSet(rs);
                }
            }
        }
        
        return null; // Seat not found
    }
    
    /**
     * Retrieves all seats for a specific screening.
     *
     * @param screeningId The ID of the screening
     * @return A list of seats for the screening
     * @throws SQLException If a database error occurs
     */
    public List<Seat> getSeatsByScreening(int screeningId) throws SQLException {
        String query = "SELECT * FROM seats WHERE screening_id = ? ORDER BY row_number, column_number";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, screeningId);
            
            List<Seat> seats = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(extractSeatFromResultSet(rs));
                }
            }
            
            return seats;
        }
    }
    
    /**
     * Updates the reservation status of a seat.
     *
     * @param seatId The ID of the seat to update
     * @param isReserved The new reservation status
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateSeatReservation(int seatId, boolean isReserved) throws SQLException {
        String query = "UPDATE seats SET is_reserved = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, isReserved ? 1 : 0);
            stmt.setInt(2, seatId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Updates the reservation status of multiple seats.
     *
     * @param seatIds The IDs of the seats to update
     * @param isReserved The new reservation status
     * @return true if all updates were successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateMultipleSeatReservations(List<Integer> seatIds, boolean isReserved) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) {
            System.err.println("WARNING: No seat IDs provided for updating reservation status");
            return true; // Return true as there's nothing to update
        }
        
        System.out.println("Updating seat reservations: seats=" + seatIds + ", isReserved=" + isReserved);
        
        String query = "UPDATE seats SET is_reserved = ? WHERE id = ?";
        boolean success = true;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            conn.setAutoCommit(false);
            
            try {
                for (int seatId : seatIds) {
                    stmt.setInt(1, isReserved ? 1 : 0);
                    stmt.setInt(2, seatId);
                    stmt.addBatch();
                    
                    // Print debug info
                    System.out.println("Adding to batch: UPDATE seats SET is_reserved = " + 
                                      (isReserved ? 1 : 0) + " WHERE id = " + seatId);
                }
                
                int[] results = stmt.executeBatch();
                
                // Log the results
                System.out.println("Batch execution results: ");
                for (int i = 0; i < results.length; i++) {
                    System.out.println("Seat " + seatIds.get(i) + ": " + 
                                     (results[i] > 0 ? "Updated" : "Not updated"));
                    
                    // Consider the update successful even if the seat wasn't changed
                    // (this handles the case where a seat reservation status is already correct)
                    if (results[i] < 0 && results[i] != java.sql.Statement.SUCCESS_NO_INFO) {
                        success = false;
                    }
                }
                
                if (success) {
                    conn.commit();
                    System.out.println("Successfully committed seat reservation updates");
                } else {
                    conn.rollback();
                    System.err.println("Rolled back seat reservation updates due to error");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("SQL exception during seat reservation update: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        
        return success;
    }
    
    /**
     * Retrieves all seats reserved for a specific reservation.
     *
     * @param reservationId The ID of the reservation
     * @return A list of seats for the reservation
     * @throws SQLException If a database error occurs
     */
    public List<Seat> getSeatsByReservation(int reservationId) throws SQLException {
        String query = "SELECT s.* FROM seats s " +
                      "JOIN reservation_seats rs ON s.id = rs.seat_id " +
                      "WHERE rs.reservation_id = ? " +
                      "ORDER BY s.row_number, s.column_number";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            
            List<Seat> seats = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(extractSeatFromResultSet(rs));
                }
            }
            
            return seats;
        }
    }
    
    /**
     * Helper method to extract a Seat object from a ResultSet.
     *
     * @param rs The ResultSet containing seat data
     * @return A Seat object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Seat extractSeatFromResultSet(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setId(rs.getInt("id"));
        seat.setScreeningId(rs.getInt("screening_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatType(SeatType.valueOf(rs.getString("seat_type")));
        seat.setReserved(rs.getInt("is_reserved") == 1);
        seat.setRowNumber(rs.getInt("row_number"));
        seat.setColumnNumber(rs.getInt("column_number"));
        return seat;
    }
}
