package org.godsendjoseph.pet_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;

import java.util.List;

/**
 * Custom adapter for displaying categories in a Spinner.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    private LayoutInflater inflater;
    private List<Category> categories;
    private int resource;

    /**
     * Constructor for the spinner adapter
     * @param context The context
     * @param resource Layout resource for spinner items
     * @param categories List of categories to display
     */
    public CategorySpinnerAdapter(@NonNull Context context, int resource, @NonNull List<Category> categories) {
        super(context, resource, categories);
        this.inflater = LayoutInflater.from(context);
        this.categories = categories;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createDropDownItemView(position, convertView, parent);
    }

    /**
     * Creates a view for a selected spinner item
     */
    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
        }

        TextView textView = (TextView) convertView;
        Category category = categories.get(position);
        textView.setText(category.getName());

        return convertView;
    }

    /**
     * Creates a view for a dropdown spinner item
     */
    private View createDropDownItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_category_spinner, parent, false);
        }

        TextView tvCategoryName = convertView.findViewById(R.id.tv_category_name);
        View viewCategoryColor = convertView.findViewById(R.id.view_category_color);

        Category category = categories.get(position);
        tvCategoryName.setText(category.getName());

        // Set category color
        if (category.getColor() != null && !category.getColor().isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(category.getColor());
                viewCategoryColor.setBackgroundColor(color);
                viewCategoryColor.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                // If color parsing fails, hide the color indicator
                viewCategoryColor.setVisibility(View.GONE);
            }
        } else {
            viewCategoryColor.setVisibility(View.GONE);
        }

        return convertView;
    }
}
