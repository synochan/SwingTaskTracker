package com.cinebook.controller;

import com.cinebook.dao.UserDAO;
import com.cinebook.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for user-related operations.
 * Handles business logic for user management.
 */
public class UserController {
    private UserDAO userDAO;
    private User currentUser; // Store the currently logged-in user
    
    /**
     * Constructor for UserController.
     */
    public UserController() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }
    
    /**
     * Authenticates a user with the given credentials.
     *
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return true if authentication is successful, false otherwise
     */
    public boolean login(String username, String password) {
        try {
            User user = userDAO.authenticateUser(username, password);
            if (user != null) {
                this.currentUser = user;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Creates a new user account.
     *
     * @param username The username for the new account
     * @param password The password for the new account
     * @param email The email for the new account
     * @param phoneNumber The phone number for the new account
     * @param fullName The full name for the new account
     * @param isAdmin Whether the new account is an admin
     * @return true if account creation is successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email, 
                              String phoneNumber, String fullName, boolean isAdmin) {
        try {
            // Check if username already exists
            if (userDAO.usernameExists(username)) {
                return false;
            }
            
            // Check if email already exists
            if (userDAO.emailExists(email)) {
                return false;
            }
            
            User newUser = new User(username, password, email, phoneNumber, fullName, isAdmin);
            int userId = userDAO.addUser(newUser);
            return userId > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates an existing user's profile.
     *
     * @param user The User object with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserProfile(User user) {
        try {
            boolean success = userDAO.updateUser(user);
            
            // If we're updating the current user, refresh the current user object
            if (success && currentUser != null && currentUser.getId() == user.getId()) {
                currentUser = user;
            }
            
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The User object if found, null otherwise
     */
    public User getUserById(int id) {
        try {
            return userDAO.getUserById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all users.
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteUser(int id) {
        try {
            // Don't allow deleting the current user
            if (currentUser != null && currentUser.getId() == id) {
                return false;
            }
            
            return userDAO.deleteUser(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets the currently logged-in user.
     *
     * @return The currently logged-in User, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if the current user is an admin.
     *
     * @return true if the current user is an admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
}
