package org.godsendjoseph.pet_app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.godsendjoseph.pet_app.ui.adapters.ExpenseAdapter;
import org.godsendjoseph.pet_app.ui.adapters.ExpenseSummaryAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.DashboardViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


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

        // Observe data
        observeViewModel();

        // Load data
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

    private void observeViewModel() {
        // Observe total expenses
        viewModel.getTotalExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double total) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                tvTotalExpenses.setText(currencyFormat.format(total));
            }
        });

        // Observe monthly expenses
        viewModel.getMonthlyExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double monthly) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                tvMonthlyExpenses.setText(currencyFormat.format(monthly));
            }
        });

        // Observe weekly expenses
        viewModel.getWeeklyExpensesLiveData().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double weekly) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                tvWeeklyExpenses.setText(currencyFormat.format(weekly));
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

    @Override
    public void onExpenseClick(int position) {
        // Handle expense click
        List<Expense> expenses = viewModel.getRecentExpensesLiveData().getValue();
        if (expenses != null && position < expenses.size()) {
            Expense expense = expenses.get(position);

            // Navigate to expense details/edit
            // For example, using Navigation component:
            // NavDirections action = DashboardFragmentDirections.actionDashboardToExpenseDetails(expense.getId());
            // Navigation.findNavController(requireView()).navigate(action);
        }
    }
}