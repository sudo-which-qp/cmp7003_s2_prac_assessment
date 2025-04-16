package org.godsendjoseph.pet_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.ExpenseSummary;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying expense summaries (by category, day, month, etc.) in a RecyclerView.
 * Used for insights and dashboard sections.
 */
public class ExpenseSummaryAdapter extends RecyclerView.Adapter<ExpenseSummaryAdapter.SummaryViewHolder> {
    private Context context;
    private List<ExpenseSummary> summaryList;
    private double maxAmount; // For calculating progress bar percentage

    /**
     * Constructor for the adapter
     * @param context The context
     * @param summaryList List of expense summaries to display
     */
    public ExpenseSummaryAdapter(Context context, List<ExpenseSummary> summaryList) {
        this.context = context;
        this.summaryList = summaryList;
        calculateMaxAmount();
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        ExpenseSummary summary = summaryList.get(position);

        // Format currency amount
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String formattedAmount = currencyFormat.format(summary.getAmount());

        // Format percentage
        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.getDefault());
        percentFormat.setMinimumFractionDigits(1);
        percentFormat.setMaximumFractionDigits(1);
        String formattedPercent = percentFormat.format(summary.getPercentage() / 100.0);

        // Set data to views
        holder.tvCategory.setText(summary.getCategory());
        holder.tvAmount.setText(formattedAmount);
        holder.tvPercentage.setText(formattedPercent);

        // Set progress based on percentage of max amount
        int progress = 0;
        if (maxAmount > 0) {
            progress = (int) ((summary.getAmount() / maxAmount) * 100);
        }
        holder.progressBar.setProgress(progress);

        // Set progress bar color based on category color
        if (summary.getColor() != null && !summary.getColor().isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(summary.getColor());
                holder.progressBar.getProgressDrawable().setColorFilter(
                        color, android.graphics.PorterDuff.Mode.SRC_IN);
            } catch (IllegalArgumentException e) {
                // Use default color if parsing fails
            }
        }
    }

    @Override
    public int getItemCount() {
        return summaryList.size();
    }

    /**
     * Updates the summary list and refreshes the adapter
     * @param newSummaryList New list of summaries
     */
    public void updateSummaryList(List<ExpenseSummary> newSummaryList) {
        this.summaryList.clear();
        this.summaryList.addAll(newSummaryList);
        calculateMaxAmount();
        notifyDataSetChanged();
    }

    /**
     * Calculate the maximum amount in the list for progress bar scaling
     */
    private void calculateMaxAmount() {
        maxAmount = 0;
        for (ExpenseSummary summary : summaryList) {
            if (summary.getAmount() > maxAmount) {
                maxAmount = summary.getAmount();
            }
        }
    }

    /**
     * ViewHolder class for expense summary items
     */
    public static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        TextView tvAmount;
        TextView tvPercentage;
        ProgressBar progressBar;

        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_summary_category);
            tvAmount = itemView.findViewById(R.id.tv_summary_amount);
            tvPercentage = itemView.findViewById(R.id.tv_summary_percentage);
            progressBar = itemView.findViewById(R.id.progress_bar_summary);
        }
    }
}
