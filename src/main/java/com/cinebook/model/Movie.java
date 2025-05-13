package com.cinebook.model;

/**
 * Represents a movie in the CineBook CDO system.
 */
public class Movie {
    private int id;
    private String title;
    private String director;
    private String cast;
    private String genre;
    private String synopsis;
    private int durationMinutes;
    private String rating; // e.g., G, PG, PG-13, R
    private String releaseDate;
    private String posterUrl;
    private boolean isActive;
    
    // Default constructor
    public Movie() {
    }
    
    // Constructor for creating a new movie
    public Movie(String title, String director, String cast, String genre, String synopsis, 
                int durationMinutes, String rating, String releaseDate, String posterUrl, boolean isActive) {
        this.title = title;
        this.director = director;
        this.cast = cast;
        this.genre = genre;
        this.synopsis = synopsis;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.isActive = isActive;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Movie(int id, String title, String director, String cast, String genre, String synopsis, 
                int durationMinutes, String rating, String releaseDate, String posterUrl, boolean isActive) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.cast = cast;
        this.genre = genre;
        this.synopsis = synopsis;
        this.durationMinutes = durationMinutes;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }
    
    public String getCast() {
        return cast;
    }
    
    public void setCast(String cast) {
        this.cast = cast;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getSynopsis() {
        return synopsis;
    }
    
    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }
    
    public int getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public String getRating() {
        return rating;
    }
    
    public void setRating(String rating) {
        this.rating = rating;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getPosterUrl() {
        return posterUrl;
    }
    
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    // Returns formatted duration string (e.g., "2h 15m")
    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return hours + "h " + minutes + "m";
    }
    
    @Override
    public String toString() {
        return title;
    }
}
