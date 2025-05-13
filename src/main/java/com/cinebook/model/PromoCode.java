package com.cinebook.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a promotional code.
 */
public class PromoCode {
    private int id;
    private String code;
    private String description;
    private DiscountType discountType;
    private double discountAmount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Integer maxUses;  // Nullable
    private int currentUses;
    private double minPurchaseAmount;
    private boolean isActive;
    
    /**
     * Enum representing types of discounts.
     */
    public enum DiscountType {
        PERCENTAGE,
        FIXED
    }
    
    /**
     * Default constructor.
     */
    public PromoCode() {
        this.currentUses = 0;
        this.minPurchaseAmount = 0.0;
        this.isActive = true;
    }
    
    /**
     * Constructor with all fields.
     */
    public PromoCode(int id, String code, String description, DiscountType discountType, 
                     double discountAmount, LocalDate validFrom, LocalDate validUntil,
                     Integer maxUses, int currentUses, double minPurchaseAmount, boolean isActive) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        this.currentUses = currentUses;
        this.minPurchaseAmount = minPurchaseAmount;
        this.isActive = isActive;
    }
    
    /**
     * Constructor for creating a new promo code (without ID).
     */
    public PromoCode(String code, String description, DiscountType discountType, 
                     double discountAmount, LocalDate validFrom, LocalDate validUntil,
                     Integer maxUses, double minPurchaseAmount) {
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        this.currentUses = 0;
        this.minPurchaseAmount = minPurchaseAmount;
        this.isActive = true;
    }
    
    // Getters and Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public DiscountType getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }
    
    public double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public LocalDate getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDate getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public int getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(int currentUses) {
        this.currentUses = currentUses;
    }
    
    public double getMinPurchaseAmount() {
        return minPurchaseAmount;
    }
    
    public void setMinPurchaseAmount(double minPurchaseAmount) {
        this.minPurchaseAmount = minPurchaseAmount;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Calculate the discount amount based on the purchase total.
     *
     * @param purchaseTotal The total amount of the purchase
     * @return The discount amount
     */
    public double calculateDiscount(double purchaseTotal) {
        if (!isValid() || purchaseTotal < minPurchaseAmount) {
            return 0.0;
        }
        
        if (discountType == DiscountType.PERCENTAGE) {
            return purchaseTotal * (discountAmount / 100.0);
        } else { // FIXED
            return Math.min(discountAmount, purchaseTotal); // Can't discount more than the total
        }
    }
    
    /**
     * Check if the promo code is valid.
     *
     * @return true if the code is valid, false otherwise
     */
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        
        // Check if code is active
        if (!isActive) {
            return false;
        }
        
        // Check date range
        if (today.isBefore(validFrom) || today.isAfter(validUntil)) {
            return false;
        }
        
        // Check usage limits
        if (maxUses != null && currentUses >= maxUses) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Increment the usage count of this promo code.
     */
    public void incrementUsage() {
        this.currentUses++;
    }
    
    /**
     * Format the valid from date.
     *
     * @return The formatted date
     */
    public String getFormattedValidFrom() {
        if (validFrom == null) {
            return "";
        }
        return validFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * Format the valid until date.
     *
     * @return The formatted date
     */
    public String getFormattedValidUntil() {
        if (validUntil == null) {
            return "";
        }
        return validUntil.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * Get a user-friendly representation of the discount.
     *
     * @return A string describing the discount
     */
    public String getFormattedDiscount() {
        if (discountType == DiscountType.PERCENTAGE) {
            return String.format("%.0f%%", discountAmount);
        } else {
            return String.format("₱%.2f", discountAmount);
        }
    }
    
    @Override
    public String toString() {
        return code + " - " + description + " (" + getFormattedDiscount() + ")";
    }
}