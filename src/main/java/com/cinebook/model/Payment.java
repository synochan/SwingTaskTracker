package com.cinebook.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a payment transaction in the CineBook CDO system.
 */
public class Payment {
    private int id;
    private int reservationId;
    private double amount;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private LocalDateTime paymentTime;
    private boolean isSuccessful;
    
    // Additional fields for display purposes
    private String customerName;
    
    // Date formatter for consistent date/time display
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Default constructor
    public Payment() {
        this.paymentTime = LocalDateTime.now();
    }
    
    // Constructor for creating a new payment
    public Payment(int reservationId, double amount, PaymentMethod paymentMethod) {
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentTime = LocalDateTime.now();
        this.isSuccessful = false;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Payment(int id, int reservationId, double amount, PaymentMethod paymentMethod, 
                  String transactionReference, LocalDateTime paymentTime, boolean isSuccessful) {
        this.id = id;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.paymentTime = paymentTime;
        this.isSuccessful = isSuccessful;
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
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public boolean isSuccessful() {
        return isSuccessful;
    }
    
    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    // Helper method to generate a random transaction reference
    public void generateTransactionReference() {
        // Format: CBCDO-yyyyMMdd-RandomDigits
        String prefix = "CBCDO";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomDigits = String.format("%06d", (int) (Math.random() * 1000000));
        this.transactionReference = prefix + "-" + dateStr + "-" + randomDigits;
    }
    
    // Format the payment time
    public String getFormattedPaymentTime() {
        return paymentTime.format(DATETIME_FORMATTER);
    }
    
    // Get the formatted amount with currency symbol
    public String getFormattedAmount() {
        return String.format("₱%.2f", amount);
    }
    
    @Override
    public String toString() {
        return "Payment #" + id + " - " + getFormattedAmount() + " via " + paymentMethod + 
                " on " + getFormattedPaymentTime() + (isSuccessful ? " (Successful)" : " (Failed)");
    }
}
