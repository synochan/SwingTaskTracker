package com.cinebook.dao;

import com.cinebook.model.Concession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Concession operations.
 * Handles CRUD operations for concessions in the database.
 */
public class ConcessionDAO {
    
    /**
     * Adds a new concession to the database.
     *
     * @param concession The Concession object to add
     * @return The ID of the newly created concession, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int addConcession(Concession concession) throws SQLException {
        String query = "INSERT INTO concessions (name, description, price, category, is_available) " +
                      "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, concession.getName());
            stmt.setString(2, concession.getDescription());
            stmt.setDouble(3, concession.getPrice());
            stmt.setString(4, concession.getCategory());
            stmt.setInt(5, concession.isAvailable() ? 1 : 0);
            
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
     * Retrieves a concession by its ID.
     *
     * @param id The ID of the concession to retrieve
     * @return The Concession object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Concession getConcessionById(int id) throws SQLException {
        String query = "SELECT * FROM concessions WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractConcessionFromResultSet(rs);
                }
            }
        }
        
        return null; // Concession not found
    }
    
    /**
     * Updates an existing concession in the database.
     *
     * @param concession The Concession object with updated information
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateConcession(Concession concession) throws SQLException {
        String query = "UPDATE concessions SET name = ?, description = ?, price = ?, " +
                      "category = ?, is_available = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, concession.getName());
            stmt.setString(2, concession.getDescription());
            stmt.setDouble(3, concession.getPrice());
            stmt.setString(4, concession.getCategory());
            stmt.setInt(5, concession.isAvailable() ? 1 : 0);
            stmt.setInt(6, concession.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a concession from the database.
     *
     * @param id The ID of the concession to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteConcession(int id) throws SQLException {
        String query = "DELETE FROM concessions WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Retrieves all concessions from the database.
     *
     * @return A list of all concessions
     * @throws SQLException If a database error occurs
     */
    public List<Concession> getAllConcessions() throws SQLException {
        String query = "SELECT * FROM concessions ORDER BY category, name";
        List<Concession> concessions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                concessions.add(extractConcessionFromResultSet(rs));
            }
        }
        
        return concessions;
    }
    
    /**
     * Retrieves all available concessions from the database.
     *
     * @return A list of all available concessions
     * @throws SQLException If a database error occurs
     */
    public List<Concession> getAllAvailableConcessions() throws SQLException {
        String query = "SELECT * FROM concessions WHERE is_available = 1 ORDER BY category, name";
        List<Concession> concessions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                concessions.add(extractConcessionFromResultSet(rs));
            }
        }
        
        return concessions;
    }
    
    /**
     * Retrieves all concessions of a specific category.
     *
     * @param category The category of concessions to retrieve
     * @return A list of concessions in the specified category
     * @throws SQLException If a database error occurs
     */
    public List<Concession> getConcessionsByCategory(String category) throws SQLException {
        String query = "SELECT * FROM concessions WHERE category = ? AND is_available = 1 ORDER BY name";
        List<Concession> concessions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    concessions.add(extractConcessionFromResultSet(rs));
                }
            }
        }
        
        return concessions;
    }
    
    /**
     * Helper method to extract a Concession object from a ResultSet.
     *
     * @param rs The ResultSet containing concession data
     * @return A Concession object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Concession extractConcessionFromResultSet(ResultSet rs) throws SQLException {
        Concession concession = new Concession();
        concession.setId(rs.getInt("id"));
        concession.setName(rs.getString("name"));
        concession.setDescription(rs.getString("description"));
        concession.setPrice(rs.getDouble("price"));
        concession.setCategory(rs.getString("category"));
        concession.setAvailable(rs.getInt("is_available") == 1);
        concession.setQuantity(0); // Default quantity is 0
        return concession;
    }
}
