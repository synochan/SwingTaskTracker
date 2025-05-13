package com.cinebook.model;

/**
 * Enum representing different payment methods available for transactions.
 */
public enum PaymentMethod {
    GCASH("GCash"),
    PAYMAYA("PayMaya"),
    CREDIT_CARD("Credit Card");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
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
