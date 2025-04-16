package org.godsendjoseph.pet_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.AuthResult;
import org.godsendjoseph.pet_app.auth.LoginValidator;
import org.godsendjoseph.pet_app.auth.SessionManager;

/**
 * Activity for user login.
 * Handles user authentication and redirects to main activity on success.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsernameEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn() && sessionManager.isSessionValid()) {
            navigateToMainActivity();
            return;
        }

        // Initialize views
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsernameEmail = findViewById(R.id.et_username_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        // Get input values
        String usernameEmail = etUsernameEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        String usernameEmailError = validateUsernameEmail(usernameEmail);
        String passwordError = LoginValidator.validatePassword(password, false);

        if (usernameEmailError != null) {
            etUsernameEmail.setError(usernameEmailError);
            etUsernameEmail.requestFocus();
            return;
        }

        if (passwordError != null) {
            etPassword.setError(passwordError);
            etPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Perform login in a separate thread to avoid UI freezing
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AuthResult result = authManager.login(usernameEmail, password);

                // Update UI on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);

                        if (result.isSuccess()) {
                            // Start a new session
                            sessionManager.startSession(authManager.getCurrentUser());

                            // Navigate to main activity
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private String validateUsernameEmail(String usernameEmail) {
        if (TextUtils.isEmpty(usernameEmail)) {
            return "Username or email is required";
        }
        return null;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}