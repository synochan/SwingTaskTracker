package com.cinebook.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a ticket in the CineBook CDO system.
 * A ticket is generated after a successful reservation and payment.
 */
public class Ticket {
    private int id;
    private int reservationId;
    private int seatId;
    private String ticketCode;
    private boolean isUsed;
    private LocalDateTime generationTime;
    
    // Additional fields for display purposes
    private String movieTitle;
    private String cinemaName;
    private LocalDateTime screeningTime;
    private String seatNumber;
    private SeatType seatType;
    private String customerName;
    
    // Date formatter for consistent date/time display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Default constructor
    public Ticket() {
        this.generationTime = LocalDateTime.now();
    }
    
    // Constructor for creating a new ticket
    public Ticket(int reservationId, int seatId) {
        this.reservationId = reservationId;
        this.seatId = seatId;
        this.isUsed = false;
        this.generationTime = LocalDateTime.now();
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Ticket(int id, int reservationId, int seatId, String ticketCode, boolean isUsed, LocalDateTime generationTime) {
        this.id = id;
        this.reservationId = reservationId;
        this.seatId = seatId;
        this.ticketCode = ticketCode;
        this.isUsed = isUsed;
        this.generationTime = generationTime;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }
    
    public int getSeatId() {
        return seatId;
    }
    
    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
    
    public String getTicketCode() {
        return ticketCode;
    }
    
    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
    
    public boolean isUsed() {
        return isUsed;
    }
    
    public void setUsed(boolean used) {
        isUsed = used;
    }
    
    public LocalDateTime getGenerationTime() {
        return generationTime;
    }
    
    public void setGenerationTime(LocalDateTime generationTime) {
        this.generationTime = generationTime;
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
    
    public LocalDateTime getScreeningTime() {
        return screeningTime;
    }
    
    public void setScreeningTime(LocalDateTime screeningTime) {
        this.screeningTime = screeningTime;
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
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    // Helper method to generate a random ticket code
    public void generateTicketCode() {
        // Format: TICK-yyyyMMdd-RandomDigits
        String prefix = "TICK";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomDigits = String.format("%06d", (int) (Math.random() * 1000000));
        this.ticketCode = prefix + "-" + dateStr + "-" + randomDigits;
    }
    
    // Format the screening date
    public String getFormattedScreeningDate() {
        return screeningTime != null ? screeningTime.format(DATE_FORMATTER) : "";
    }
    
    // Format the screening time
    public String getFormattedScreeningTime() {
        return screeningTime != null ? screeningTime.format(TIME_FORMATTER) : "";
    }
    
    // Format the generation time
    public String getFormattedGenerationTime() {
        return generationTime.format(DATETIME_FORMATTER);
    }
    
    @Override
    public String toString() {
        return "Ticket #" + id + " - " + movieTitle + " - " + seatNumber + " - " + getFormattedScreeningDate();
    }
}
