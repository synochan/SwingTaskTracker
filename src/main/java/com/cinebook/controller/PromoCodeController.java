package com.cinebook.controller;

import com.cinebook.dao.PromoCodeDAO;
import com.cinebook.model.PromoCode;
import com.cinebook.model.PromoCode.DiscountType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for promo code-related operations.
 */
public class PromoCodeController {
    private PromoCodeDAO promoCodeDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Constructor for PromoCodeController.
     */
    public PromoCodeController() {
        promoCodeDAO = new PromoCodeDAO();
    }
    
    /**
     * Get all promo codes.
     *
     * @return A list of all promo codes
     */
    public List<PromoCode> getAllPromoCodes() {
        try {
            return promoCodeDAO.getAllPromoCodes();
        } catch (SQLException e) {
            System.err.println("Error getting all promo codes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get all active promo codes.
     *
     * @return A list of all active promo codes
     */
    public List<PromoCode> getActivePromoCodes() {
        try {
            return promoCodeDAO.getActivePromoCodes();
        } catch (SQLException e) {
            System.err.println("Error getting active promo codes: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get a promo code by ID.
     *
     * @param id The ID of the promo code
     * @return The promo code, or null if not found
     */
    public PromoCode getPromoCodeById(int id) {
        try {
            return promoCodeDAO.getPromoCodeById(id);
        } catch (SQLException e) {
            System.err.println("Error getting promo code by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get a promo code by code.
     *
     * @param code The code of the promo code
     * @return The promo code, or null if not found
     */
    public PromoCode getPromoCodeByCode(String code) {
        try {
            return promoCodeDAO.getPromoCodeByCode(code);
        } catch (SQLException e) {
            System.err.println("Error getting promo code by code: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Add a new promo code.
     *
     * @param code The promo code
     * @param description The description
     * @param discountType The discount type (PERCENTAGE or FIXED)
     * @param discountAmount The discount amount
     * @param validFrom The valid from date (yyyy-MM-dd)
     * @param validUntil The valid until date (yyyy-MM-dd)
     * @param maxUses The maximum number of uses (null for unlimited)
     * @param minPurchaseAmount The minimum purchase amount
     * @return The ID of the new promo code, or -1 if failed
     */
    public int addPromoCode(String code, String description, String discountType, 
                          double discountAmount, String validFrom, String validUntil,
                          String maxUses, double minPurchaseAmount) {
        
        // Validate input
        if (code == null || code.isEmpty() || description == null || description.isEmpty() ||
            discountType == null || discountAmount <= 0 || validFrom == null || validUntil == null) {
            return -1;
        }
        
        try {
            // Parse dates
            LocalDate fromDate = LocalDate.parse(validFrom, DATE_FORMATTER);
            LocalDate untilDate = LocalDate.parse(validUntil, DATE_FORMATTER);
            
            // Check date order
            if (fromDate.isAfter(untilDate)) {
                return -1;
            }
            
            // Parse discount type
            DiscountType type = DiscountType.valueOf(discountType);
            
            // Parse max uses
            Integer maxUsesInt = null;
            if (maxUses != null && !maxUses.isEmpty()) {
                try {
                    maxUsesInt = Integer.parseInt(maxUses);
                    if (maxUsesInt <= 0) {
                        maxUsesInt = null;
                    }
                } catch (NumberFormatException e) {
                    // Invalid number, keep as null
                }
            }
            
            // Create promo code object
            PromoCode promoCode = new PromoCode(code, description, type, discountAmount,
                                             fromDate, untilDate, maxUsesInt, minPurchaseAmount);
            
            // Add to database
            return promoCodeDAO.addPromoCode(promoCode);
            
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing dates: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid discount type: " + e.getMessage());
            return -1;
        } catch (SQLException e) {
            System.err.println("Error adding promo code: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Update an existing promo code.
     *
     * @param id The ID of the promo code to update
     * @param code The promo code
     * @param description The description
     * @param discountType The discount type (PERCENTAGE or FIXED)
     * @param discountAmount The discount amount
     * @param validFrom The valid from date (yyyy-MM-dd)
     * @param validUntil The valid until date (yyyy-MM-dd)
     * @param maxUses The maximum number of uses (null for unlimited)
     * @param currentUses The current number of uses
     * @param minPurchaseAmount The minimum purchase amount
     * @param isActive Whether the promo code is active
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePromoCode(int id, String code, String description, String discountType, 
                                double discountAmount, String validFrom, String validUntil,
                                String maxUses, int currentUses, double minPurchaseAmount, boolean isActive) {
        
        // Validate input
        if (code == null || code.isEmpty() || description == null || description.isEmpty() ||
            discountType == null || discountAmount <= 0 || validFrom == null || validUntil == null) {
            return false;
        }
        
        try {
            // Parse dates
            LocalDate fromDate = LocalDate.parse(validFrom, DATE_FORMATTER);
            LocalDate untilDate = LocalDate.parse(validUntil, DATE_FORMATTER);
            
            // Check date order
            if (fromDate.isAfter(untilDate)) {
                return false;
            }
            
            // Parse discount type
            DiscountType type = DiscountType.valueOf(discountType);
            
            // Parse max uses
            Integer maxUsesInt = null;
            if (maxUses != null && !maxUses.isEmpty()) {
                try {
                    maxUsesInt = Integer.parseInt(maxUses);
                    if (maxUsesInt <= 0) {
                        maxUsesInt = null;
                    }
                } catch (NumberFormatException e) {
                    // Invalid number, keep as null
                }
            }
            
            // Create promo code object
            PromoCode promoCode = new PromoCode(id, code, description, type, discountAmount,
                                             fromDate, untilDate, maxUsesInt, currentUses,
                                             minPurchaseAmount, isActive);
            
            // Update in database
            return promoCodeDAO.updatePromoCode(promoCode);
            
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing dates: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid discount type: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating promo code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a promo code.
     *
     * @param id The ID of the promo code to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deletePromoCode(int id) {
        try {
            return promoCodeDAO.deletePromoCode(id);
        } catch (SQLException e) {
            System.err.println("Error deleting promo code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate a promo code and calculate the discount.
     *
     * @param code The promo code to validate
     * @param purchaseAmount The purchase amount
     * @return The discount amount, or 0 if the code is invalid
     */
    public double applyPromoCode(String code, double purchaseAmount) {
        if (code == null || code.isEmpty()) {
            return 0.0;
        }
        
        try {
            PromoCode promoCode = promoCodeDAO.validatePromoCode(code, purchaseAmount);
            
            if (promoCode == null) {
                return 0.0;
            }
            
            return promoCode.calculateDiscount(purchaseAmount);
        } catch (SQLException e) {
            System.err.println("Error validating promo code: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Mark a promo code as used.
     *
     * @param code The promo code to mark as used
     * @return true if successful, false otherwise
     */
    public boolean usePromoCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        try {
            return promoCodeDAO.incrementPromoCodeUsage(code);
        } catch (SQLException e) {
            System.err.println("Error using promo code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a promo code is valid for a given purchase amount.
     *
     * @param code The promo code to check
     * @param purchaseAmount The purchase amount
     * @return A validation result message
     */
    public PromoCodeValidationResult validatePromoCode(String code, double purchaseAmount) {
        if (code == null || code.isEmpty()) {
            return new PromoCodeValidationResult(false, "Please enter a promo code.", null);
        }
        
        try {
            PromoCode promoCode = promoCodeDAO.getPromoCodeByCode(code);
            
            if (promoCode == null) {
                return new PromoCodeValidationResult(false, "Invalid promo code.", null);
            }
            
            if (!promoCode.isActive()) {
                return new PromoCodeValidationResult(false, "This promo code is no longer active.", null);
            }
            
            LocalDate today = LocalDate.now();
            if (today.isBefore(promoCode.getValidFrom())) {
                return new PromoCodeValidationResult(false, 
                    "This promo code is not valid yet. Valid from " + promoCode.getFormattedValidFrom() + ".", 
                    null);
            }
            
            if (today.isAfter(promoCode.getValidUntil())) {
                return new PromoCodeValidationResult(false, 
                    "This promo code has expired. Was valid until " + promoCode.getFormattedValidUntil() + ".", 
                    null);
            }
            
            if (promoCode.getMaxUses() != null && promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
                return new PromoCodeValidationResult(false, 
                    "This promo code has reached its maximum number of uses.", 
                    null);
            }
            
            if (purchaseAmount < promoCode.getMinPurchaseAmount()) {
                return new PromoCodeValidationResult(false, 
                    "This promo code requires a minimum purchase of ₱" + promoCode.getMinPurchaseAmount() + ".", 
                    null);
            }
            
            double discount = promoCode.calculateDiscount(purchaseAmount);
            String message = "Promo code applied: " + promoCode.getFormattedDiscount() + " off";
            
            return new PromoCodeValidationResult(true, message, promoCode);
            
        } catch (SQLException e) {
            System.err.println("Error validating promo code: " + e.getMessage());
            return new PromoCodeValidationResult(false, "An error occurred while validating the promo code.", null);
        }
    }
    
    /**
     * A class representing the result of a promo code validation.
     */
    public static class PromoCodeValidationResult {
        private boolean valid;
        private String message;
        private PromoCode promoCode;
        
        public PromoCodeValidationResult(boolean valid, String message, PromoCode promoCode) {
            this.valid = valid;
            this.message = message;
            this.promoCode = promoCode;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public PromoCode getPromoCode() {
            return promoCode;
        }
    }
}