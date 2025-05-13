package com.cinebook.model;

/**
 * Represents a seat in a cinema for a specific screening.
 */
public class Seat {
    private int id;
    private int screeningId;
    private String seatNumber; // e.g., A1, B5, etc.
    private SeatType seatType;
    private boolean isReserved;
    private int rowNumber; // Numeric row (1, 2, 3...)
    private int columnNumber; // Numeric column (1, 2, 3...)
    
    // Default constructor
    public Seat() {
    }
    
    // Constructor for creating a new seat
    public Seat(int screeningId, String seatNumber, SeatType seatType, boolean isReserved, int rowNumber, int columnNumber) {
        this.screeningId = screeningId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.isReserved = isReserved;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Seat(int id, int screeningId, String seatNumber, SeatType seatType, boolean isReserved, int rowNumber, int columnNumber) {
        this.id = id;
        this.screeningId = screeningId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.isReserved = isReserved;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getScreeningId() {
        return screeningId;
    }
    
    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public SeatType getSeatType() {
        return seatType;
    }
    
    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }
    
    public boolean isReserved() {
        return isReserved;
    }
    
    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }
    
    public int getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    
    public int getColumnNumber() {
        return columnNumber;
    }
    
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
    
    // Calculate the price of this seat based on the screening prices
    public double getPrice(Screening screening) {
        if (seatType == SeatType.DELUXE) {
            return screening.getDeluxeSeatPrice();
        } else {
            return screening.getStandardSeatPrice();
        }
    }
    
    // Calculate the price of this seat based on a reservation
    public double getPrice(Reservation reservation) {
        // Default prices if we can't determine the actual price
        if (seatType == SeatType.DELUXE) {
            return 150.0; // Default deluxe price
        } else {
            return 100.0; // Default standard price
        }
    }
    
    @Override
    public String toString() {
        return seatNumber + " (" + seatType + ")";
    }
}
