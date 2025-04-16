package org.godsendjoseph.pet_app.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.database.ExpenseDAO;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;
import org.godsendjoseph.pet_app.models.ExpenseSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ViewModel for dashboard-related data.
 * Handles loading summary and statistics for the dashboard.
 */
public class DashboardViewModel extends AndroidViewModel {
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private AuthManager authManager;

    private MutableLiveData<List<ExpenseSummary>> categorySummaryLiveData = new MutableLiveData<>();
    private MutableLiveData<Double> totalExpensesLiveData = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> monthlyExpensesLiveData = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> weeklyExpensesLiveData = new MutableLiveData<>(0.0);
    private MutableLiveData<List<Expense>> recentExpensesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        expenseDAO = new ExpenseDAO(application);
        categoryDAO = new CategoryDAO(application);
        authManager = AuthManager.getInstance(application);

        // Initialize with empty data
        categorySummaryLiveData.setValue(new ArrayList<>());
        recentExpensesLiveData.setValue(new ArrayList<>());
    }

    /**
     * Load all dashboard data
     */
    public void loadDashboardData() {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();

                    // Load total expenses
                    double totalExpenses = expenseDAO.getTotalExpenses(userId);
                    totalExpensesLiveData.postValue(totalExpenses);

                    // Load current month expenses
                    double monthlyExpenses = getMonthlyExpenses(userId);
                    monthlyExpensesLiveData.postValue(monthlyExpenses);

                    // Load current week expenses
                    double weeklyExpenses = getWeeklyExpenses(userId);
                    weeklyExpensesLiveData.postValue(weeklyExpenses);

                    // Load recent expenses (last 5)
                    List<Expense> recentExpenses = getRecentExpenses(userId, 5);
                    recentExpensesLiveData.postValue(recentExpenses);

                    // Load category summary
                    List<ExpenseSummary> categorySummary = getCategorySummary(userId, totalExpenses);
                    categorySummaryLiveData.postValue(categorySummary);

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading dashboard data: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Get expenses for the current month
     * @param userId User ID
     * @return Total amount for the current month
     */
    private double getMonthlyExpenses(int userId) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1); // First day of month
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(calendar.getTime());

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Last day of month
            String endDate = dateFormat.format(calendar.getTime());

            List<Expense> expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

            double total = 0;
            for (Expense expense : expenses) {
                total += expense.getAmount();
            }

            return total;
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error calculating monthly expenses: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get expenses for the current week
     * @param userId User ID
     * @return Total amount for the current week
     */
    private double getWeeklyExpenses(int userId) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // First day of week
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(calendar.getTime());

            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Last day of week
            String endDate = dateFormat.format(calendar.getTime());

            List<Expense> expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

            double total = 0;
            for (Expense expense : expenses) {
                total += expense.getAmount();
            }

            return total;
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error calculating weekly expenses: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get the most recent expenses
     * @param userId User ID
     * @param limit Maximum number of expenses to return
     * @return List of recent expenses
     */
    private List<Expense> getRecentExpenses(int userId, int limit) {
        try {
            List<Expense> allExpenses = expenseDAO.getExpensesByUserId(userId);
            List<Expense> recentExpenses = new ArrayList<>();

            // Add up to limit expenses to the result list
            for (int i = 0; i < Math.min(limit, allExpenses.size()); i++) {
                recentExpenses.add(allExpenses.get(i));
            }

            return recentExpenses;
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error getting recent expenses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get expense summary by category
     * @param userId User ID
     * @param totalExpenses Total expense amount for percentage calculation
     * @return List of expense summaries by category
     */
    private List<ExpenseSummary> getCategorySummary(int userId, double totalExpenses) {
        try {
            List<Category> categories = categoryDAO.getAllCategories(userId);
            List<ExpenseSummary> summarList = new ArrayList<>();

            for (Category category : categories) {
                double amount = expenseDAO.getTotalExpensesByCategory(userId, category.getId());

                if (amount > 0) {
                    double percentage = (totalExpenses > 0) ? (amount / totalExpenses) * 100 : 0;

                    ExpenseSummary summary = new ExpenseSummary(
                            category.getName(),
                            amount,
                            percentage,
                            category.getColor()
                    );

                    summarList.add(summary);
                }
            }

            return summarList;
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error generating category summary: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get monthly expense data for a chart
     * @param months Number of months to include (e.g., 6 for last 6 months)
     * @return LiveData with a map of month names to expense amounts
     */
    public LiveData<Map<String, Double>> getMonthlyExpenseData(int months) {
        MutableLiveData<Map<String, Double>> dataLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    Map<String, Double> monthlyData = new HashMap<>();
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    Calendar calendar = Calendar.getInstance();

                    // Go back to start with the oldest month
                    calendar.add(Calendar.MONTH, -(months - 1));

                    for (int i = 0; i < months; i++) {
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);

                        // Set to first day of month
                        calendar.set(year, month, 1);
                        String startDate = dateFormat.format(calendar.getTime());

                        // Set to last day of month
                        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        String endDate = dateFormat.format(calendar.getTime());

                        List<Expense> expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

                        double total = 0;
                        for (Expense expense : expenses) {
                            total += expense.getAmount();
                        }

                        String monthName = monthFormat.format(calendar.getTime());
                        monthlyData.put(monthName, total);

                        // Move to next month
                        calendar.add(Calendar.MONTH, 1);
                    }

                    dataLiveData.postValue(monthlyData);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error generating monthly chart data: " + e.getMessage());
                    dataLiveData.postValue(new HashMap<>());
                }
            }
        }).start();

        return dataLiveData;
    }

    // Getters for LiveData
    public LiveData<List<ExpenseSummary>> getCategorySummaryLiveData() {
        return categorySummaryLiveData;
    }

    public LiveData<Double> getTotalExpensesLiveData() {
        return totalExpensesLiveData;
    }

    public LiveData<Double> getMonthlyExpensesLiveData() {
        return monthlyExpensesLiveData;
    }

    public LiveData<Double> getWeeklyExpensesLiveData() {
        return weeklyExpensesLiveData;
    }

    public LiveData<List<Expense>> getRecentExpensesLiveData() {
        return recentExpensesLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
