package org.godsendjoseph.pet_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1500; // milliseconds

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Use a handler to delay the transition to the next screen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthAndNavigate();
            }
        }, SPLASH_DURATION);
    }

    /**
     * Check authentication status and navigate to the appropriate screen
     */
    private void checkAuthAndNavigate() {
        if (authManager.isLoggedIn() && sessionManager.isSessionValid()) {
            // User is logged in and session is valid, go to main activity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // User is not logged in or session expired, go to login activity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        // Close the splash activity
        finish();
    }
}