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
                }
                
                int[] results = stmt.executeBatch();
                
                // Check if all updates were successful
                for (int result : results) {
                    if (result <= 0) {
                        success = false;
                        break;
                    }
                }
                
                if (success) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback();
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
