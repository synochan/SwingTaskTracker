package com.cinebook.dao;

import com.cinebook.model.Screening;

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
 * Data Access Object for Screening operations.
 * Handles CRUD operations for movie screenings in the database.
 */
public class ScreeningDAO {
    
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Adds a new screening to the database.
     *
     * @param screening The Screening object to add
     * @return The ID of the newly created screening, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int addScreening(Screening screening) throws SQLException {
        String query = "INSERT INTO screenings (movie_id, cinema_id, screening_time, " +
                      "standard_seat_price, deluxe_seat_price, is_active) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, screening.getMovieId());
            stmt.setInt(2, screening.getCinemaId());
            stmt.setString(3, screening.getScreeningTime().format(DB_FORMATTER));
            stmt.setDouble(4, screening.getStandardSeatPrice());
            stmt.setDouble(5, screening.getDeluxeSeatPrice());
            stmt.setInt(6, screening.isActive() ? 1 : 0);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return -1; // No rows affected, insertion failed
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int screeningId = generatedKeys.getInt(1);
                    
                    // Generate seats for this screening
                    generateSeatsForScreening(screeningId, screening.getCinemaId());
                    
                    return screeningId; // Return the generated ID
                } else {
                    return -1; // No ID generated, insertion failed
                }
            }
        }
    }
    
    /**
     * Generates seats for a new screening based on the cinema configuration.
     *
     * @param screeningId The ID of the screening to generate seats for
     * @param cinemaId The ID of the cinema to get configuration from
     * @throws SQLException If a database error occurs
     */
    private void generateSeatsForScreening(int screeningId, int cinemaId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            // Get cinema details
            String cinemaQuery = "SELECT total_rows, seats_per_row, has_deluxe_seats FROM cinemas WHERE id = ?";
            try (PreparedStatement cinemaStmt = conn.prepareStatement(cinemaQuery)) {
                cinemaStmt.setInt(1, cinemaId);
                ResultSet cinemaRs = cinemaStmt.executeQuery();
                
                if (cinemaRs.next()) {
                    int totalRows = cinemaRs.getInt("total_rows");
                    int seatsPerRow = cinemaRs.getInt("seats_per_row");
                    boolean hasDeluxeSeats = cinemaRs.getInt("has_deluxe_seats") == 1;
                    
                    // Define the last few rows as deluxe (if cinema has deluxe seats)
                    int deluxeRowStart = hasDeluxeSeats ? totalRows - 2 : totalRows + 1; // No deluxe seats if row > totalRows
                    
                    // Prepare seat insertion statement
                    String seatQuery = "INSERT INTO seats (screening_id, seat_number, seat_type, is_reserved, row_number, column_number) " +
                                      "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement seatStmt = conn.prepareStatement(seatQuery)) {
                        
                        // Generate seats row by row
                        for (int row = 1; row <= totalRows; row++) {
                            String rowLabel = getRowLabel(row);
                            String seatType = (row >= deluxeRowStart) ? "DELUXE" : "STANDARD";
                            
                            for (int column = 1; column <= seatsPerRow; column++) {
                                String seatNumber = rowLabel + column;
                                
                                seatStmt.setInt(1, screeningId);
                                seatStmt.setString(2, seatNumber);
                                seatStmt.setString(3, seatType);
                                seatStmt.setInt(4, 0); // Not reserved initially
                                seatStmt.setInt(5, row);
                                seatStmt.setInt(6, column);
                                
                                seatStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Converts a row number to a letter label (A-Z, then AA, AB, etc.)
     *
     * @param rowNumber The numeric row number
     * @return A letter label for the row
     */
    private String getRowLabel(int rowNumber) {
        if (rowNumber <= 26) {
            return String.valueOf((char) ('A' + rowNumber - 1));
        } else {
            int firstChar = (rowNumber - 1) / 26;
            int secondChar = (rowNumber - 1) % 26;
            return String.valueOf((char) ('A' + firstChar - 1)) + String.valueOf((char) ('A' + secondChar));
        }
    }
    
    /**
     * Retrieves a screening by its ID.
     *
     * @param id The ID of the screening to retrieve
     * @return The Screening object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Screening getScreeningById(int id) throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE s.id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractScreeningFromResultSet(rs, true);
                }
            }
        }
        
        return null; // Screening not found
    }
    
    /**
     * Updates an existing screening in the database.
     *
     * @param screening The Screening object with updated information
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateScreening(Screening screening) throws SQLException {
        String query = "UPDATE screenings SET movie_id = ?, cinema_id = ?, screening_time = ?, " +
                      "standard_seat_price = ?, deluxe_seat_price = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, screening.getMovieId());
            stmt.setInt(2, screening.getCinemaId());
            stmt.setString(3, screening.getScreeningTime().format(DB_FORMATTER));
            stmt.setDouble(4, screening.getStandardSeatPrice());
            stmt.setDouble(5, screening.getDeluxeSeatPrice());
            stmt.setInt(6, screening.isActive() ? 1 : 0);
            stmt.setInt(7, screening.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a screening from the database.
     *
     * @param id The ID of the screening to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteScreening(int id) throws SQLException {
        // First, delete associated seats
        String deleteSeatsQuery = "DELETE FROM seats WHERE screening_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement deleteSeatsStmt = conn.prepareStatement(deleteSeatsQuery)) {
            
            deleteSeatsStmt.setInt(1, id);
            deleteSeatsStmt.executeUpdate();
            
            // Then delete the screening
            String deleteScreeningQuery = "DELETE FROM screenings WHERE id = ?";
            try (PreparedStatement deleteScreeningStmt = conn.prepareStatement(deleteScreeningQuery)) {
                deleteScreeningStmt.setInt(1, id);
                int affectedRows = deleteScreeningStmt.executeUpdate();
                return affectedRows > 0;
            }
        }
    }
    
    /**
     * Retrieves all screenings from the database.
     *
     * @return A list of all screenings
     * @throws SQLException If a database error occurs
     */
    public List<Screening> getAllScreenings() throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "ORDER BY s.screening_time";
        
        return getScreeningsWithQuery(query);
    }
    
    /**
     * Retrieves all active screenings for a specific date.
     *
     * @param date The date to filter screenings by (format: YYYY-MM-DD)
     * @return A list of screenings on the specified date
     * @throws SQLException If a database error occurs
     */
    public List<Screening> getScreeningsByDate(String date) throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE s.is_active = 1 AND m.is_active = 1 AND c.is_active = 1 " +
                      "AND date(s.screening_time) = ? " +
                      "ORDER BY s.screening_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, date);
            
            List<Screening> screenings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    screenings.add(extractScreeningFromResultSet(rs, true));
                }
            }
            
            return screenings;
        }
    }
    
    /**
     * Retrieves all active screenings for a specific movie.
     *
     * @param movieId The ID of the movie
     * @return A list of screenings for the specified movie
     * @throws SQLException If a database error occurs
     */
    public List<Screening> getScreeningsByMovie(int movieId) throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE s.is_active = 1 AND m.is_active = 1 AND c.is_active = 1 " +
                      "AND s.movie_id = ? " +
                      "ORDER BY s.screening_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, movieId);
            
            List<Screening> screenings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    screenings.add(extractScreeningFromResultSet(rs, true));
                }
            }
            
            return screenings;
        }
    }
    
    /**
     * Retrieves all active screenings for a specific cinema.
     *
     * @param cinemaId The ID of the cinema
     * @return A list of screenings for the specified cinema
     * @throws SQLException If a database error occurs
     */
    public List<Screening> getScreeningsByCinema(int cinemaId) throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE s.is_active = 1 AND m.is_active = 1 AND c.is_active = 1 " +
                      "AND s.cinema_id = ? " +
                      "ORDER BY s.screening_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, cinemaId);
            
            List<Screening> screenings = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    screenings.add(extractScreeningFromResultSet(rs, true));
                }
            }
            
            return screenings;
        }
    }
    
    /**
     * Retrieves all future active screenings.
     *
     * @return A list of future screenings
     * @throws SQLException If a database error occurs
     */
    public List<Screening> getFutureScreenings() throws SQLException {
        String query = "SELECT s.*, m.title as movie_title, c.name as cinema_name " +
                      "FROM screenings s " +
                      "JOIN movies m ON s.movie_id = m.id " +
                      "JOIN cinemas c ON s.cinema_id = c.id " +
                      "WHERE s.is_active = 1 AND m.is_active = 1 AND c.is_active = 1 " +
                      "AND s.screening_time > datetime('now', 'localtime') " +
                      "ORDER BY s.screening_time";
        
        return getScreeningsWithQuery(query);
    }
    
    /**
     * Helper method to execute a query and return a list of screenings.
     *
     * @param query The SQL query to execute
     * @return A list of screenings from the query results
     * @throws SQLException If a database error occurs
     */
    private List<Screening> getScreeningsWithQuery(String query) throws SQLException {
        List<Screening> screenings = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                screenings.add(extractScreeningFromResultSet(rs, true));
            }
        }
        
        return screenings;
    }
    
    /**
     * Helper method to extract a Screening object from a ResultSet.
     *
     * @param rs The ResultSet containing screening data
     * @param withJoinData Whether the ResultSet includes joined data like movie_title
     * @return A Screening object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Screening extractScreeningFromResultSet(ResultSet rs, boolean withJoinData) throws SQLException {
        Screening screening;
        
        LocalDateTime screeningTime = LocalDateTime.parse(
            rs.getString("screening_time"), DB_FORMATTER);
        
        if (withJoinData) {
            screening = new Screening(
                rs.getInt("id"),
                rs.getInt("movie_id"),
                rs.getString("movie_title"),
                rs.getInt("cinema_id"),
                rs.getString("cinema_name"),
                screeningTime,
                rs.getDouble("standard_seat_price"),
                rs.getDouble("deluxe_seat_price"),
                rs.getInt("is_active") == 1
            );
        } else {
            screening = new Screening(
                rs.getInt("id"),
                rs.getInt("movie_id"),
                rs.getInt("cinema_id"),
                screeningTime,
                rs.getDouble("standard_seat_price"),
                rs.getDouble("deluxe_seat_price"),
                rs.getInt("is_active") == 1
            );
        }
        
        return screening;
    }
}
