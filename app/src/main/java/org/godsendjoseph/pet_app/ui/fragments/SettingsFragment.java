package org.godsendjoseph.pet_app.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.LoginValidator;
import org.godsendjoseph.pet_app.models.User;
import org.godsendjoseph.pet_app.ui.activities.LoginActivity;
import org.godsendjoseph.pet_app.ui.viewmodels.SettingsViewModel;

/**
 * Fragment for user settings and profile management.
 */
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private AuthManager authManager;

    // Profile section
    private TextView tvUsername;
    private EditText etFullName;
    private EditText etEmail;
    private Button btnUpdateProfile;

    // Password section
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnChangePassword;

    // Settings section
    private Switch switchDarkMode;
    private Spinner spinnerCurrency;
    private Switch switchNotifications;

    // Account actions
    private Button btnLogout;
    private Button btnDeleteAccount;

    // Other UI
    private ProgressBar progressBar;

    // Currency options
    private final String[] CURRENCIES = {
            "$", "€", "£", "¥", "₹", "₽", "₩", "₿"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize AuthManager
        authManager = AuthManager.getInstance(requireContext());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Initialize views
        initViews(view);

        // Setup currency spinner
        setupCurrencySpinner();

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();
    }

    private void initViews(View view) {
        // Profile section
        tvUsername = view.findViewById(R.id.tv_username);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        btnUpdateProfile = view.findViewById(R.id.btn_update_profile);

        // Password section
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        // Settings section
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        // Account actions
        btnLogout = view.findViewById(R.id.btn_logout);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);

        // Other UI
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, CURRENCIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
    }

    private void setupListeners() {
        // Profile update button
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        // Password change button
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        // Dark mode switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkMode(isChecked);
            // In a real app, you would apply the theme change here
        });

        // Currency spinner
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currency = CURRENCIES[position];
                viewModel.setCurrency(currency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Notifications switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotificationEnabled(isChecked);
        });

        // Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Delete account button
        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountDialog();
            }
        });
    }

    private void observeViewModel() {
        // Observe user profile
        viewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    tvUsername.setText(user.getUsername());
                    etFullName.setText(user.getFullName());
                    etEmail.setText(user.getEmail());
                }
            }
        });

        // Observe dark mode setting
        viewModel.getDarkModeLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean darkModeEnabled) {
                switchDarkMode.setChecked(darkModeEnabled);
            }
        });

        // Observe currency setting
        viewModel.getCurrencyLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String currency) {
                for (int i = 0; i < CURRENCIES.length; i++) {
                    if (CURRENCIES[i].equals(currency)) {
                        spinnerCurrency.setSelection(i);
                        break;
                    }
                }
            }
        });

        // Observe notification setting
        viewModel.getNotificationEnabledLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean notificationEnabled) {
                switchNotifications.setChecked(notificationEnabled);
            }
        });

        // Observe loading state
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validate input
        String fullNameError = LoginValidator.validateFullName(fullName);
        String emailError = LoginValidator.validateEmail(email);

        if (fullNameError != null) {
            etFullName.setError(fullNameError);
            etFullName.requestFocus();
            return;
        }

        if (emailError != null) {
            etEmail.setError(emailError);
            etEmail.requestFocus();
            return;
        }

        // Update profile
        viewModel.updateUserProfile(fullName, email).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate input
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return;
        }

        String passwordError = LoginValidator.validatePassword(newPassword, true);
        if (passwordError != null) {
            etNewPassword.setError(passwordError);
            etNewPassword.requestFocus();
            return;
        }

        String confirmPasswordError = LoginValidator.validatePasswordConfirmation(newPassword, confirmPassword);
        if (confirmPasswordError != null) {
            etConfirmPassword.setError(confirmPasswordError);
            etConfirmPassword.requestFocus();
            return;
        }

        // Change password
        viewModel.changePassword(currentPassword, newPassword).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();

                    // Clear password fields
                    etCurrentPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                }
            }
        });
    }

    private void logout() {
        authManager.logout();

        // Navigate to login screen
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showDeleteAccountDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_delete_account, null);

        EditText etPassword = dialogView.findViewById(R.id.et_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setView(dialogView)
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = etPassword.getText().toString();

                        if (TextUtils.isEmpty(password)) {
                            etPassword.setError("Password is required");
                            etPassword.requestFocus();
                            return;
                        }

                        // Delete account
                        viewModel.deleteAccount(password).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean success) {
                                if (success) {
                                    dialog.dismiss();

                                    // Navigate to login screen
                                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }
                            }
                        });
                    }
                });
            }
        });

        dialog.show();
    }
}