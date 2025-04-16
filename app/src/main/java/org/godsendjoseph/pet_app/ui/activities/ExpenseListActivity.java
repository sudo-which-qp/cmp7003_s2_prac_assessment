package org.godsendjoseph.pet_app.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.SessionManager;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.database.ExpenseDAO;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;
import org.godsendjoseph.pet_app.ui.adapters.CategorySpinnerAdapter;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseListActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenseList;
    private TextView tvNoExpenses;
    private Spinner spinnerCategory;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private FloatingActionButton fabAddExpense;

    private AuthManager authManager;
    private SessionManager sessionManager;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;

    private List<Category> categories;
    private int selectedCategoryId = -1; // -1 means all categories
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private boolean isDateFilterActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        // Initialize managers and DAOs
        authManager = AuthManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        expenseDAO = new ExpenseDAO(this);
        categoryDAO = new CategoryDAO(this);

        // Check session validity
        if (!sessionManager.checkSessionAndRedirect()) {
            return;
        }

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();
        setupCalendars();
        loadCategories();

        // Setup listeners
        setupListeners();

        // Initial data load
        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh expenses list on resume
        if (sessionManager.isSessionValid()) {
            sessionManager.refreshSession();
            loadExpenses();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_expenses);
        tvNoExpenses = findViewById(R.id.tv_no_expenses);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(this, expenseList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupCalendars() {
        // Initialize calendars for date filtering
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        // Set start date to first day of current month
        startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);

        // Update date display
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        tvEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void loadCategories() {
        int userId = authManager.getCurrentUserId();

        // Get all categories (including default ones)
        categories = new ArrayList<>();

        // Add "All Categories" option
        Category allCategory = new Category();
        allCategory.setId(-1);
        allCategory.setName("All Categories");
        categories.add(allCategory);

        // Add user's categories
        categories.addAll(categoryDAO.getAllCategories(userId));

        // Setup category spinner
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupListeners() {
        fabAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExpenseListActivity.this, ExpenseFormActivity.class);
                startActivity(intent);
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categories.get(position).getId();
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1;
                loadExpenses();
            }
        });

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(true);
            }
        });

        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(false);
            }
        });
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (isStartDate) {
                            startDateCalendar.set(Calendar.YEAR, year);
                            startDateCalendar.set(Calendar.MONTH, month);
                            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Ensure start date is not after end date
                            if (startDateCalendar.after(endDateCalendar)) {
                                endDateCalendar.setTime(startDateCalendar.getTime());
                            }
                        } else {
                            endDateCalendar.set(Calendar.YEAR, year);
                            endDateCalendar.set(Calendar.MONTH, month);
                            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Ensure end date is not before start date
                            if (endDateCalendar.before(startDateCalendar)) {
                                startDateCalendar.setTime(endDateCalendar.getTime());
                            }
                        }

                        updateDateDisplay();
                        isDateFilterActive = true;
                        loadExpenses();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadExpenses() {
        int userId = authManager.getCurrentUserId();
        List<Expense> expenses;

        // Apply filters
        if (selectedCategoryId != -1 && isDateFilterActive) {
            // Filter by category and date range
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());

            expenses = filterExpensesByCategoryAndDate(userId, selectedCategoryId, startDate, endDate);
        } else if (selectedCategoryId != -1) {
            // Filter by category only
            expenses = expenseDAO.getExpensesByCategory(userId, selectedCategoryId);
        } else if (isDateFilterActive) {
            // Filter by date range only
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());

            expenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);
        } else {
            // No filters
            expenses = expenseDAO.getExpensesByUserId(userId);
        }

        // Update the list
        expenseList.clear();
        if (expenses != null && !expenses.isEmpty()) {
            expenseList.addAll(expenses);
            recyclerView.setVisibility(View.VISIBLE);
            tvNoExpenses.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            tvNoExpenses.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private List<Expense> filterExpensesByCategoryAndDate(int userId, int categoryId, String startDate, String endDate) {
        // This is a custom query that combines category and date filters
        // In a real app with Room, you would define this in the DAO
        // For SQLite direct implementation, we need to create a custom query here

        // For now, we'll just use the existing methods and filter in memory
        List<Expense> categoryExpenses = expenseDAO.getExpensesByCategory(userId, categoryId);
        List<Expense> dateRangeExpenses = expenseDAO.getExpensesByDateRange(userId, startDate, endDate);

        // Find common expenses (those that match both filters)
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : categoryExpenses) {
            if (isExpenseInList(expense, dateRangeExpenses)) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

    private boolean isExpenseInList(Expense expense, List<Expense> expenseList) {
        for (Expense e : expenseList) {
            if (e.getId() == expense.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onExpenseClick(int position) {
        Expense expense = expenseList.get(position);
        Intent intent = new Intent(this, ExpenseFormActivity.class);
        intent.putExtra("expense_id", expense.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expense_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_filters) {
            // Clear all filters
            selectedCategoryId = -1;
            isDateFilterActive = false;

            // Reset start date to first day of current month
            startDateCalendar = Calendar.getInstance();
            startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);

            // Reset end date to today
            endDateCalendar = Calendar.getInstance();

            // Update UI
            spinnerCategory.setSelection(0);
            updateDateDisplay();
            loadExpenses();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}