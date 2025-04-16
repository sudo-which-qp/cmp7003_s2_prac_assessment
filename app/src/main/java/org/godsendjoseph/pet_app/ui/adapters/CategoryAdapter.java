package org.godsendjoseph.pet_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;

import java.util.List;

/**
 * Adapter for displaying category items in a RecyclerView.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener listener;

    /**
     * Interface for handling category item clicks
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
        void onCategoryLongClick(int position);
    }

    /**
     * Constructor for the adapter
     * @param context The context
     * @param categoryList List of categories to display
     * @param listener Click listener for category items
     */
    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryDescription.setText(category.getDescription());

        // Set category color
        if (category.getColor() != null && !category.getColor().isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(category.getColor());
                holder.viewCategoryColor.setBackgroundColor(color);
            } catch (IllegalArgumentException e) {
                // Default color if parsing fails
                holder.viewCategoryColor.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            }
        } else {
            // Default color if none specified
            holder.viewCategoryColor.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
        }

        // Show delete icon only for user-defined categories (where userId > 0)
        if (category.getUserId() > 0) {
            holder.ivDelete.setVisibility(View.VISIBLE);
        } else {
            holder.ivDelete.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(adapterPosition);
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onCategoryLongClick(adapterPosition);
                    return true;
                }
                return false;
            }
        });

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onCategoryLongClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    /**
     * Updates the category list and refreshes the adapter
     * @param newCategoryList New list of categories
     */
    public void updateCategoryList(List<Category> newCategoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(newCategoryList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for category items
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCategoryName;
        TextView tvCategoryDescription;
        View viewCategoryColor;
        ImageView ivDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_category);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryDescription = itemView.findViewById(R.id.tv_category_description);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);
            ivDelete = itemView.findViewById(R.id.iv_delete_category);
        }
    }
}
