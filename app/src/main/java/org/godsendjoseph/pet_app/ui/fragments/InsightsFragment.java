package org.godsendjoseph.pet_app.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
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
import org.godsendjoseph.pet_app.models.ExpenseSummary;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseSummaryAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.InsightsViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment for displaying insights and analytics about expenses.
 */
public class InsightsFragment extends Fragment {

    private InsightsViewModel viewModel;

    // Views
    private TextView tvStartDate;
    private TextView tvEndDate;
    private Button btnApplyDateFilter;
    private RecyclerView rvCategorySummary;
    private RecyclerView rvLocationSummary;
    private RecyclerView rvMonthSummary;
    private TextView tvAverageDailyExpense;
    private TextView tvMaxExpense;
    private TextView tvMostExpensiveCategory;
    private ProgressBar progressBar;
    private View viewChartContainer;

    // Adapters
    private ExpenseSummaryAdapter categorySummaryAdapter;
    private ExpenseSummaryAdapter locationSummaryAdapter;
    private ExpenseSummaryAdapter monthSummaryAdapter;

    // Date filter
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(InsightsViewModel.class);

        // Setup calendars for date filtering
        setupCalendars();

        // Setup adapters
        setupAdapters();

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();

        // Load initial data (last 30 days by default)
        loadInsightsWithDateRange();
    }

    private void initViews(View view) {
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        btnApplyDateFilter = view.findViewById(R.id.btn_apply_date_filter);
        rvCategorySummary = view.findViewById(R.id.rv_category_summary);
        rvLocationSummary = view.findViewById(R.id.rv_location_summary);
        rvMonthSummary = view.findViewById(R.id.rv_month_summary);
        tvAverageDailyExpense = view.findViewById(R.id.tv_average_daily_expense);
        tvMaxExpense = view.findViewById(R.id.tv_max_expense);
        tvMostExpensiveCategory = view.findViewById(R.id.tv_most_expensive_category);
        progressBar = view.findViewById(R.id.progress_bar);
        viewChartContainer = view.findViewById(R.id.chart_container);
    }

    private void setupCalendars() {
        // Initialize calendars for date filtering
        endDateCalendar = Calendar.getInstance(); // Today

        startDateCalendar = Calendar.getInstance();
        startDateCalendar.add(Calendar.MONTH, -1); // 1 month ago

        // Update date display
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        tvEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
    }

    private void setupAdapters() {
        // Set up category summary adapter
        categorySummaryAdapter = new ExpenseSummaryAdapter(requireContext(), new ArrayList<>());
        rvCategorySummary.setAdapter(categorySummaryAdapter);
        rvCategorySummary.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up location summary adapter
        locationSummaryAdapter = new ExpenseSummaryAdapter(requireContext(), new ArrayList<>());
        rvLocationSummary.setAdapter(locationSummaryAdapter);
        rvLocationSummary.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up month summary adapter
        monthSummaryAdapter = new ExpenseSummaryAdapter(requireContext(), new ArrayList<>());
        rvMonthSummary.setAdapter(monthSummaryAdapter);
        rvMonthSummary.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupListeners() {
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

        btnApplyDateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInsightsWithDateRange();
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
                                updateDateDisplay();
                            } else {
                                updateDateDisplay();
                            }
                        } else {
                            endDateCalendar.set(Calendar.YEAR, year);
                            endDateCalendar.set(Calendar.MONTH, month);
                            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            // Ensure end date is not before start date
                            if (endDateCalendar.before(startDateCalendar)) {
                                startDateCalendar.setTime(endDateCalendar.getTime());
                            }
                            updateDateDisplay();
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void observeViewModel() {
        // Observe category summary
        viewModel.getCategorySummaryLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExpenseSummary>>() {
            @Override
            public void onChanged(List<ExpenseSummary> summaries) {
                categorySummaryAdapter.updateSummaryList(summaries);
            }
        });

        // Observe location summary
        viewModel.getLocationSummaryLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExpenseSummary>>() {
            @Override
            public void onChanged(List<ExpenseSummary> summaries) {
                locationSummaryAdapter.updateSummaryList(summaries);
            }
        });

        // Observe month summary
        viewModel.getMonthSummaryLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExpenseSummary>>() {
            @Override
            public void onChanged(List<ExpenseSummary> summaries) {
                monthSummaryAdapter.updateSummaryList(summaries);
            }
        });

        // Observe average daily expense
        viewModel.getAverageDailyExpenseLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double average) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                tvAverageDailyExpense.setText(currencyFormat.format(average));
            }
        });

        // Observe max expense
        viewModel.getMaxExpenseLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double max) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                tvMaxExpense.setText(currencyFormat.format(max));
            }
        });

        // Observe most expensive category
        viewModel.getMostExpensiveCategoryLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String category) {
                tvMostExpensiveCategory.setText(category);
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

        // Observe pie chart data
        viewModel.getPieChartData().observe(getViewLifecycleOwner(), new Observer<Map<String, Double>>() {
            @Override
            public void onChanged(Map<String, Double> chartData) {
                // Update pie chart
                updatePieChart(chartData);
            }
        });

        // Observe bar chart data
        viewModel.getBarChartData().observe(getViewLifecycleOwner(), new Observer<Map<String, Double>>() {
            @Override
            public void onChanged(Map<String, Double> chartData) {
                // Update bar chart
                updateBarChart(chartData);
            }
        });
    }

    private void loadInsightsWithDateRange() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(startDateCalendar.getTime());
        String endDate = dateFormat.format(endDateCalendar.getTime());

        viewModel.loadInsightsData(startDate, endDate);
    }

    private void updatePieChart(Map<String, Double> chartData) {
        // In a real app, you would use a charting library like MPAndroidChart
        // For this prototype, we'll simulate chart display

        // Example code for when we add a charting library:
        /*
        PieChart pieChart = view.findViewById(R.id.pie_chart);
        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : chartData.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses by Category");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
        */

        // Show/hide chart container based on data availability
        viewChartContainer.setVisibility(chartData.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateBarChart(Map<String, Double> chartData) {
        // In a real app, you would use a charting library like MPAndroidChart
        // For this prototype, we'll simulate chart display

        // Example code for when we add a charting library:
        /*
        BarChart barChart = view.findViewById(R.id.bar_chart);
        List<BarEntry> entries = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Double> entry : chartData.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue().floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
        */
    }
}