package com.cinebook.dao;

import com.cinebook.model.Payment;
import com.cinebook.model.PaymentMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Payment operations.
 * Handles CRUD operations for payments in the database.
 */
public class PaymentDAO {
    
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Processes a payment and updates the reservation's payment status.
     *
     * @param payment The Payment object to process
     * @return The ID of the newly created payment, or -1 if creation failed
     * @throws SQLException If a database error occurs
     */
    public int processPayment(Payment payment) throws SQLException {
        String query = "INSERT INTO payments (reservation_id, amount, payment_method, " +
                      "transaction_reference, payment_time, is_successful) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setInt(1, payment.getReservationId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentMethod().toString());
            stmt.setString(4, payment.getTransactionReference());
            stmt.setString(5, payment.getPaymentTime().format(DB_FORMATTER));
            stmt.setInt(6, payment.isSuccessful() ? 1 : 0);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return -1; // No rows affected, insertion failed
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                conn.rollback();
                return -1; // No ID generated, insertion failed
            }
            
            int paymentId = generatedKeys.getInt(1);
            
            // If payment was successful, update the reservation's payment status
            if (payment.isSuccessful()) {
                String updateReservationQuery = "UPDATE reservations SET is_paid = 1 WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateReservationQuery)) {
                    updateStmt.setInt(1, payment.getReservationId());
                    updateStmt.executeUpdate();
                }
            }
            
            conn.commit();
            return paymentId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
                // Don't close the connection here, as it's managed by DBConnection
            }
        }
    }
    
    /**
     * Retrieves a payment by its ID.
     *
     * @param id The ID of the payment to retrieve
     * @return The Payment object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Payment getPaymentById(int id) throws SQLException {
        String query = "SELECT p.*, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM payments p " +
                      "JOIN reservations r ON p.reservation_id = r.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE p.id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
            }
        }
        
        return null; // Payment not found
    }
    
    /**
     * Retrieves a payment by reservation ID.
     *
     * @param reservationId The ID of the reservation
     * @return The Payment object if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Payment getPaymentByReservation(int reservationId) throws SQLException {
        String query = "SELECT p.*, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM payments p " +
                      "JOIN reservations r ON p.reservation_id = r.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE p.reservation_id = ? " +
                      "ORDER BY p.payment_time DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
            }
        }
        
        return null; // Payment not found
    }
    
    /**
     * Retrieves all payments for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return A list of payments within the date range
     * @throws SQLException If a database error occurs
     */
    public List<Payment> getPaymentsByDateRange(String startDate, String endDate) throws SQLException {
        String query = "SELECT p.*, " +
                      "CASE WHEN r.user_id IS NULL THEN r.guest_name ELSE u.full_name END as customer_name " +
                      "FROM payments p " +
                      "JOIN reservations r ON p.reservation_id = r.id " +
                      "LEFT JOIN users u ON r.user_id = u.id " +
                      "WHERE date(p.payment_time) BETWEEN ? AND ? " +
                      "AND p.is_successful = 1 " +
                      "ORDER BY p.payment_time";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            List<Payment> payments = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
            
            return payments;
        }
    }
    
    /**
     * Calculates total sales for a date range.
     *
     * @param startDate The start date (format: YYYY-MM-DD)
     * @param endDate The end date (format: YYYY-MM-DD)
     * @return The total sales amount
     * @throws SQLException If a database error occurs
     */
    public double calculateTotalSales(String startDate, String endDate) throws SQLException {
        String query = "SELECT SUM(amount) as total_sales FROM payments " +
                      "WHERE date(payment_time) BETWEEN ? AND ? " +
                      "AND is_successful = 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_sales");
                }
            }
        }
        
        return 0.0; // No sales or error
    }
    
    /**
     * Helper method to extract a Payment object from a ResultSet.
     *
     * @param rs The ResultSet containing payment data
     * @return A Payment object populated with data from the ResultSet
     * @throws SQLException If a database error occurs
     */
    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        
        payment.setId(rs.getInt("id"));
        payment.setReservationId(rs.getInt("reservation_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        payment.setTransactionReference(rs.getString("transaction_reference"));
        
        LocalDateTime paymentTime = LocalDateTime.parse(
            rs.getString("payment_time"), DB_FORMATTER);
        payment.setPaymentTime(paymentTime);
        
        payment.setSuccessful(rs.getInt("is_successful") == 1);
        
        // Set additional display fields if available
        if (rs.getMetaData().getColumnCount() > 7) { // If customer_name column exists
            payment.setCustomerName(rs.getString("customer_name"));
        }
        
        return payment;
    }
}
