package com.cinebook.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int HASH_ITERATIONS = 10000;
    
    /**
     * Hashes a password using SHA-256 algorithm with salt.
     *
     * @param password The password to hash
     * @return The hashed password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password
            byte[] hash = hashWithSalt(password.getBytes(StandardCharsets.UTF_8), salt);
            
            // Combine salt and hash
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);
            
            // Return as Base64 string
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash.
     *
     * @param password The password to verify
     * @param hashedPassword The stored hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            // Decode the stored hash
            byte[] combined = Base64.getDecoder().decode(hashedPassword);
            
            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, salt.length);
            
            // Extract stored hash
            byte[] storedHash = new byte[combined.length - salt.length];
            System.arraycopy(combined, salt.length, storedHash, 0, storedHash.length);
            
            // Hash the provided password with the extracted salt
            byte[] newHash = hashWithSalt(password.getBytes(StandardCharsets.UTF_8), salt);
            
            // Compare the hashes
            return MessageDigest.isEqual(storedHash, newHash);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Hashes data with a salt using the specified algorithm.
     *
     * @param data The data to hash
     * @param salt The salt to use
     * @return The hashed data
     * @throws NoSuchAlgorithmException If the algorithm is not available
     */
    private static byte[] hashWithSalt(byte[] data, byte[] salt) throws NoSuchAlgorithmException {
        // Create a digest instance for the hashing algorithm
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        
        // Add salt
        digest.update(salt);
        
        // Hash data with multiple iterations for security
        byte[] hash = digest.digest(data);
        for (int i = 0; i < HASH_ITERATIONS - 1; i++) {
            digest.reset();
            hash = digest.digest(hash);
        }
        
        return hash;
    }
}