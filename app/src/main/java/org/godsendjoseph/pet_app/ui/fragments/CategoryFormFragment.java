package org.godsendjoseph.pet_app.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.ui.viewmodels.CategoryViewModel;

/**
 * Fragment for adding or editing a category.
 */
public class CategoryFormFragment extends Fragment {

    private CategoryViewModel viewModel;

    // Views
    private EditText etCategoryName;
    private EditText etCategoryDescription;
    private View viewCategoryColor;
    private TextView tvColorValue;
    private Button btnSelectColor;
    private Button btnSave;
    private ProgressBar progressBar;

    // State
    private int selectedColor = Color.parseColor("#4CAF50"); // Default color
    private int categoryId = -1; // -1 for new category, positive for editing

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Initialize views
        initViews(view);

        // Get category ID from arguments if editing
        if (getArguments() != null && getArguments().containsKey("category_id")) {
            categoryId = getArguments().getInt("category_id", -1);
            loadCategoryData();
        }

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();
    }

    private void initViews(View view) {
        etCategoryName = view.findViewById(R.id.et_category_name);
        etCategoryDescription = view.findViewById(R.id.et_category_description);
        viewCategoryColor = view.findViewById(R.id.view_category_color);
        tvColorValue = view.findViewById(R.id.tv_color_value);
        btnSelectColor = view.findViewById(R.id.btn_select_color);
        btnSave = view.findViewById(R.id.btn_save_category);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set initial color
        updateColorDisplay();
    }

    private void loadCategoryData() {
        progressBar.setVisibility(View.VISIBLE);

        viewModel.getCategoryById(categoryId).observe(getViewLifecycleOwner(), new Observer<Category>() {
            @Override
            public void onChanged(Category category) {
                if (category != null) {
                    etCategoryName.setText(category.getName());
                    etCategoryDescription.setText(category.getDescription());

                    if (category.getColor() != null && !category.getColor().isEmpty()) {
                        try {
                            selectedColor = Color.parseColor(category.getColor());
                            updateColorDisplay();
                        } catch (IllegalArgumentException e) {
                            // Invalid color string, keep default
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        btnSelectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    saveCategory();
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openColorPicker() {
        // Create a simple dialog with predefined color options
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Category Color");

        // Predefined colors
        final int[] colors = new int[] {
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#673AB7"), // Deep Purple
                Color.parseColor("#3F51B5"), // Indigo
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#03A9F4"), // Light Blue
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#CDDC39"), // Lime
                Color.parseColor("#FFEB3B"), // Yellow
                Color.parseColor("#FFC107"), // Amber
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#FF5722")  // Deep Orange
        };

        String[] colorNames = new String[] {
                "Red", "Pink", "Purple", "Deep Purple",
                "Indigo", "Blue", "Light Blue", "Cyan",
                "Teal", "Green", "Light Green", "Lime",
                "Yellow", "Amber", "Orange", "Deep Orange"
        };

        // Create color preview squares for each item
        final CharSequence[] colorItems = new CharSequence[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colorItems[i] = "  " + colorNames[i];
        }

        builder.setItems(colorItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedColor = colors[which];
                updateColorDisplay();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateColorDisplay() {
        viewCategoryColor.setBackgroundColor(selectedColor);
        tvColorValue.setText(String.format("#%06X", (0xFFFFFF & selectedColor)));
    }

    private boolean validateInput() {
        String name = etCategoryName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etCategoryName.setError("Category name is required");
            etCategoryName.requestFocus();
            return false;
        }

        return true;
    }

    private void saveCategory() {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String name = etCategoryName.getText().toString().trim();
        String description = etCategoryDescription.getText().toString().trim();
        String colorHex = String.format("#%06X", (0xFFFFFF & selectedColor));

        if (categoryId > 0) {
            // Update existing category
            Category category = new Category(categoryId, name, description, colorHex, viewModel.getCurrentUserId());
            viewModel.updateCategory(category).observe(getViewLifecycleOwner(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer result) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (result > 0) {
                        Toast.makeText(requireContext(), "Category updated", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to update category", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Create new category
            viewModel.addCategory(name, description, colorHex).observe(getViewLifecycleOwner(), new Observer<Long>() {
                @Override
                public void onChanged(Long result) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);

                    if (result > 0) {
                        Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}