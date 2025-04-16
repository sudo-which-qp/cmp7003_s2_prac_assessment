package org.godsendjoseph.pet_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.models.Expense;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying expense items in a RecyclerView.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private Context context;
    private List<Expense> expenseList;
    private OnExpenseClickListener listener;
    private CategoryDAO categoryDAO;

    /**
     * Interface for handling expense item clicks
     */
    public interface OnExpenseClickListener {
        void onExpenseClick(int position);
    }

    /**
     * Constructor for the adapter
     * @param context The context
     * @param expenseList List of expenses to display
     * @param listener Click listener for expense items
     */
    public ExpenseAdapter(Context context, List<Expense> expenseList, OnExpenseClickListener listener) {
        this.context = context;
        this.expenseList = expenseList;
        this.listener = listener;
        this.categoryDAO = new CategoryDAO(context);
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Format currency amount
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String formattedAmount = currencyFormat.format(expense.getAmount());

        // Get category for the expense
        Category category = categoryDAO.getCategoryById(expense.getCategoryId());
        String categoryName = (category != null) ? category.getName() : "Uncategorized";

        // Set data to views
        holder.tvExpenseTitle.setText(expense.getTitle());
        holder.tvExpenseAmount.setText(formattedAmount);
        holder.tvExpenseDate.setText(expense.getDate());
        holder.tvExpenseCategory.setText(categoryName);
        holder.tvExpenseLocation.setText(expense.getLocation());

        // Handle item clicks
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onExpenseClick(adapterPosition);
                }
            }
        });

        // Set category color indicator
        if (category != null && category.getColor() != null) {
            try {
                int color = android.graphics.Color.parseColor(category.getColor());
                holder.viewCategoryColor.setBackgroundColor(color);
                holder.viewCategoryColor.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                // If color parsing fails, hide the color indicator
                holder.viewCategoryColor.setVisibility(View.GONE);
            }
        } else {
            holder.viewCategoryColor.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    /**
     * Updates the expense list and refreshes the adapter
     * @param newExpenseList New list of expenses
     */
    public void updateExpenseList(List<Expense> newExpenseList) {
        this.expenseList.clear();
        this.expenseList.addAll(newExpenseList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for expense items
     */
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvExpenseTitle;
        TextView tvExpenseAmount;
        TextView tvExpenseDate;
        TextView tvExpenseCategory;
        TextView tvExpenseLocation;
        View viewCategoryColor;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_expense);
            tvExpenseTitle = itemView.findViewById(R.id.tv_expense_title);
            tvExpenseAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvExpenseDate = itemView.findViewById(R.id.tv_expense_date);
            tvExpenseCategory = itemView.findViewById(R.id.tv_expense_category);
            tvExpenseLocation = itemView.findViewById(R.id.tv_expense_location);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);
        }
    }
}
