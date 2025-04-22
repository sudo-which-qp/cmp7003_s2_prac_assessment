package org.godsendjoseph.pet_app.ui.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.SessionManager;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.database.ExpenseDAO;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class ExpenseFormActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etAmount;
    private EditText etDate;
    private EditText etTime;
    private AutoCompleteTextView actvLocation;
    private Spinner spinnerCategory;
    private EditText etNotes;
    private Button btnSave;

    private AuthManager authManager;
    private SessionManager sessionManager;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;

    private Calendar calendar;
    private int expenseId = -1; // -1 for new expense, otherwise editing existing expense
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_form);

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

        // Get expense ID from intent (if editing)
        if (getIntent().hasExtra("expense_id")) {
            expenseId = getIntent().getIntExtra("expense_id", -1);
            setTitle("Edit Expense");
        } else {
            setTitle("Add Expense");
        }

        // Setup date and time pickers
        calendar = Calendar.getInstance();

        // Initialize views
        initViews();

        setupDateTimePickers();


        // Load categories
        loadCategories();

        // Load expense data if editing
        if (expenseId != -1) {
            loadExpenseData();
        }

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_expense_title);
        etAmount = findViewById(R.id.et_expense_amount);
        etDate = findViewById(R.id.et_expense_date);
        etTime = findViewById(R.id.et_expense_time);
        actvLocation = findViewById(R.id.actv_expense_location);
        spinnerCategory = findViewById(R.id.spinner_expense_category);
        etNotes = findViewById(R.id.et_expense_notes);
        btnSave = findViewById(R.id.btn_save_expense);

        // Set current date and time
        updateDateTimeFields();

        // Setup location autocomplete
        setupLocationAutocomplete();
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateTimeFields();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateDateTimeFields();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeFields() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        etDate.setText(dateFormat.format(calendar.getTime()));
        etTime.setText(timeFormat.format(calendar.getTime()));
    }

    private void setupLocationAutocomplete() {
        // For a simple implementation, we'll use a predefined list of locations
        // In a real app, you might want to use a Google Places API or similar
        String[] locations = {
                "Home", "Office", "Supermarket", "Restaurant", "Gas Station",
                "Shopping Mall", "Gym", "Pharmacy", "Coffee Shop", "Online"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, locations);
        actvLocation.setAdapter(adapter);
        actvLocation.setThreshold(1); // Start showing suggestions after first character
    }

    private void loadCategories() {
        // Load both default and user-defined categories
        int userId = authManager.getCurrentUserId();
        categories = categoryDAO.getAllCategories(userId);

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void loadExpenseData() {
        Expense expense = expenseDAO.getExpenseById(expenseId);
        if (expense != null) {
            etTitle.setText(expense.getTitle());
            etAmount.setText(String.valueOf(expense.getAmount()));
            etDate.setText(expense.getDate());
            etTime.setText(expense.getTime());
            actvLocation.setText(expense.getLocation());
            etNotes.setText(expense.getNotes());

            // Set the category spinner selection
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == expense.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            // Parse date and time to set calendar
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                calendar.setTime(dateFormat.parse(expense.getDate()));

                String[] timeParts = expense.getTime().split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });
    }

    private void saveExpense() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get values
        String title = etTitle.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        int categoryId = categories.get(categoryPosition).getId();

        int userId = authManager.getCurrentUserId();

        // Create or update expense
        if (expenseId == -1) {
            // New expense
            Expense expense = new Expense(userId, title, amount, date, time, location, categoryId, notes);
            long result = expenseDAO.insertExpense(expense);

            if (result > 0) {
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update existing expense
            Expense expense = new Expense(expenseId, userId, title, amount, date, time, location, categoryId, notes, "");
            int result = expenseDAO.updateExpense(expense);

            if (result > 0) {
                Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update expense", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        String title = etTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Amount is required");
            etAmount.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than zero");
                etAmount.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Please enter a valid amount");
            etAmount.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(location)) {
            actvLocation.setError("Location is required");
            actvLocation.requestFocus();
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}