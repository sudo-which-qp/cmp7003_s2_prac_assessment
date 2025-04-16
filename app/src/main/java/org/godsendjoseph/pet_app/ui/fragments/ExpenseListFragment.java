package org.godsendjoseph.pet_app.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;
import org.godsendjoseph.pet_app.ui.activities.ExpenseFormActivity;
import org.godsendjoseph.pet_app.ui.adapters.CategorySpinnerAdapter;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.CategoryViewModel;
import org.godsendjoseph.pet_app.ui.viewmodels.ExpenseViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying and filtering the list of expenses.
 */
public class ExpenseListFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private ExpenseViewModel expenseViewModel;
    private CategoryViewModel categoryViewModel;

    // Views
    private RecyclerView recyclerView;
    private TextView tvNoExpenses;
    private Spinner spinnerCategory;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddExpense;

    // Adapters
    private ExpenseAdapter expenseAdapter;

    // Filter state
    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId = -1; // -1 means all categories
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private boolean isDateFilterActive = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enable options menu
        setHasOptionsMenu(true);

        // Initialize views
        initViews(view);

        // Initialize ViewModels
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Setup calendars for date filtering
        setupCalendars();

        // Setup adapters
        setupAdapters();

        // Setup listeners
        setupListeners();

        // Observe data
        observeViewModels();

        // Load data
        categoryViewModel.loadAllCategories();
        expenseViewModel.loadExpenses();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_expenses);
        tvNoExpenses = view.findViewById(R.id.tv_no_expenses);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddExpense = view.findViewById(R.id.fab_add_expense);
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

    private void setupAdapters() {
        // Setup expense adapter
        expenseAdapter = new ExpenseAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(expenseAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupListeners() {
        fabAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ExpenseFormActivity.class);
                startActivity(intent);
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categories.size() > position) {
                    selectedCategoryId = categories.get(position).getId();
                    applyFilters();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1;
                applyFilters();
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
                requireContext(),
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
                        applyFilters();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void observeViewModels() {
        // Observe categories for spinner
        categoryViewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categoryList) {
                categories.clear();

                // Add "All Categories" option
                Category allCategory = new Category();
                allCategory.setId(-1);
                allCategory.setName("All Categories");
                categories.add(allCategory);

                // Add other categories
                categories.addAll(categoryList);

                // Update spinner
                CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(
                        requireContext(), android.R.layout.simple_spinner_item, categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                // Set selection to previously selected category if exists
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == selectedCategoryId) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        });

        // Observe expenses
        expenseViewModel.getExpenseListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Expense>>() {
            @Override
            public void onChanged(List<Expense> expenses) {
                if (expenses.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoExpenses.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoExpenses.setVisibility(View.GONE);
                    expenseAdapter.updateExpenseList(expenses);
                }
            }
        });

        // Observe loading state
        expenseViewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        expenseViewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilters() {
        // Apply selected filters
        if (selectedCategoryId != -1 && isDateFilterActive) {
            // Filter by both category and date range
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());

            expenseViewModel.loadExpensesByCategoryAndDateRange(selectedCategoryId, startDate, endDate);
        } else if (selectedCategoryId != -1) {
            // Filter by category only
            expenseViewModel.loadExpensesByCategory(selectedCategoryId);
        } else if (isDateFilterActive) {
            // Filter by date range only
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = dateFormat.format(startDateCalendar.getTime());
            String endDate = dateFormat.format(endDateCalendar.getTime());

            expenseViewModel.loadExpensesByDateRange(startDate, endDate);
        } else {
            // No filters
            expenseViewModel.loadExpenses();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.expense_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_filters) {
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

            // Reload expenses without filters
            expenseViewModel.loadExpenses();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh data when returning to the fragment
        if (isDateFilterActive || selectedCategoryId != -1) {
            applyFilters();
        } else {
            expenseViewModel.loadExpenses();
        }
    }

    @Override
    public void onExpenseClick(int position) {
        // Handle expense click
        List<Expense> expenses = expenseViewModel.getExpenseListLiveData().getValue();
        if (expenses != null && position < expenses.size()) {
            Expense expense = expenses.get(position);

            // Navigate to expense form for editing
            Intent intent = new Intent(requireContext(), ExpenseFormActivity.class);
            intent.putExtra("expense_id", expense.getId());
            startActivity(intent);
        }
    }
}