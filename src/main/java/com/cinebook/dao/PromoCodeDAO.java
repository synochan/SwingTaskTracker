package com.cinebook.dao;

import com.cinebook.model.PromoCode;
import com.cinebook.model.PromoCode.DiscountType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for promo code operations.
 */
public class PromoCodeDAO {
    private Connection connection;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Constructor for PromoCodeDAO.
     */
    public PromoCodeDAO() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }
    
    /**
     * Get all promo codes.
     *
     * @return A list of all promo codes
     * @throws SQLException If a database error occurs
     */
    public List<PromoCode> getAllPromoCodes() throws SQLException {
        List<PromoCode> promoCodes = new ArrayList<>();
        String query = "SELECT * FROM promo_codes ORDER BY valid_until DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                promoCodes.add(createPromoCodeFromResultSet(rs));
            }
        }
        
        return promoCodes;
    }
    
    /**
     * Get all active promo codes.
     *
     * @return A list of all active promo codes
     * @throws SQLException If a database error occurs
     */
    public List<PromoCode> getActivePromoCodes() throws SQLException {
        List<PromoCode> promoCodes = new ArrayList<>();
        String query = "SELECT * FROM promo_codes WHERE is_active = 1 " +
                       "AND valid_from <= date('now') AND valid_until >= date('now') " +
                       "AND (max_uses IS NULL OR current_uses < max_uses) " +
                       "ORDER BY valid_until ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                promoCodes.add(createPromoCodeFromResultSet(rs));
            }
        }
        
        return promoCodes;
    }
    
    /**
     * Get a promo code by ID.
     *
     * @param id The ID of the promo code
     * @return The promo code, or null if not found
     * @throws SQLException If a database error occurs
     */
    public PromoCode getPromoCodeById(int id) throws SQLException {
        String query = "SELECT * FROM promo_codes WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createPromoCodeFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a promo code by code.
     *
     * @param code The code of the promo code
     * @return The promo code, or null if not found
     * @throws SQLException If a database error occurs
     */
    public PromoCode getPromoCodeByCode(String code) throws SQLException {
        String query = "SELECT * FROM promo_codes WHERE code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createPromoCodeFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Add a new promo code.
     *
     * @param promoCode The promo code to add
     * @return The ID of the new promo code
     * @throws SQLException If a database error occurs
     */
    public int addPromoCode(PromoCode promoCode) throws SQLException {
        String query = "INSERT INTO promo_codes (code, description, discount_type, discount_amount, " +
                       "valid_from, valid_until, max_uses, current_uses, min_purchase_amount, is_active) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, promoCode.getCode());
            pstmt.setString(2, promoCode.getDescription());
            pstmt.setString(3, promoCode.getDiscountType().name());
            pstmt.setDouble(4, promoCode.getDiscountAmount());
            pstmt.setString(5, promoCode.getValidFrom().format(DATE_FORMATTER));
            pstmt.setString(6, promoCode.getValidUntil().format(DATE_FORMATTER));
            
            if (promoCode.getMaxUses() != null) {
                pstmt.setInt(7, promoCode.getMaxUses());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(8, promoCode.getCurrentUses());
            pstmt.setDouble(9, promoCode.getMinPurchaseAmount());
            pstmt.setInt(10, promoCode.isActive() ? 1 : 0);
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating promo code failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Update an existing promo code.
     *
     * @param promoCode The promo code to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updatePromoCode(PromoCode promoCode) throws SQLException {
        String query = "UPDATE promo_codes SET code = ?, description = ?, discount_type = ?, " +
                       "discount_amount = ?, valid_from = ?, valid_until = ?, max_uses = ?, " +
                       "current_uses = ?, min_purchase_amount = ?, is_active = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, promoCode.getCode());
            pstmt.setString(2, promoCode.getDescription());
            pstmt.setString(3, promoCode.getDiscountType().name());
            pstmt.setDouble(4, promoCode.getDiscountAmount());
            pstmt.setString(5, promoCode.getValidFrom().format(DATE_FORMATTER));
            pstmt.setString(6, promoCode.getValidUntil().format(DATE_FORMATTER));
            
            if (promoCode.getMaxUses() != null) {
                pstmt.setInt(7, promoCode.getMaxUses());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(8, promoCode.getCurrentUses());
            pstmt.setDouble(9, promoCode.getMinPurchaseAmount());
            pstmt.setInt(10, promoCode.isActive() ? 1 : 0);
            pstmt.setInt(11, promoCode.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Delete a promo code.
     *
     * @param id The ID of the promo code to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean deletePromoCode(int id) throws SQLException {
        String query = "DELETE FROM promo_codes WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Update the usage count of a promo code.
     *
     * @param code The code of the promo code
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean incrementPromoCodeUsage(String code) throws SQLException {
        String query = "UPDATE promo_codes SET current_uses = current_uses + 1 WHERE code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Check if a promo code is valid.
     *
     * @param code The code to check
     * @param purchaseAmount The purchase amount
     * @return The PromoCode object if valid, null otherwise
     * @throws SQLException If a database error occurs
     */
    public PromoCode validatePromoCode(String code, double purchaseAmount) throws SQLException {
        PromoCode promoCode = getPromoCodeByCode(code);
        
        if (promoCode == null) {
            return null; // Code doesn't exist
        }
        
        // Check if code is valid (active, within date range, not exceeded max uses)
        if (!promoCode.isValid()) {
            return null;
        }
        
        // Check minimum purchase amount
        if (purchaseAmount < promoCode.getMinPurchaseAmount()) {
            return null;
        }
        
        return promoCode;
    }
    
    /**
     * Helper method to create a PromoCode object from a ResultSet.
     *
     * @param rs The ResultSet
     * @return The PromoCode object
     * @throws SQLException If a database error occurs
     */
    private PromoCode createPromoCodeFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String code = rs.getString("code");
        String description = rs.getString("description");
        DiscountType discountType = DiscountType.valueOf(rs.getString("discount_type"));
        double discountAmount = rs.getDouble("discount_amount");
        
        LocalDate validFrom = LocalDate.parse(rs.getString("valid_from"), DATE_FORMATTER);
        LocalDate validUntil = LocalDate.parse(rs.getString("valid_until"), DATE_FORMATTER);
        
        Integer maxUses = rs.getInt("max_uses");
        if (rs.wasNull()) {
            maxUses = null;
        }
        
        int currentUses = rs.getInt("current_uses");
        double minPurchaseAmount = rs.getDouble("min_purchase_amount");
        boolean isActive = rs.getInt("is_active") == 1;
        
        return new PromoCode(id, code, description, discountType, discountAmount, 
                            validFrom, validUntil, maxUses, currentUses, 
                            minPurchaseAmount, isActive);
    }
}