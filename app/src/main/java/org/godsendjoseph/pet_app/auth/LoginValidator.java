package org.godsendjoseph.pet_app.auth;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Validator for login and registration form fields.
 * Contains methods to validate usernames, emails, passwords, etc.
 */
public class LoginValidator {
    /**
     * Validate a username
     * @param username Username to validate
     * @return null if valid, error message if invalid
     */
    public static String validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return "Username is required";
        }

        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }

        if (username.length() > 30) {
            return "Username cannot exceed 30 characters";
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscore";
        }

        return null; // Valid
    }

    /**
     * Validate an email address
     * @param email Email to validate
     * @return null if valid, error message if invalid
     */
    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address";
        }

        return null; // Valid
    }

    /**
     * Validate a password
     * @param password Password to validate
     * @param isRegistration true if validating for registration (stricter), false for login
     * @return null if valid, error message if invalid
     */
    public static String validatePassword(String password, boolean isRegistration) {
        if (TextUtils.isEmpty(password)) {
            return "Password is required";
        }

        if (isRegistration) {
            if (password.length() < 6) {
                return "Password must be at least 6 characters";
            }

            if (!password.matches(".*\\d.*")) {
                return "Password must contain at least one number";
            }

            if (!password.matches(".*[A-Z].*")) {
                return "Password must contain at least one uppercase letter";
            }

            if (!password.matches(".*[a-z].*")) {
                return "Password must contain at least one lowercase letter";
            }
        }

        return null; // Valid
    }

    /**
     * Validate password confirmation (for registration)
     * @param password Original password
     * @param confirmPassword Password confirmation
     * @return null if valid, error message if invalid
     */
    public static String validatePasswordConfirmation(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return "Please confirm your password";
        }

        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }

        return null; // Valid
    }

    /**
     * Validate a full name
     * @param fullName Full name to validate
     * @return null if valid, error message if invalid
     */
    public static String validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "Full name is required";
        }

        if (fullName.length() < 2) {
            return "Full name must be at least 2 characters";
        }

        if (fullName.length() > 50) {
            return "Full name cannot exceed 50 characters";
        }

        return null; // Valid
    }
}
