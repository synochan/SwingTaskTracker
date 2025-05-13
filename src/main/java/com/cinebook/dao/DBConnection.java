package com.cinebook.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 * Singleton class to manage database connections.
 * This class handles establishing a connection to the SQLite database and provides
 * a method to get the connection instance.
 */
public class DBConnection {
    private static Connection connection = null;
    private static final String DATABASE_PATH = "db/cinebook.db";
    
    // Private constructor to prevent instantiation
    private DBConnection() { }
    
    /**
     * Gets a connection to the database.
     * If a connection doesn't exist, it creates one.
     *
     * @return The Connection object
     * @throws SQLException If a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Create the db directory if it doesn't exist
                File dbDirectory = new File("db");
                if (!dbDirectory.exists()) {
                    dbDirectory.mkdirs();
                }
                
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                
                // Create a connection to the database
                connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH);
                
                // Set PRAGMA statements for SQLite
                Statement statement = connection.createStatement();
                statement.execute("PRAGMA foreign_keys = ON");
                statement.close();
                
                System.out.println("Connected to the SQLite database.");
                
                // Initialize the database schema if it doesn't exist
                initializeDatabase();
                
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC driver not found: " + e.getMessage());
                throw new SQLException("SQLite JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    /**
     * Initializes the database schema if it doesn't exist.
     * This method creates all the necessary tables for the application.
     */
    private static void initializeDatabase() {
        try {
            Statement statement = connection.createStatement();
            
            // Create Users table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "phone_number TEXT, " +
                "full_name TEXT NOT NULL, " +
                "is_admin INTEGER NOT NULL DEFAULT 0, " +
                "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Create Cinemas table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS cinemas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "location TEXT NOT NULL, " +
                "total_seats INTEGER NOT NULL, " +
                "total_rows INTEGER NOT NULL, " +
                "seats_per_row INTEGER NOT NULL, " +
                "has_deluxe_seats INTEGER NOT NULL DEFAULT 0, " +
                "is_active INTEGER NOT NULL DEFAULT 1" +
                ")"
            );
            
            // Create Movies table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "director TEXT NOT NULL, " +
                "cast TEXT NOT NULL, " +
                "genre TEXT NOT NULL, " +
                "synopsis TEXT NOT NULL, " +
                "duration_minutes INTEGER NOT NULL, " +
                "rating TEXT NOT NULL, " +
                "release_date TEXT NOT NULL, " +
                "poster_url TEXT, " +
                "is_active INTEGER NOT NULL DEFAULT 1" +
                ")"
            );
            
            // Create Screenings table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS screenings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER NOT NULL, " +
                "cinema_id INTEGER NOT NULL, " +
                "screening_time TIMESTAMP NOT NULL, " +
                "standard_seat_price REAL NOT NULL, " +
                "deluxe_seat_price REAL NOT NULL, " +
                "is_active INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (movie_id) REFERENCES movies(id), " +
                "FOREIGN KEY (cinema_id) REFERENCES cinemas(id)" +
                ")"
            );
            
            // Create Seats table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS seats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "screening_id INTEGER NOT NULL, " +
                "seat_number TEXT NOT NULL, " +
                "seat_type TEXT NOT NULL, " +
                "is_reserved INTEGER NOT NULL DEFAULT 0, " +
                "row_number INTEGER NOT NULL, " +
                "column_number INTEGER NOT NULL, " +
                "FOREIGN KEY (screening_id) REFERENCES screenings(id), " +
                "UNIQUE (screening_id, seat_number)" +
                ")"
            );
            
            // Create Concessions table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS concessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "category TEXT NOT NULL, " +
                "is_available INTEGER NOT NULL DEFAULT 1" +
                ")"
            );
            
            // Create Reservations table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "guest_name TEXT, " +
                "guest_email TEXT, " +
                "guest_phone TEXT, " +
                "screening_id INTEGER NOT NULL, " +
                "reservation_time TIMESTAMP NOT NULL, " +
                "total_amount REAL NOT NULL, " +
                "is_paid INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (screening_id) REFERENCES screenings(id)" +
                ")"
            );
            
            // Create ReservationSeats table (junction table)
            statement.execute(
                "CREATE TABLE IF NOT EXISTS reservation_seats (" +
                "reservation_id INTEGER NOT NULL, " +
                "seat_id INTEGER NOT NULL, " +
                "PRIMARY KEY (reservation_id, seat_id), " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id), " +
                "FOREIGN KEY (seat_id) REFERENCES seats(id)" +
                ")"
            );
            
            // Create ReservationConcessions table (junction table)
            statement.execute(
                "CREATE TABLE IF NOT EXISTS reservation_concessions (" +
                "reservation_id INTEGER NOT NULL, " +
                "concession_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "PRIMARY KEY (reservation_id, concession_id), " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id), " +
                "FOREIGN KEY (concession_id) REFERENCES concessions(id)" +
                ")"
            );
            
            // Create Payments table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER NOT NULL, " +
                "amount REAL NOT NULL, " +
                "payment_method TEXT NOT NULL, " +
                "transaction_reference TEXT NOT NULL, " +
                "payment_time TIMESTAMP NOT NULL, " +
                "is_successful INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id)" +
                ")"
            );
            
            // Create Tickets table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS tickets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reservation_id INTEGER NOT NULL, " +
                "seat_id INTEGER NOT NULL, " +
                "ticket_code TEXT NOT NULL UNIQUE, " +
                "is_used INTEGER NOT NULL DEFAULT 0, " +
                "generation_time TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(id), " +
                "FOREIGN KEY (seat_id) REFERENCES seats(id)" +
                ")"
            );
            
            statement.close();
            System.out.println("Database schema initialized.");
            
            // Insert initial admin user if none exists
            insertInitialData();
            
        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inserts initial data into the database if it doesn't exist.
     * This includes an admin user, sample cinemas, and concession items.
     */
    private static void insertInitialData() {
        try {
            Statement statement = connection.createStatement();
            
            // Check if any users exist
            if (statement.executeQuery("SELECT COUNT(*) FROM users").getInt(1) == 0) {
                // Insert admin user
                statement.execute(
                    "INSERT INTO users (username, password, email, phone_number, full_name, is_admin) " +
                    "VALUES ('admin', 'admin123', 'admin@cinebook.com', '09123456789', 'System Administrator', 1)"
                );
                System.out.println("Admin user created.");
            }
            
            // Check if any cinemas exist
            if (statement.executeQuery("SELECT COUNT(*) FROM cinemas").getInt(1) == 0) {
                // Insert sample cinemas
                statement.execute(
                    "INSERT INTO cinemas (name, location, total_seats, total_rows, seats_per_row, has_deluxe_seats) " +
                    "VALUES ('CineStar Limketkai', 'Limketkai Center, Cagayan de Oro', 120, 10, 12, 1)"
                );
                statement.execute(
                    "INSERT INTO cinemas (name, location, total_seats, total_rows, seats_per_row, has_deluxe_seats) " +
                    "VALUES ('FilmHouse Centrio', 'Centrio Mall, Cagayan de Oro', 100, 10, 10, 1)"
                );
                statement.execute(
                    "INSERT INTO cinemas (name, location, total_seats, total_rows, seats_per_row, has_deluxe_seats) " +
                    "VALUES ('MovieWorld SM CDO', 'SM City, Cagayan de Oro', 150, 15, 10, 1)"
                );
                System.out.println("Sample cinemas created.");
            }
            
            // Check if any concessions exist
            if (statement.executeQuery("SELECT COUNT(*) FROM concessions").getInt(1) == 0) {
                // Insert sample concessions
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Small Popcorn', 'Freshly popped buttered popcorn (small)', 80.00, 'Food', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Large Popcorn', 'Freshly popped buttered popcorn (large)', 120.00, 'Food', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Nachos with Cheese', 'Crispy nachos with cheese dip', 100.00, 'Food', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Hotdog Sandwich', 'Juicy hotdog in a fresh bun', 90.00, 'Food', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Small Soda', 'Regular soda drink (small)', 60.00, 'Drinks', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Large Soda', 'Regular soda drink (large)', 80.00, 'Drinks', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Bottled Water', '500ml purified water', 40.00, 'Drinks', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Movie Combo 1', 'Large popcorn + 2 regular sodas', 180.00, 'Combo', 1)"
                );
                statement.execute(
                    "INSERT INTO concessions (name, description, price, category, is_available) " +
                    "VALUES ('Movie Combo 2', 'Large popcorn + nachos + 2 regular sodas', 250.00, 'Combo', 1)"
                );
                System.out.println("Sample concessions created.");
            }
            
            statement.close();
            
        } catch (SQLException e) {
            System.err.println("Error inserting initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Closes the database connection.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
