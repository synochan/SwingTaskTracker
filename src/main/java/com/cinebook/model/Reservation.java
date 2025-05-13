package com.cinebook.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reservation in the CineBook CDO system.
 * A reservation includes booking information for seats and potentially concessions.
 */
public class Reservation {
    private int id;
    private int userId; // Null for guest bookings
    private String guestName; // Only used for guest bookings
    private String guestEmail; // Only used for guest bookings
    private String guestPhone; // Only used for guest bookings
    private int screeningId;
    private LocalDateTime reservationTime;
    private double totalAmount;
    private boolean isPaid;
    private List<Seat> selectedSeats;
    private List<Concession> selectedConcessions;
    
    // Additional fields for display purposes
    private String movieTitle;
    private String cinemaName;
    private LocalDateTime screeningTime;
    
    // Date formatter for consistent date/time display
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Default constructor
    public Reservation() {
        this.selectedSeats = new ArrayList<>();
        this.selectedConcessions = new ArrayList<>();
        this.reservationTime = LocalDateTime.now();
    }
    
    // Constructor for registered user reservation
    public Reservation(int userId, int screeningId) {
        this.userId = userId;
        this.screeningId = screeningId;
        this.reservationTime = LocalDateTime.now();
        this.selectedSeats = new ArrayList<>();
        this.selectedConcessions = new ArrayList<>();
        this.isPaid = false;
    }
    
    // Constructor for guest reservation
    public Reservation(String guestName, String guestEmail, String guestPhone, int screeningId) {
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.screeningId = screeningId;
        this.reservationTime = LocalDateTime.now();
        this.selectedSeats = new ArrayList<>();
        this.selectedConcessions = new ArrayList<>();
        this.isPaid = false;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Reservation(int id, int userId, String guestName, String guestEmail, String guestPhone, 
                       int screeningId, LocalDateTime reservationTime, double totalAmount, boolean isPaid) {
        this.id = id;
        this.userId = userId;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.screeningId = screeningId;
        this.reservationTime = reservationTime;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.selectedSeats = new ArrayList<>();
        this.selectedConcessions = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getGuestName() {
        return guestName;
    }
    
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    
    public String getGuestEmail() {
        return guestEmail;
    }
    
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }
    
    public String getGuestPhone() {
        return guestPhone;
    }
    
    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }
    
    public int getScreeningId() {
        return screeningId;
    }
    
    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }
    
    public LocalDateTime getReservationTime() {
        return reservationTime;
    }
    
    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public boolean isPaid() {
        return isPaid;
    }
    
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
    
    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }
    
    public void setSelectedSeats(List<Seat> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }
    
    public void addSeat(Seat seat) {
        this.selectedSeats.add(seat);
    }
    
    public List<Concession> getSelectedConcessions() {
        return selectedConcessions;
    }
    
    public void setSelectedConcessions(List<Concession> selectedConcessions) {
        this.selectedConcessions = selectedConcessions;
    }
    
    public void addConcession(Concession concession) {
        this.selectedConcessions.add(concession);
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
    
    // Calculate the total price of all selected seats
    public double calculateSeatsTotal(Screening screening) {
        double total = 0;
        for (Seat seat : selectedSeats) {
            if (seat.getSeatType() == SeatType.DELUXE) {
                total += screening.getDeluxeSeatPrice();
            } else {
                total += screening.getStandardSeatPrice();
            }
        }
        return total;
    }
    
    // Calculate the total price of all selected concessions
    public double calculateConcessionsTotal() {
        double total = 0;
        for (Concession concession : selectedConcessions) {
            total += concession.getPrice() * concession.getQuantity();
        }
        return total;
    }
    
    // Calculate and update the total amount for the reservation
    public void calculateTotalAmount(Screening screening) {
        this.totalAmount = calculateSeatsTotal(screening) + calculateConcessionsTotal();
    }
    
    // Format the reservation time
    public String getFormattedReservationTime() {
        return reservationTime.format(DATETIME_FORMATTER);
    }
    
    // Format the screening time
    public String getFormattedScreeningTime() {
        return screeningTime != null ? screeningTime.format(DATETIME_FORMATTER) : "";
    }
    
    // Check if this is a guest reservation
    public boolean isGuestReservation() {
        return userId == 0 && guestName != null && !guestName.isEmpty();
    }
    
    // Get customer name (either registered user or guest)
    public String getCustomerName() {
        return isGuestReservation() ? guestName : "User #" + userId;
    }
    
    // Get customer email (either registered user or guest)
    public String getCustomerEmail() {
        return isGuestReservation() ? guestEmail : "";
    }
    
    // Get customer phone (either registered user or guest)
    public String getCustomerPhone() {
        return isGuestReservation() ? guestPhone : "";
    }
    
    @Override
    public String toString() {
        return "Reservation #" + id + " - " + getCustomerName() + " - " + getFormattedReservationTime();
    }
}
