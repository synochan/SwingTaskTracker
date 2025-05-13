package com.cinebook.dao;

import com.cinebook.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Movie operations.
 * Handles CRUD operations for movies in the database.
 */
public class MovieDAO {
    
    /**
     * Adds a new movie to the database.
     *
     * @param movie The Movie object to add
     * @return The ID of the newly created movie, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int addMovie(Movie movie) throws SQLException {
        String query = "INSERT INTO movies (title, director, cast, genre, synopsis, " +
                      "duration_minutes, rating, release_date, poster_url, is_active) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setString(3, movie.getCast());
            stmt.setString(4, movie.getGenre());
            stmt.setString(5, movie.getSynopsis());
            stmt.setInt(6, movie.getDurationMinutes());
            stmt.setString(7, movie.getRating());
            stmt.setString(8, movie.getReleaseDate());
            stmt.setString(9, movie.getPosterUrl());
            stmt.setInt(10, movie.isActive() ? 1 : 0);
            
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
     * Retrieves a movie by its ID.
     *
     * @param id The ID of the movie to retrieve
     * @return The Movie object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Movie getMovieById(int id) throws SQLException {
        String query = "SELECT * FROM movies WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMovieFromResultSet(rs);
                }
            }
        }
        
        return null; // Movie not found
    }
    
    /**
     * Updates an existing movie in the database.
     *
     * @param movie The Movie object with updated information
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateMovie(Movie movie) throws SQLException {
        String query = "UPDATE movies SET title = ?, director = ?, cast = ?, genre = ?, " +
                      "synopsis = ?, duration_minutes = ?, rating = ?, release_date = ?, " +
                      "poster_url = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getDirector());
            stmt.setString(3, movie.getCast());
            stmt.setString(4, movie.getGenre());
            stmt.setString(5, movie.getSynopsis());
            stmt.setInt(6, movie.getDurationMinutes());
            stmt.setString(7, movie.getRating());
            stmt.setString(8, movie.getReleaseDate());
            stmt.setString(9, movie.getPosterUrl());
            stmt.setInt(10, movie.isActive() ? 1 : 0);
            stmt.setInt(11, movie.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Deletes a movie from the database.
     *
     * @param id The ID of the movie to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deleteMovie(int id) throws SQLException {
        String query = "DELETE FROM movies WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Retrieves all movies from the database.
     *
     * @return A list of all movies
     * @throws SQLException If a database error occurs
     */
    public List<Movie> getAllMovies() throws SQLException {
        String query = "SELECT * FROM movies ORDER BY title";
        List<Movie> movies = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        }
        
        return movies;
    }
    
    /**
     * Retrieves all active movies from the database.
     *
     * @return A list of all active movies
     * @throws SQLException If a database error occurs
     */
    public List<Movie> getAllActiveMovies() throws SQLException {
        String query = "SELECT * FROM movies WHERE is_active = 1 ORDER BY title";
        List<Movie> movies = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        }
        
        return movies;
    }
    
    /**
     * Helper method to extract a Movie object from a ResultSet.
     *
     * @param rs The ResultSet containing movie data
     * @return A Movie object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setDirector(rs.getString("director"));
        movie.setCast(rs.getString("cast"));
        movie.setGenre(rs.getString("genre"));
        movie.setSynopsis(rs.getString("synopsis"));
        movie.setDurationMinutes(rs.getInt("duration_minutes"));
        movie.setRating(rs.getString("rating"));
        movie.setReleaseDate(rs.getString("release_date"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setActive(rs.getInt("is_active") == 1);
        return movie;
    }
}
