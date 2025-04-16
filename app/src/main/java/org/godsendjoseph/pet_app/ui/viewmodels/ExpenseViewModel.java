package org.godsendjoseph.pet_app.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.database.ExpenseDAO;
import org.godsendjoseph.pet_app.models.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for expense-related data.
 * Handles loading, filtering, and operations on expenses.
 */
public class ExpenseViewModel extends AndroidViewModel {
    private ExpenseDAO expenseDAO;
    private AuthManager authManager;

    private MutableLiveData<List<Expense>> expenseListLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        expenseDAO = new ExpenseDAO(application);
        authManager = AuthManager.getInstance(application);

        // Initialize with empty list
        expenseListLiveData.setValue(new ArrayList<>());
    }

    /**
     * Load all expenses for the current user
     */
    public void loadExpenses() {
        isLoadingLiveData.setValue(true);

        // Use a background thread for database operations
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Expense> expenses = expenseDAO.getExpensesByUserId(userId);

                    // Update LiveData on main thread
                    expenseListLiveData.postValue(expenses);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading expenses: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load expenses filtered by category
     * @param categoryId Category ID to filter by
     */
    public void loadExpensesByCategory(int categoryId) {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Expense> expenses = expenseDAO.getExpensesByCategory(userId, categoryId);

                    expenseListLiveData.postValue(expenses);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading expenses: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load expenses filtered by date range
     * @param startDate Start date (YYYY-MM-DD)
     * @param endDate End date (YYYY-MM-DD)
     */
    public void loadExpensesByDateRange(String startDate, String endDate) {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Expense> expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

                    expenseListLiveData.postValue(expenses);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading expenses: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load expenses filtered by location
     * @param location Location to filter by
     */
    public void loadExpensesByLocation(String location) {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Expense> expenses = expenseDAO.getExpensesByLocation(userId, location);

                    expenseListLiveData.postValue(expenses);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading expenses: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load expenses with both category and date range filters
     * @param categoryId Category ID to filter by
     * @param startDate Start date (YYYY-MM-DD)
     * @param endDate End date (YYYY-MM-DD)
     */
    public void loadExpensesByCategoryAndDateRange(int categoryId, String startDate, String endDate) {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Expense> categoryExpenses = expenseDAO.getExpensesByCategory(userId, categoryId);
                    List<Expense> dateRangeExpenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

                    // Find common expenses (both filters)
                    List<Expense> filteredExpenses = new ArrayList<>();
                    for (Expense expense : categoryExpenses) {
                        if (containsExpense(dateRangeExpenses, expense.getId())) {
                            filteredExpenses.add(expense);
                        }
                    }

                    expenseListLiveData.postValue(filteredExpenses);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading expenses: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Delete an expense
     * @param expenseId ID of the expense to delete
     */
    public void deleteExpense(int expenseId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int result = expenseDAO.deleteExpense(expenseId);

                    if (result > 0) {
                        // Refresh the expense list
                        loadExpenses();
                    } else {
                        errorMessageLiveData.postValue("Failed to delete expense");
                    }
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error deleting expense: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Get an expense by ID
     * @param expenseId ID of the expense to retrieve
     * @return The expense object
     */
    public Expense getExpenseById(int expenseId) {
        // A proper implementation would use a LiveData return type
        // but for simplicity in our fragment, we'll use a direct method
        return expenseDAO.getExpenseById(expenseId);
    }

    /**
     * Get the current user ID
     * @return The ID of the current user
     */
    public int getCurrentUserId() {
        return authManager.getCurrentUserId();
    }

    /**
     * Update an existing expense
     * @param expense The expense object to update
     * @return Number of rows affected (1 if successful)
     */
    public int updateExpense(Expense expense) {
        // A proper implementation would use a background thread
        // but we'll return the value directly since the fragment handles threading
        return expenseDAO.updateExpense(expense);
    }

    /**
     * Insert a new expense
     * @param expense The expense object to insert
     * @return The ID of the newly inserted expense, or -1 if failed
     */
    public long insertExpense(Expense expense) {
        // A proper implementation would use a background thread
        // but we'll return the value directly since the fragment handles threading
        return expenseDAO.insertExpense(expense);
    }

    /**
     * Get the total amount of all expenses
     * @return LiveData with the total amount
     */
    public LiveData<Double> getTotalExpenseAmount() {
        MutableLiveData<Double> totalLiveData = new MutableLiveData<>(0.0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    double total = expenseDAO.getTotalExpenses(userId);
                    totalLiveData.postValue(total);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error calculating total: " + e.getMessage());
                }
            }
        }).start();

        return totalLiveData;
    }

    /**
     * Helper method to check if an expense ID exists in a list
     */
    private boolean containsExpense(List<Expense> expenses, int expenseId) {
        for (Expense expense : expenses) {
            if (expense.getId() == expenseId) {
                return true;
            }
        }
        return false;
    }

    // Getters for LiveData
    public LiveData<List<Expense>> getExpenseListLiveData() {
        return expenseListLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
