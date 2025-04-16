package org.godsendjoseph.pet_app.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.godsendjoseph.pet_app.models.User;
import org.godsendjoseph.pet_app.ui.activities.LoginActivity;

/**
 * Manager class for session handling - to check if user is logged in,
 * save login state, and manage session timeouts.
 */
public class SessionManager {
    private static final String TAG = "SessionManager";

    // Shared preferences constants
    private static final String PREF_NAME = "session_prefs";
    private static final String KEY_LAST_ACTIVE = "last_active_time";
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds

    // Singleton instance
    private static SessionManager instance;

    private Context context;
    private SharedPreferences preferences;
    private AuthManager authManager;

    // Private constructor to enforce singleton pattern
    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.authManager = AuthManager.getInstance(context);
    }

    /**
     * Get the singleton instance of SessionManager
     * @param context Application context
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Update the last active timestamp to keep the session alive
     */
    public void refreshSession() {
        if (authManager.isLoggedIn()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
            editor.apply();
        }
    }

    /**
     * Check if the session is still valid or has timed out
     * @return true if session is valid, false if timed out
     */
    public boolean isSessionValid() {
        if (!authManager.isLoggedIn()) {
            return false;
        }

        long lastActive = preferences.getLong(KEY_LAST_ACTIVE, 0);
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastActive;

        return elapsedTime < SESSION_TIMEOUT;
    }

    /**
     * End the current session (timeout or manual logout)
     */
    public void endSession() {
        authManager.logout();

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Check session validity and redirect to login if invalid
     * @return true if session is valid, false if redirected to login
     */
    public boolean checkSessionAndRedirect() {
        if (!isSessionValid()) {
            endSession();

            // Redirect to login screen
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            return false;
        }

        // Session is valid, refresh it
        refreshSession();
        return true;
    }

    /**
     * Start a new session for the given user
     * @param user User to start session for
     */
    public void startSession(User user) {
        refreshSession();
    }
}
