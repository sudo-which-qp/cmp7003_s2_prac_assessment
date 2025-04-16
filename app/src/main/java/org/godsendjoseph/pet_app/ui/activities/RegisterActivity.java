package org.godsendjoseph.pet_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etFullName;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Initialize views
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etFullName = findViewById(R.id.et_full_name);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login activity
            }
        });
    }

    private void register() {
        // Get input values
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        // Validate input
        String usernameError = LoginValidator.validateUsername(username);
        String emailError = LoginValidator.validateEmail(email);
        String passwordError = LoginValidator.validatePassword(password, true);
        String confirmPasswordError = LoginValidator.validatePasswordConfirmation(password, confirmPassword);
        String fullNameError = LoginValidator.validateFullName(fullName);

        // Check for validation errors
        if (usernameError != null) {
            etUsername.setError(usernameError);
            etUsername.requestFocus();
            return;
        }

        if (emailError != null) {
            etEmail.setError(emailError);
            etEmail.requestFocus();
            return;
        }

        if (passwordError != null) {
            etPassword.setError(passwordError);
            etPassword.requestFocus();
            return;
        }

        if (confirmPasswordError != null) {
            etConfirmPassword.setError(confirmPasswordError);
            etConfirmPassword.requestFocus();
            return;
        }

        if (fullNameError != null) {
            etFullName.setError(fullNameError);
            etFullName.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Perform registration in a separate thread to avoid UI freezing
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AuthResult result = authManager.register(username, email, password, fullName);

                // Update UI on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        if (result.isSuccess()) {
                            Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();

                            // Automatically login the user
                            final AuthResult loginResult = authManager.login(username, password);

                            if (loginResult.isSuccess()) {
                                // Start a new session
                                sessionManager.startSession(authManager.getCurrentUser());

                                // Navigate to main activity
                                navigateToMainActivity();
                            } else {
                                // If auto-login fails, go back to login screen
                                finish();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}