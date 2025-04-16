package org.godsendjoseph.pet_app.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.godsendjoseph.pet_app.database.UserDAO;
import org.godsendjoseph.pet_app.models.User;
import org.godsendjoseph.pet_app.utils.PasswordUtils;

/**
 * Manager class for handling authentication operations like login, registration,
 * and session management.
 * Implements the Singleton pattern for centralized auth management.
 */
public class AuthManager {
    private static final String TAG = "AuthManager";

    // Shared preferences constants
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Singleton instance
    private static AuthManager instance;

    private Context context;
    private SharedPreferences preferences;
    private UserDAO userDAO;

    // Private constructor to enforce singleton pattern
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userDAO = new UserDAO(context);
    }

    /**
     * Get the singleton instance of AuthManager
     * @param context Application context
     * @return AuthManager instance
     */
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Register a new user
     * @param username Username
     * @param email Email address
     * @param password Password (will be hashed)
     * @param fullName User's full name
     * @return Registration result with status and message
     */
    public AuthResult register(String username, String email, String password, String fullName) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            return new AuthResult(false, "Username is required");
        }

        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "Email is required");
        }

        if (password == null || password.length() < 6) {
            return new AuthResult(false, "Password must be at least 6 characters");
        }

        // Check if username or email already exists
        if (userDAO.isUsernameExists(username)) {
            return new AuthResult(false, "Username already exists");
        }

        if (userDAO.isEmailExists(email)) {
            return new AuthResult(false, "Email already exists");
        }

        // Hash the password (in a real app, use a proper password hashing library)
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Create new user
        User user = new User(username, email, hashedPassword, fullName);

        // Insert user into database
        long userId = userDAO.insertUser(user);

        if (userId > 0) {
            return new AuthResult(true, "Registration successful");
        } else {
            return new AuthResult(false, "Registration failed");
        }
    }

    /**
     * Login a user
     * @param usernameOrEmail Username or email
     * @param password Password
     * @return Login result with status and message
     */
    public AuthResult login(String usernameOrEmail, String password) {
        // Validate input
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return new AuthResult(false, "Username or email is required");
        }

        if (password == null || password.isEmpty()) {
            return new AuthResult(false, "Password is required");
        }

        // Hash the password to compare with stored hash
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Check credentials
        User user = userDAO.authenticateUser(usernameOrEmail, hashedPassword);

        if (user != null) {
            // Save user session
            saveUserSession(user);
            return new AuthResult(true, "Login successful");
        } else {
            return new AuthResult(false, "Invalid username/email or password");
        }
    }

    /**
     * Authenticate a user without creating a session
     * Used for verifying credentials during operations like password change
     * @param usernameOrEmail Username or email
     * @param password Password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return null;
        }

        // Hash the password to compare with stored hash
        String hashedPassword = PasswordUtils.hashPassword(password);

        // Use the UserDAO to authenticate
        return userDAO.authenticateUser(usernameOrEmail, hashedPassword);
    }

    /**
     * Logout the current user
     */
    public void logout() {
        // Clear user session
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Check if a user is currently logged in
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get the ID of the currently logged in user
     * @return User ID, or -1 if not logged in
     */
    public int getCurrentUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get the username of the currently logged in user
     * @return Username, or null if not logged in
     */
    public String getCurrentUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    /**
     * Get the email of the currently logged in user
     * @return Email, or null if not logged in
     */
    public String getCurrentEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    /**
     * Get the current logged in user
     * @return User object, or null if not logged in
     */
    public User getCurrentUser() {
        int userId = getCurrentUserId();
        if (userId != -1) {
            return userDAO.getUserById(userId);
        }
        return null;
    }

    /**
     * Save user session data to shared preferences
     * @param user User to save
     */
    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
}
