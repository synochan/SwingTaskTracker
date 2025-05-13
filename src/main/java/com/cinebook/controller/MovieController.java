package com.cinebook.controller;

import com.cinebook.dao.MovieDAO;
import com.cinebook.dao.ScreeningDAO;
import com.cinebook.model.Movie;
import com.cinebook.model.Screening;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for movie-related operations.
 * Handles business logic for movie management.
 */
public class MovieController {
    private MovieDAO movieDAO;
    private ScreeningDAO screeningDAO;
    
    /**
     * Constructor for MovieController.
     */
    public MovieController() {
        this.movieDAO = new MovieDAO();
        this.screeningDAO = new ScreeningDAO();
    }
    
    /**
     * Adds a new movie to the database.
     *
     * @param movie The Movie object to add
     * @return The ID of the newly created movie, or -1 if creation failed
     */
    public int addMovie(Movie movie) {
        try {
            return movieDAO.addMovie(movie);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Updates an existing movie in the database.
     *
     * @param movie The Movie object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateMovie(Movie movie) {
        try {
            return movieDAO.updateMovie(movie);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a movie from the database.
     *
     * @param id The ID of the movie to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteMovie(int id) {
        try {
            return movieDAO.deleteMovie(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves a movie by its ID.
     *
     * @param id The ID of the movie to retrieve
     * @return The Movie object if found, null otherwise
     */
    public Movie getMovieById(int id) {
        try {
            return movieDAO.getMovieById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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
     * Retrieves all screenings for a specific movie.
     *
     * @param movieId The ID of the movie
     * @return A list of screenings for the movie
     */
    public List<Screening> getScreeningsByMovie(int movieId) {
        try {
            return screeningDAO.getScreeningsByMovie(movieId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Toggles the active status of a movie.
     *
     * @param movieId The ID of the movie
     * @return true if the update was successful, false otherwise
     */
    public boolean toggleMovieStatus(int movieId) {
        try {
            Movie movie = movieDAO.getMovieById(movieId);
            if (movie != null) {
                movie.setActive(!movie.isActive());
                return movieDAO.updateMovie(movie);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Searches for movies by title.
     *
     * @param searchTerm The search term
     * @return A list of movies matching the search term
     */
    public List<Movie> searchMoviesByTitle(String searchTerm) {
        try {
            List<Movie> allMovies = movieDAO.getAllActiveMovies();
            List<Movie> matchingMovies = new ArrayList<>();
            
            for (Movie movie : allMovies) {
                if (movie.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                    matchingMovies.add(movie);
                }
            }
            
            return matchingMovies;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Searches for movies by genre.
     *
     * @param genre The genre to search for
     * @return A list of movies in the specified genre
     */
    public List<Movie> searchMoviesByGenre(String genre) {
        try {
            List<Movie> allMovies = movieDAO.getAllActiveMovies();
            List<Movie> matchingMovies = new ArrayList<>();
            
            for (Movie movie : allMovies) {
                if (movie.getGenre().toLowerCase().contains(genre.toLowerCase())) {
                    matchingMovies.add(movie);
                }
            }
            
            return matchingMovies;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
