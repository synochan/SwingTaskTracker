package com.cinebook.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a movie screening in the CineBook CDO system.
 * A screening is a specific showing of a movie at a specific time in a specific cinema.
 */
public class Screening {
    private int id;
    private int movieId;
    private int cinemaId;
    private LocalDateTime screeningTime;
    private double standardSeatPrice;
    private double deluxeSeatPrice;
    private boolean isActive;
    
    // Additional fields for display purposes
    private String movieTitle;
    private String cinemaName;
    
    // Date formatter for consistent date/time display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Default constructor
    public Screening() {
    }
    
    // Constructor for creating a new screening
    public Screening(int movieId, int cinemaId, LocalDateTime screeningTime, 
                     double standardSeatPrice, double deluxeSeatPrice, boolean isActive) {
        this.movieId = movieId;
        this.cinemaId = cinemaId;
        this.screeningTime = screeningTime;
        this.standardSeatPrice = standardSeatPrice;
        this.deluxeSeatPrice = deluxeSeatPrice;
        this.isActive = isActive;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Screening(int id, int movieId, int cinemaId, LocalDateTime screeningTime, 
                    double standardSeatPrice, double deluxeSeatPrice, boolean isActive) {
        this.id = id;
        this.movieId = movieId;
        this.cinemaId = cinemaId;
        this.screeningTime = screeningTime;
        this.standardSeatPrice = standardSeatPrice;
        this.deluxeSeatPrice = deluxeSeatPrice;
        this.isActive = isActive;
    }
    
    // Constructor with additional display fields
    public Screening(int id, int movieId, String movieTitle, int cinemaId, String cinemaName, 
                    LocalDateTime screeningTime, double standardSeatPrice, double deluxeSeatPrice, boolean isActive) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.screeningTime = screeningTime;
        this.standardSeatPrice = standardSeatPrice;
        this.deluxeSeatPrice = deluxeSeatPrice;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getMovieId() {
        return movieId;
    }
    
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
    
    public int getCinemaId() {
        return cinemaId;
    }
    
    public void setCinemaId(int cinemaId) {
        this.cinemaId = cinemaId;
    }
    
    public LocalDateTime getScreeningTime() {
        return screeningTime;
    }
    
    public void setScreeningTime(LocalDateTime screeningTime) {
        this.screeningTime = screeningTime;
    }
    
    public double getStandardSeatPrice() {
        return standardSeatPrice;
    }
    
    public void setStandardSeatPrice(double standardSeatPrice) {
        this.standardSeatPrice = standardSeatPrice;
    }
    
    public double getDeluxeSeatPrice() {
        return deluxeSeatPrice;
    }
    
    public void setDeluxeSeatPrice(double deluxeSeatPrice) {
        this.deluxeSeatPrice = deluxeSeatPrice;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getMovieTitle() {
        return movieTitle;
    }
    
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
    
    public String getCinemaName() {
        return cinemaName;
    }
    
    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }
    
    // Helper methods for formatted date/time
    public String getFormattedDate() {
        return screeningTime.format(DATE_FORMATTER);
    }
    
    public String getFormattedTime() {
        return screeningTime.format(TIME_FORMATTER);
    }
    
    public String getFormattedDateTime() {
        return screeningTime.format(DATETIME_FORMATTER);
    }
    
    @Override
    public String toString() {
        return movieTitle + " - " + getFormattedDateTime() + " at " + cinemaName;
    }
}
