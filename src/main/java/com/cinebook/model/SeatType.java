package com.cinebook.model;

/**
 * Enum representing different types of seats available in cinemas.
 */
public enum SeatType {
    STANDARD("Standard"),
    DELUXE("Deluxe");
    
    private final String displayName;
    
    SeatType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
