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
 * ViewModel for insights and analytics screens.
 * Handles data processing for charts and statistics.
 */
public class InsightsViewModel extends AndroidViewModel {
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private AuthManager authManager;

    private MutableLiveData<List<ExpenseSummary>> categorySummaryLiveData = new MutableLiveData<>();
    private MutableLiveData<List<ExpenseSummary>> monthSummaryLiveData = new MutableLiveData<>();
    private MutableLiveData<List<ExpenseSummary>> locationSummaryLiveData = new MutableLiveData<>();
    private MutableLiveData<Double> averageDailyExpenseLiveData = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> maxExpenseLiveData = new MutableLiveData<>(0.0);
    private MutableLiveData<String> mostExpensiveCategoryLiveData = new MutableLiveData<>("");
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public InsightsViewModel(@NonNull Application application) {
        super(application);
        expenseDAO = new ExpenseDAO(application);
        categoryDAO = new CategoryDAO(application);
        authManager = AuthManager.getInstance(application);

        // Initialize with empty lists
        categorySummaryLiveData.setValue(new ArrayList<>());
        monthSummaryLiveData.setValue(new ArrayList<>());
        locationSummaryLiveData.setValue(new ArrayList<>());
    }

    /**
     * Load all insights data with date range filter
     * @param startDate Start date (YYYY-MM-DD)
     * @param endDate End date (YYYY-MM-DD)
     */
    public void loadInsightsData(String startDate, String endDate) {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();

                    // Get expenses within date range
                    List<Expense> expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

                    // Calculate total for percentage calculations
                    double total = calculateTotal(expenses);

                    // Generate category summary
                    generateCategorySummary(userId, expenses, total);

                    // Generate month summary
                    generateMonthSummary(expenses, total);

                    // Generate location summary
                    generateLocationSummary(expenses, total);

                    // Calculate statistics
                    calculateStatistics(expenses, startDate, endDate);

                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading insights: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Calculate total amount from a list of expenses
     */
    private double calculateTotal(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    /**
     * Generate summary of expenses by category
     */
    private void generateCategorySummary(int userId, List<Expense> expenses, double total) {
        try {
            Map<Integer, ExpenseSummary> summaryMap = new HashMap<>();
            List<Category> allCategories = categoryDAO.getAllCategories(userId);
            Map<Integer, Category> categoryMap = new HashMap<>();

            // Create map of category ID to Category object
            for (Category category : allCategories) {
                categoryMap.put(category.getId(), category);
            }

            // Group expenses by category
            for (Expense expense : expenses) {
                int categoryId = expense.getCategoryId();
                Category category = categoryMap.get(categoryId);

                if (category != null) {
                    ExpenseSummary summary = summaryMap.get(categoryId);

                    if (summary == null) {
                        double percentage = (total > 0) ? (expense.getAmount() / total) * 100 : 0;
                        summary = new ExpenseSummary(category.getName(), expense.getAmount(),
                                percentage, category.getColor(), 1);
                    } else {
                        double newAmount = summary.getAmount() + expense.getAmount();
                        double percentage = (total > 0) ? (newAmount / total) * 100 : 0;
                        summary.setAmount(newAmount);
                        summary.setPercentage(percentage);
                        summary.setCount(summary.getCount() + 1);
                    }

                    summaryMap.put(categoryId, summary);
                }
            }

            // Convert map to list
            List<ExpenseSummary> summaryList = new ArrayList<>(summaryMap.values());
            categorySummaryLiveData.postValue(summaryList);
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error generating category summary: " + e.getMessage());
        }
    }

    /**
     * Generate summary of expenses by month
     */
    private void generateMonthSummary(List<Expense> expenses, double total) {
        try {
            Map<String, ExpenseSummary> summaryMap = new HashMap<>();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

            // Group expenses by month
            for (Expense expense : expenses) {
                try {
                    // Parse date to get month
                    String[] dateParts = expense.getDate().split("-");
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);

                    String monthKey = monthFormat.format(calendar.getTime());

                    ExpenseSummary summary = summaryMap.get(monthKey);

                    if (summary == null) {
                        double percentage = (total > 0) ? (expense.getAmount() / total) * 100 : 0;
                        summary = new ExpenseSummary(monthKey, expense.getAmount(),
                                percentage, "#3F51B5", 1);
                    } else {
                        double newAmount = summary.getAmount() + expense.getAmount();
                        double percentage = (total > 0) ? (newAmount / total) * 100 : 0;
                        summary.setAmount(newAmount);
                        summary.setPercentage(percentage);
                        summary.setCount(summary.getCount() + 1);
                    }

                    summaryMap.put(monthKey, summary);
                } catch (Exception e) {
                    // Skip this expense if date parsing fails
                    continue;
                }
            }

            // Convert map to list
            List<ExpenseSummary> summaryList = new ArrayList<>(summaryMap.values());
            monthSummaryLiveData.postValue(summaryList);
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error generating month summary: " + e.getMessage());
        }
    }

    /**
     * Generate summary of expenses by location
     */
    private void generateLocationSummary(List<Expense> expenses, double total) {
        try {
            Map<String, ExpenseSummary> summaryMap = new HashMap<>();

            // Group expenses by location
            for (Expense expense : expenses) {
                String location = expense.getLocation();

                if (location != null && !location.isEmpty()) {
                    ExpenseSummary summary = summaryMap.get(location);

                    if (summary == null) {
                        double percentage = (total > 0) ? (expense.getAmount() / total) * 100 : 0;
                        summary = new ExpenseSummary(location, expense.getAmount(),
                                percentage, "#FF9800", 1);
                    } else {
                        double newAmount = summary.getAmount() + expense.getAmount();
                        double percentage = (total > 0) ? (newAmount / total) * 100 : 0;
                        summary.setAmount(newAmount);
                        summary.setPercentage(percentage);
                        summary.setCount(summary.getCount() + 1);
                    }

                    summaryMap.put(location, summary);
                }
            }

            // Convert map to list
            List<ExpenseSummary> summaryList = new ArrayList<>(summaryMap.values());
            locationSummaryLiveData.postValue(summaryList);
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error generating location summary: " + e.getMessage());
        }
    }

    /**
     * Calculate various statistics from expenses
     */
    private void calculateStatistics(List<Expense> expenses, String startDate, String endDate) {
        try {
            // Calculate max expense
            double maxExpense = 0;

            // Find most expensive category
            Map<Integer, Double> categoryTotalMap = new HashMap<>();
            int userId = authManager.getCurrentUserId();
            List<Category> allCategories = categoryDAO.getAllCategories(userId);
            Map<Integer, Category> categoryMap = new HashMap<>();

            // Create map of category ID to Category object
            for (Category category : allCategories) {
                categoryMap.put(category.getId(), category);
            }

            for (Expense expense : expenses) {
                // Update max expense
                if (expense.getAmount() > maxExpense) {
                    maxExpense = expense.getAmount();
                }

                // Update category totals
                int categoryId = expense.getCategoryId();
                Double categoryTotal = categoryTotalMap.get(categoryId);
                if (categoryTotal == null) {
                    categoryTotalMap.put(categoryId, expense.getAmount());
                } else {
                    categoryTotalMap.put(categoryId, categoryTotal + expense.getAmount());
                }
            }

            // Find most expensive category
            int mostExpensiveCategoryId = -1;
            double highestCategoryTotal = 0;

            for (Map.Entry<Integer, Double> entry : categoryTotalMap.entrySet()) {
                if (entry.getValue() > highestCategoryTotal) {
                    highestCategoryTotal = entry.getValue();
                    mostExpensiveCategoryId = entry.getKey();
                }
            }

            // Get category name
            String mostExpensiveCategory = "";
            if (mostExpensiveCategoryId != -1) {
                Category category = categoryMap.get(mostExpensiveCategoryId);
                if (category != null) {
                    mostExpensiveCategory = category.getName();
                }
            }

            // Calculate average daily expense
            double averageDailyExpense = 0;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(dateFormat.parse(startDate));

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(dateFormat.parse(endDate));

                // Calculate days between start and end dates
                long diffMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
                int diffDays = (int) (diffMillis / (24 * 60 * 60 * 1000)) + 1; // +1 to include both start and end dates

                if (diffDays > 0) {
                    double total = calculateTotal(expenses);
                    averageDailyExpense = total / diffDays;
                }
            } catch (Exception e) {
                errorMessageLiveData.postValue("Error calculating date range: " + e.getMessage());
            }

            // Update LiveData
            maxExpenseLiveData.postValue(maxExpense);
            mostExpensiveCategoryLiveData.postValue(mostExpensiveCategory);
            averageDailyExpenseLiveData.postValue(averageDailyExpense);
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error calculating statistics: " + e.getMessage());
        }
    }

    /**
     * Get data for pie chart (by category)
     */
    public LiveData<Map<String, Double>> getPieChartData() {
        MutableLiveData<Map<String, Double>> chartDataLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ExpenseSummary> categorySummary = categorySummaryLiveData.getValue();
                    Map<String, Double> chartData = new HashMap<>();

                    if (categorySummary != null) {
                        for (ExpenseSummary summary : categorySummary) {
                            chartData.put(summary.getCategory(), summary.getAmount());
                        }
                    }

                    chartDataLiveData.postValue(chartData);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error generating pie chart data: " + e.getMessage());
                    chartDataLiveData.postValue(new HashMap<>());
                }
            }
        }).start();

        return chartDataLiveData;
    }

    /**
     * Get data for bar chart (by month)
     */
    public LiveData<Map<String, Double>> getBarChartData() {
        MutableLiveData<Map<String, Double>> chartDataLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ExpenseSummary> monthSummary = monthSummaryLiveData.getValue();
                    Map<String, Double> chartData = new HashMap<>();

                    if (monthSummary != null) {
                        for (ExpenseSummary summary : monthSummary) {
                            chartData.put(summary.getCategory(), summary.getAmount());
                        }
                    }

                    chartDataLiveData.postValue(chartData);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error generating bar chart data: " + e.getMessage());
                    chartDataLiveData.postValue(new HashMap<>());
                }
            }
        }).start();

        return chartDataLiveData;
    }

    // Getters for LiveData
    public LiveData<List<ExpenseSummary>> getCategorySummaryLiveData() {
        return categorySummaryLiveData;
    }

    public LiveData<List<ExpenseSummary>> getMonthSummaryLiveData() {
        return monthSummaryLiveData;
    }

    public LiveData<List<ExpenseSummary>> getLocationSummaryLiveData() {
        return locationSummaryLiveData;
    }

    public LiveData<Double> getAverageDailyExpenseLiveData() {
        return averageDailyExpenseLiveData;
    }

    public LiveData<Double> getMaxExpenseLiveData() {
        return maxExpenseLiveData;
    }

    public LiveData<String> getMostExpensiveCategoryLiveData() {
        return mostExpensiveCategoryLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }
}
