package org.godsendjoseph.pet_app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import org.godsendjoseph.pet_app.models.Expense;
import org.godsendjoseph.pet_app.models.ExpenseSummary;
import org.godsendjoseph.pet_app.ui.activities.ExpenseFormActivity;
import org.godsendjoseph.pet_app.ui.activities.ExpenseListActivity;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseAdapter;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseSummaryAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.DashboardViewModel;
import org.godsendjoseph.pet_app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collections;
import java.util.Date;

public class DashboardFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private DashboardViewModel viewModel;

    // Views
    private TextView tvTotalExpenses;
    private TextView tvMonthlyExpenses;
    private TextView tvWeeklyExpenses;
    private RecyclerView rvCategorySummary;
    private RecyclerView rvRecentExpenses;
    private ProgressBar progressBar;
    private View viewChartContainer;
    private TextView tvViewAllExpenses;

    // Adapters
    private ExpenseSummaryAdapter categorySummaryAdapter;
    private ExpenseAdapter recentExpensesAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Set up adapters
        setupAdapters();

        // Setup click listeners
        setupClickListeners();

        // Observe data
        observeViewModel();

        // Load data
        // Load data with a slight delay to ensure views are ready
        view.post(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh data when returning to this fragment
        refreshData();

        // Force adapter updates
        if (categorySummaryAdapter != null && viewModel.getCategorySummaryLiveData().getValue() != null) {
            categorySummaryAdapter.updateSummaryList(viewModel.getCategorySummaryLiveData().getValue());
        }

        if (recentExpensesAdapter != null && viewModel.getRecentExpensesLiveData().getValue() != null) {
            recentExpensesAdapter.updateExpenseList(viewModel.getRecentExpensesLiveData().getValue());
        }

        // Request layout refresh
        if (rvCategorySummary != null) rvCategorySummary.requestLayout();
        if (rvRecentExpenses != null) rvRecentExpenses.requestLayout();
    }

    /**
     * Refresh all dashboard data
     */
    public void refreshData() {
        viewModel.loadDashboardData();
    }

    private void initViews(View view) {
        tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);
        tvMonthlyExpenses = view.findViewById(R.id.tv_monthly_expenses);
        tvWeeklyExpenses = view.findViewById(R.id.tv_weekly_expenses);
        rvCategorySummary = view.findViewById(R.id.rv_category_summary);
        rvRecentExpenses = view.findViewById(R.id.rv_recent_expenses);
        progressBar = view.findViewById(R.id.progress_bar);
        viewChartContainer = view.findViewById(R.id.chart_container);
        tvViewAllExpenses = view.findViewById(R.id.tv_view_all_expenses);
    }

    private void setupAdapters() {
        // Set up category summary adapter
        categorySummaryAdapter = new ExpenseSummaryAdapter(requireContext(), new ArrayList<>());
        rvCategorySummary.setAdapter(categorySummaryAdapter);
        rvCategorySummary.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Set up recent expenses adapter
        recentExpensesAdapter = new ExpenseAdapter(requireContext(), new ArrayList<>(), this);
        rvRecentExpenses.setAdapter(recentExpensesAdapter);
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupClickListeners() {
        // Setup "View All" click listener for expenses
        if (tvViewAllExpenses != null) {
            tvViewAllExpenses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to the expense list activity/fragment
                    // This depends on your navigation structure
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), ExpenseListActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    private void observeViewModel() {
        // Observe total expenses
        viewModel.getTotalExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double total) {
                // Use CurrencyUtils for consistent currency formatting
                tvTotalExpenses.setText(CurrencyUtils.formatCurrency(requireContext(), total));
            }
        });

        // Observe monthly expenses
        viewModel.getMonthlyExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double monthly) {
                // Use CurrencyUtils for consistent currency formatting
                tvMonthlyExpenses.setText(CurrencyUtils.formatCurrency(requireContext(), monthly));
            }
        });

        // Observe weekly expenses
        viewModel.getWeeklyExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double weekly) {
                // Use CurrencyUtils for consistent currency formatting
                tvWeeklyExpenses.setText(CurrencyUtils.formatCurrency(requireContext(), weekly));
            }
        });

        // Observe category summary
        viewModel.getCategorySummaryLiveData().observe(getViewLifecycleOwner(), new Observer<List<ExpenseSummary>>() {
            @Override
            public void onChanged(List<ExpenseSummary> summaries) {
                categorySummaryAdapter.updateSummaryList(summaries);

                // Show/hide chart container based on data availability
                viewChartContainer.setVisibility(summaries.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Observe recent expenses
        viewModel.getRecentExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<List<Expense>>() {
            @Override
            public void onChanged(List<Expense> expenses) {
                recentExpensesAdapter.updateExpenseList(expenses);
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

        // Observe chart data
        viewModel.getMonthlyExpenseData(6).observe(getViewLifecycleOwner(), new Observer<Map<String, Double>>() {
            @Override
            public void onChanged(Map<String, Double> chartData) {
                // Update chart with data
                updateChart(chartData);
            }
        });
    }

    private void updateChart(Map<String, Double> chartData) {
        if (chartData == null || chartData.isEmpty()) {
            viewChartContainer.setVisibility(View.GONE);
            return;
        }

        // Make sure the chart container is visible
        viewChartContainer.setVisibility(View.VISIBLE);

        // Get reference to chart view
        View chartView = viewChartContainer.findViewById(R.id.chart_view);
        if (chartView == null) return;

        // Clear any previous content if it's a ViewGroup
        if (chartView instanceof ViewGroup) {
            ((ViewGroup) chartView).removeAllViews();

            // Create a linear layout to show bars
            LinearLayout chartLayout = new LinearLayout(requireContext());
            chartLayout.setOrientation(LinearLayout.VERTICAL);
            chartLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Find max value for scaling
            double maxValue = 0;
            for (Double value : chartData.values()) {
                if (value > maxValue) maxValue = value;
            }

            // Create a sorted list of month entries
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(chartData.entrySet());

            // Sort by month (assuming format is "MMM yyyy")
            Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
                        Date date1 = format.parse(o1.getKey());
                        Date date2 = format.parse(o2.getKey());
                        return date1.compareTo(date2);
                    } catch (ParseException e) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                }
            });

            // Add a bar for each month
            int[] colors = new int[]{
                    getResources().getColor(R.color.colorPrimary, null),
                    getResources().getColor(R.color.colorAccent, null),
                    getResources().getColor(R.color.colorSuccess, null),
                    getResources().getColor(R.color.colorWarning, null),
                    getResources().getColor(R.color.colorInfo, null),
                    getResources().getColor(R.color.colorError, null)
            };

            int colorIndex = 0;
            for (Map.Entry<String, Double> entry : sortedEntries) {
                // Create container for each bar
                LinearLayout barContainer = new LinearLayout(requireContext());
                barContainer.setOrientation(LinearLayout.HORIZONTAL);
                barContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                barContainer.setPadding(0, 8, 0, 8);

                // Label for month
                TextView labelView = new TextView(requireContext());
                labelView.setText(entry.getKey());
                labelView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));

                // Bar showing amount
                View barView = new View(requireContext());
                int width = 0;
                if (maxValue > 0) {
                    // Calculate proportional width based on max value
                    width = (int)(entry.getValue() / maxValue *
                            (chartView.getWidth() * 0.6));
                }
                barView.setLayoutParams(new LinearLayout.LayoutParams(
                        width > 0 ? width : 10, 30));
                barView.setBackgroundColor(colors[colorIndex % colors.length]);

                // Amount text
                TextView amountView = new TextView(requireContext());
                amountView.setText(CurrencyUtils.formatCurrency(requireContext(), entry.getValue()));
                amountView.setPadding(8, 0, 0, 0);

                // Add views to container
                barContainer.addView(labelView);
                barContainer.addView(barView);
                barContainer.addView(amountView);

                // Add bar container to chart
                chartLayout.addView(barContainer);

                colorIndex++;
            }

            ((ViewGroup) chartView).addView(chartLayout);
        }
    }

    @Override
    public void onExpenseClick(int position) {
        // Handle expense click
        List<Expense> expenses = viewModel.getRecentExpensesLiveData().getValue();
        if (expenses != null && position < expenses.size()) {
            Expense expense = expenses.get(position);

            // Navigate to expense form for editing
            Intent intent = new Intent(requireContext(), ExpenseFormActivity.class);
            intent.putExtra("expense_id", expense.getId());
            startActivity(intent);
        }
    }
}