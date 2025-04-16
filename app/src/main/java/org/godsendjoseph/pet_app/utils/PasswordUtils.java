package org.godsendjoseph.pet_app.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password operations like hashing and validation.
 * Note: For a production app, use a more secure password hashing library.
 */
public class PasswordUtils {
   /**
            * Hash a password using SHA-256
            * Note: This is a simple implementation for demonstration.
     * In a real app, use a proper password hashing library with salt.
     *
             * @param password Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple string if hashing fails
            return password + "_hashed";
        }
    }

    /**
     * Validate password complexity
     *
     * @param password Password to validate
     * @return true if password meets complexity requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        return true;
    }
}
