package org.godsendjoseph.pet_app.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;
import org.godsendjoseph.pet_app.ui.adapters.CategorySpinnerAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.CategoryViewModel;
import org.godsendjoseph.pet_app.ui.viewmodels.ExpenseViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for adding or editing an expense.
 */
public class ExpenseFormFragment extends Fragment {

    private ExpenseViewModel expenseViewModel;
    private CategoryViewModel categoryViewModel;

    // Views
    private EditText etTitle;
    private EditText etAmount;
    private EditText etDate;
    private EditText etTime;
    private AutoCompleteTextView actvLocation;
    private Spinner spinnerCategory;
    private EditText etNotes;
    private Button btnSave;
    private ProgressBar progressBar;

    // State
    private Calendar calendar;
    private List<Category> categories = new ArrayList<>();
    private int expenseId = -1; // -1 for new expense, positive for editing

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Initialize views
        initViews(view);

        // Get expense ID from arguments if editing
        if (getArguments() != null && getArguments().containsKey("expense_id")) {
            expenseId = getArguments().getInt("expense_id", -1);
        }

        // Setup date and time pickers
        calendar = Calendar.getInstance();
        setupDateTimePickers();

        // Setup location autocomplete
        setupLocationAutocomplete();

        // Observe ViewModels
        observeViewModels();

        // Load categories
        categoryViewModel.loadAllCategories();

        // Setup save button
        setupSaveButton();
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.et_expense_title);
        etAmount = view.findViewById(R.id.et_expense_amount);
        etDate = view.findViewById(R.id.et_expense_date);
        etTime = view.findViewById(R.id.et_expense_time);
        actvLocation = view.findViewById(R.id.actv_expense_location);
        spinnerCategory = view.findViewById(R.id.spinner_expense_category);
        etNotes = view.findViewById(R.id.et_expense_notes);
        btnSave = view.findViewById(R.id.btn_save_expense);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set current date and time
        updateDateTimeFields();
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
                requireContext(),
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
                requireContext(),
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
                requireContext(), android.R.layout.simple_dropdown_item_1line, locations);
        actvLocation.setAdapter(adapter);
        actvLocation.setThreshold(1); // Start showing suggestions after first character
    }

    private void observeViewModels() {
        // Observe categories for spinner
        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categoryList) {
                categories = categoryList;

                CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(
                        requireContext(), android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                // If we're editing an expense, load it now that we have categories
                if (expenseId > 0) {
                    loadExpenseData();
                }
            }
        });

        // Observe loading state
        categoryViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                // Update loading state
            }
        });

        // Observe error messages
        categoryViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadExpenseData() {
        // If in edit mode, load the expense data
        // In a real app, you would likely have an ExpenseDetailsViewModel
        // or similar to handle this operation

        // For now, we'll simulate by getting the expense object
        // from the parent activity's ViewModel or directly from the DAO
        progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // In a real implementation, this would come from a ViewModel
                    Expense expense = expenseViewModel.getExpenseById(expenseId);

                    // Update UI on main thread
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (expense != null) {
                                // Fill form fields
                                etTitle.setText(expense.getTitle());
                                etAmount.setText(String.valueOf(expense.getAmount()));
                                etDate.setText(expense.getDate());
                                etTime.setText(expense.getTime());
                                actvLocation.setText(expense.getLocation());
                                etNotes.setText(expense.getNotes());

                                // Set spinner position
                                setCategorySpinnerSelection(expense.getCategoryId());

                                // Parse date and time to update calendar
                                updateCalendarFromExpense(expense);
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Error loading expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    private void setCategorySpinnerSelection(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void updateCalendarFromExpense(Expense expense) {
        try {
            // Parse date
            String[] dateParts = expense.getDate().split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[2]);

            // Parse time
            String[] timeParts = expense.getTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Update calendar
            calendar.set(year, month, day, hour, minute);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error parsing date/time", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    saveExpense();
                }
            }
        });
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

    private void saveExpense() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        // Get values from form
        String title = etTitle.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        int categoryId = categories.get(spinnerCategory.getSelectedItemPosition()).getId();

        // Save in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean success;

                    if (expenseId > 0) {
                        // Update existing expense
                        Expense expense = new Expense(expenseId, expenseViewModel.getCurrentUserId(),
                                title, amount, date, time, location, categoryId, notes, "");
                        int result = expenseViewModel.updateExpense(expense);
                        success = result > 0;
                    } else {
                        // Create new expense
                        Expense expense = new Expense(expenseViewModel.getCurrentUserId(),
                                title, amount, date, time, location, categoryId, notes);
                        long result = expenseViewModel.insertExpense(expense);
                        success = result > 0;
                    }

                    // Update UI on main thread
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);

                            if (success) {
                                Toast.makeText(requireContext(),
                                        expenseId > 0 ? "Expense updated" : "Expense added",
                                        Toast.LENGTH_SHORT).show();

                                // Navigate back
                                if (getActivity() != null) {
                                    getActivity().onBackPressed();
                                }
                            } else {
                                Toast.makeText(requireContext(),
                                        "Failed to save expense",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}