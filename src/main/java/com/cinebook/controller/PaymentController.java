package com.cinebook.controller;

import com.cinebook.dao.PaymentDAO;
import com.cinebook.dao.ReservationDAO;
import com.cinebook.model.Payment;
import com.cinebook.model.PaymentMethod;
import com.cinebook.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for payment-related operations.
 * Handles business logic for payment processing.
 */
public class PaymentController {
    private PaymentDAO paymentDAO;
    private ReservationDAO reservationDAO;
    
    /**
     * Constructor for PaymentController.
     */
    public PaymentController() {
        this.paymentDAO = new PaymentDAO();
        this.reservationDAO = new ReservationDAO();
    }
    
    /**
     * Processes a payment for a reservation.
     *
     * @param reservationId The ID of the reservation
     * @param paymentMethod The payment method
     * @return The ID of the newly created payment, or -1 if processing failed
     */
    public int processPayment(int reservationId, PaymentMethod paymentMethod) {
        try {
            // Get the reservation
            Reservation reservation = reservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                return -1;
            }
            
            // Create a payment object
            Payment payment = new Payment(reservationId, reservation.getTotalAmount(), paymentMethod);
            payment.setPaymentTime(LocalDateTime.now());
            payment.generateTransactionReference();
            
            // Simulate payment processing - in a real system, we would integrate with a payment gateway
            // For demonstration purposes, we'll always mark the payment as successful
            payment.setSuccessful(true);
            
            // Save the payment to the database
            return paymentDAO.processPayment(payment);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Retrieves a payment by its ID.
     *
     * @param id The ID of the payment to retrieve
     * @return The Payment object if found, null otherwise
     */
    public Payment getPaymentById(int id) {
        try {
            return paymentDAO.getPaymentById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves a payment by reservation ID.
     *
     * @param reservationId The ID of the reservation
     * @return The Payment object if found, null otherwise
     */
    public Payment getPaymentByReservation(int reservationId) {
        try {
            return paymentDAO.getPaymentByReservation(reservationId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all payments for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A list of payments within the date range
     */
    public List<Payment> getPaymentsByDateRange(String startDate, String endDate) {
        try {
            return paymentDAO.getPaymentsByDateRange(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculates total sales for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return The total sales amount
     */
    public double calculateTotalSales(String startDate, String endDate) {
        try {
            return paymentDAO.calculateTotalSales(startDate, endDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Checks if a reservation has been paid.
     *
     * @param reservationId The ID of the reservation
     * @return true if the reservation has been paid, false otherwise
     */
    public boolean isReservationPaid(int reservationId) {
        try {
            Reservation reservation = reservationDAO.getReservationById(reservationId);
            return reservation != null && reservation.isPaid();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all available payment methods.
     *
     * @return An array of all payment methods
     */
    public PaymentMethod[] getAvailablePaymentMethods() {
        return PaymentMethod.values();
    }
}
