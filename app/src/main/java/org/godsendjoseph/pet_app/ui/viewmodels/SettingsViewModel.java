package org.godsendjoseph.pet_app.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.SessionManager;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.database.ExpenseDAO;
import org.godsendjoseph.pet_app.database.UserDAO;
import org.godsendjoseph.pet_app.models.User;

/**
 * ViewModel for app settings and user profile management.
 */
public class SettingsViewModel extends AndroidViewModel {
    private static final String PREF_DARK_MODE = "dark_mode";
    private static final String PREF_CURRENCY = "currency";
    private static final String PREF_NOTIFICATION = "notification";

    private UserDAO userDAO;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;

    private MutableLiveData<User> userProfileLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> darkModeLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> currencyLiveData = new MutableLiveData<>("$");
    private MutableLiveData<Boolean> notificationEnabledLiveData = new MutableLiveData<>(true);

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        userDAO = new UserDAO(application);
        expenseDAO = new ExpenseDAO(application);
        categoryDAO = new CategoryDAO(application);
        authManager = AuthManager.getInstance(application);
        sessionManager = SessionManager.getInstance(application);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);

        // Load settings
        loadSettings();

        // Load user profile data
        loadUserProfile();
    }

    /**
     * Load current user profile
     */
    public void loadUserProfile() {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    User user = userDAO.getUserById(userId);

                    if (user != null) {
                        userProfileLiveData.postValue(user);
                    } else {
                        errorMessageLiveData.postValue("Failed to load user profile");
                    }

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading user profile: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Update user profile information
     * @param fullName User's full name
     * @param email User's email
     * @return LiveData with success state
     */
    public LiveData<Boolean> updateUserProfile(String fullName, String email) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    User currentUser = userProfileLiveData.getValue();

                    if (currentUser != null) {
                        // Check if email is already taken by another user
                        if (!currentUser.getEmail().equals(email) && userDAO.isEmailExists(email)) {
                            errorMessageLiveData.postValue("Email is already in use");
                            successLiveData.postValue(false);
                            isLoadingLiveData.postValue(false);
                            return;
                        }

                        // Update user details
                        currentUser.setFullName(fullName);
                        currentUser.setEmail(email);

                        int result = userDAO.updateUser(currentUser);

                        if (result > 0) {
                            // Success
                            userProfileLiveData.postValue(currentUser);
                            successLiveData.postValue(true);
                        } else {
                            // Failure
                            errorMessageLiveData.postValue("Failed to update profile");
                            successLiveData.postValue(false);
                        }
                    } else {
                        errorMessageLiveData.postValue("User profile not loaded");
                        successLiveData.postValue(false);
                    }

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error updating profile: " + e.getMessage());
                    successLiveData.postValue(false);
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();

        return successLiveData;
    }

    /**
     * Change user password
     * @param currentPassword Current password
     * @param newPassword New password
     * @return LiveData with success state
     */
    public LiveData<Boolean> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Check current password
                    User user = authManager.authenticateUser(
                            authManager.getCurrentUsername(), currentPassword);

                    if (user != null) {
                        // Current password is correct, update to new password
                        user.setPassword(newPassword);
                        int result = userDAO.updateUser(user);

                        if (result > 0) {
                            // Success
                            successLiveData.postValue(true);
                        } else {
                            // Update failed
                            errorMessageLiveData.postValue("Failed to update password");
                            successLiveData.postValue(false);
                        }
                    } else {
                        // Current password is incorrect
                        errorMessageLiveData.postValue("Current password is incorrect");
                        successLiveData.postValue(false);
                    }

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error changing password: " + e.getMessage());
                    successLiveData.postValue(false);
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();

        return successLiveData;
    }

    /**
     * Delete user account and all associated data
     * @param password User's password for confirmation
     * @return LiveData with success state
     */
    public LiveData<Boolean> deleteAccount(String password) {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Verify password
                    User user = authManager.authenticateUser(
                            authManager.getCurrentUsername(), password);

                    if (user != null) {
                        // Password is correct, proceed with deletion
                        int userId = user.getId();

                        // Delete user's data (the database CASCADE will handle related data)
                        int result = userDAO.deleteUser(userId);

                        if (result > 0) {
                            // End session
                            sessionManager.endSession();
                            successLiveData.postValue(true);
                        } else {
                            // Deletion failed
                            errorMessageLiveData.postValue("Failed to delete account");
                            successLiveData.postValue(false);
                        }
                    } else {
                        // Password is incorrect
                        errorMessageLiveData.postValue("Password is incorrect");
                        successLiveData.postValue(false);
                    }

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error deleting account: " + e.getMessage());
                    successLiveData.postValue(false);
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();

        return successLiveData;
    }

    /**
     * Load app settings from SharedPreferences
     */
    private void loadSettings() {
        // Get dark mode setting
        boolean darkMode = sharedPreferences.getBoolean(PREF_DARK_MODE, false);
        darkModeLiveData.setValue(darkMode);

        // Get currency setting
        String currency = sharedPreferences.getString(PREF_CURRENCY, "$");
        currencyLiveData.setValue(currency);

        // Get notification setting
        boolean notificationEnabled = sharedPreferences.getBoolean(PREF_NOTIFICATION, true);
        notificationEnabledLiveData.setValue(notificationEnabled);
    }

    /**
     * Set dark mode preference
     * @param enabled Whether dark mode is enabled
     */
    public void setDarkMode(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_DARK_MODE, enabled);
        editor.apply();

        darkModeLiveData.setValue(enabled);
    }

    /**
     * Set currency preference
     * @param currency Currency symbol
     */
    public void setCurrency(String currency) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_CURRENCY, currency);
        editor.apply();

        currencyLiveData.setValue(currency);
    }

    /**
     * Set notification preference
     * @param enabled Whether notifications are enabled
     */
    public void setNotificationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_NOTIFICATION, enabled);
        editor.apply();

        notificationEnabledLiveData.setValue(enabled);
    }

    /**
     * Clear all user data for testing/development
     * @return LiveData with success state
     */
    public LiveData<Boolean> clearAllData() {
        MutableLiveData<Boolean> successLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = getApplication().getApplicationContext();
                    context.deleteDatabase("expense_tracker.db");

                    // End session
                    sessionManager.endSession();

                    successLiveData.postValue(true);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error clearing data: " + e.getMessage());
                    successLiveData.postValue(false);
                }
            }
        }).start();

        return successLiveData;
    }

    // Getters for LiveData
    public LiveData<User> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> getDarkModeLiveData() {
        return darkModeLiveData;
    }

    public LiveData<String> getCurrencyLiveData() {
        return currencyLiveData;
    }

    public LiveData<Boolean> getNotificationEnabledLiveData() {
        return notificationEnabledLiveData;
    }
}
