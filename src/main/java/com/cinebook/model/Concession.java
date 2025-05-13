package com.cinebook.model;

/**
 * Represents a concession item that can be purchased alongside movie tickets.
 */
public class Concession {
    private int id;
    private String name;
    private String description;
    private double price;
    private boolean isAvailable;
    private String category; // e.g., "Food", "Drinks", "Combo"
    private int quantity; // Used for shopping cart functionality, not stored in DB
    
    // Default constructor
    public Concession() {
        this.quantity = 0;
    }
    
    // Constructor for creating a new concession item
    public Concession(String name, String description, double price, boolean isAvailable, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = isAvailable;
        this.category = category;
        this.quantity = 0;
    }
    
    // Constructor with ID (typically used when retrieving from database)
    public Concession(int id, String name, String description, double price, boolean isAvailable, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = isAvailable;
        this.category = category;
        this.quantity = 0;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public boolean isAvailable() {
        return isAvailable;
    }
    
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    // Increment quantity by 1
    public void incrementQuantity() {
        this.quantity++;
    }
    
    // Decrement quantity by 1
    public void decrementQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }
    
    // Get the total price for this concession (price * quantity)
    public double getTotalPrice() {
        return price * quantity;
    }
    
    // Get the formatted price with currency symbol
    public String getFormattedPrice() {
        return String.format("₱%.2f", price);
    }
    
    // Get the formatted total price with currency symbol
    public String getFormattedTotalPrice() {
        return String.format("₱%.2f", getTotalPrice());
    }
    
    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }
    
    // Create a deep copy of this concession
    public Concession copy() {
        Concession copy = new Concession(this.id, this.name, this.description, this.price, this.isAvailable, this.category);
        copy.setQuantity(this.quantity);
        return copy;
    }
}
