package com.cinebook.model;

/**
 * Represents a cinema in the CineBook CDO system.
 * Each cinema has specific details and configuration.
 */
public class Cinema {
    private int id;
    private String name;
    private String location;
    private int totalSeats;
    private int totalRows;
    private int seatsPerRow;
    private boolean hasDeluxeSeats;
    private boolean isActive;
    
    // Default constructor
    public Cinema() {
    }
    
    // Constructor for creating a new cinema
    public Cinema(String name, String location, int totalSeats, int totalRows, 
                 int seatsPerRow, boolean hasDeluxeSeats, boolean isActive) {
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.hasDeluxeSeats = hasDeluxeSeats;
        this.isActive = isActive;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Cinema(int id, String name, String location, int totalSeats, int totalRows, 
                 int seatsPerRow, boolean hasDeluxeSeats, boolean isActive) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.totalSeats = totalSeats;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.hasDeluxeSeats = hasDeluxeSeats;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public int getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public int getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
    
    public int getSeatsPerRow() {
        return seatsPerRow;
    }
    
    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }
    
    public boolean hasDeluxeSeats() {
        return hasDeluxeSeats;
    }
    
    public void setHasDeluxeSeats(boolean hasDeluxeSeats) {
        this.hasDeluxeSeats = hasDeluxeSeats;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return name + " (" + location + ")";
    }
}
