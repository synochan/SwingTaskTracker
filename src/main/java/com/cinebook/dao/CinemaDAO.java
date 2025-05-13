package com.cinebook.dao;

import com.cinebook.model.Cinema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Cinema operations.
 * Handles CRUD operations for cinemas in the database.
 */
public class CinemaDAO {
    
    /**
     * Adds a new cinema to the database.
     *
     * @param cinema The Cinema object to add
     * @return The ID of the newly created cinema, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int addCinema(Cinema cinema) throws SQLException {
        String query = "INSERT INTO cinemas (name, location, total_seats, total_rows, " +
                      "seats_per_row, has_deluxe_seats, is_active) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cinema.getName());
            stmt.setString(2, cinema.getLocation());
            stmt.setInt(3, cinema.getTotalSeats());
            stmt.setInt(4, cinema.getTotalRows());
            stmt.setInt(5, cinema.getSeatsPerRow());
            stmt.setInt(6, cinema.hasDeluxeSeats() ? 1 : 0);
            stmt.setInt(7, cinema.isActive() ? 1 : 0);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return -1; // No rows affected, insertion failed
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated ID
                } else {
                    return -1; // No ID generated, insertion failed
                }
            }
        }
    }
    
    /**
     * Retrieves a cinema by its ID.
     *
     * @param id The ID of the cinema to retrieve
     * @return The Cinema object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Cinema getCinemaById(int id) throws SQLException {
        String query = "SELECT * FROM cinemas WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCinemaFromResultSet(rs);
                }
            }
        }
        
        return null; // Cinema not found
    }
    
    /**
     * Updates an existing cinema in the database.
     *
     * @param cinema The Cinema object with updated information
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateCinema(Cinema cinema) throws SQLException {
        String query = "UPDATE cinemas SET name = ?, location = ?, total_seats = ?, " +
                      "total_rows = ?, seats_per_row = ?, has_deluxe_seats = ?, " +
                      "is_active = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, cinema.getName());
            stmt.setString(2, cinema.getLocation());
            stmt.setInt(3, cinema.getTotalSeats());
            stmt.setInt(4, cinema.getTotalRows());
            stmt.setInt(5, cinema.getSeatsPerRow());
            stmt.setInt(6, cinema.hasDeluxeSeats() ? 1 : 0);
            stmt.setInt(7, cinema.isActive() ? 1 : 0);
            stmt.setInt(8, cinema.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a cinema from the database.
     *
     * @param id The ID of the cinema to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteCinema(int id) throws SQLException {
        String query = "DELETE FROM cinemas WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Retrieves all cinemas from the database.
     *
     * @return A list of all cinemas
     * @throws SQLException If a database error occurs
     */
    public List<Cinema> getAllCinemas() throws SQLException {
        String query = "SELECT * FROM cinemas ORDER BY name";
        List<Cinema> cinemas = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                cinemas.add(extractCinemaFromResultSet(rs));
            }
        }
        
        return cinemas;
    }
    
    /**
     * Retrieves all active cinemas from the database.
     *
     * @return A list of all active cinemas
     * @throws SQLException If a database error occurs
     */
    public List<Cinema> getAllActiveCinemas() throws SQLException {
        String query = "SELECT * FROM cinemas WHERE is_active = 1 ORDER BY name";
        List<Cinema> cinemas = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                cinemas.add(extractCinemaFromResultSet(rs));
            }
        }
        
        return cinemas;
    }
    
    /**
     * Helper method to extract a Cinema object from a ResultSet.
     *
     * @param rs The ResultSet containing cinema data
     * @return A Cinema object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Cinema extractCinemaFromResultSet(ResultSet rs) throws SQLException {
        Cinema cinema = new Cinema();
        cinema.setId(rs.getInt("id"));
        cinema.setName(rs.getString("name"));
        cinema.setLocation(rs.getString("location"));
        cinema.setTotalSeats(rs.getInt("total_seats"));
        cinema.setTotalRows(rs.getInt("total_rows"));
        cinema.setSeatsPerRow(rs.getInt("seats_per_row"));
        cinema.setHasDeluxeSeats(rs.getInt("has_deluxe_seats") == 1);
        cinema.setActive(rs.getInt("is_active") == 1);
        return cinema;
    }
}
