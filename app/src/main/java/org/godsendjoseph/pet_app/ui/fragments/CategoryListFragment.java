package org.godsendjoseph.pet_app.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.models.Category;
import org.godsendjoseph.pet_app.ui.adapters.CategoryAdapter;
import org.godsendjoseph.pet_app.ui.viewmodels.CategoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the list of categories (default + user-defined).
 */
public class CategoryListFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private CategoryViewModel viewModel;

    // Views
    private RecyclerView recyclerView;
    private TextView tvNoCategories;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCategory;

    // Adapter
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Initialize views
        initViews(view);

        // Setup adapter
        setupAdapter();

        // Setup listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();

        // Load categories
        viewModel.loadAllCategories();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_categories);
        tvNoCategories = view.findViewById(R.id.tv_no_categories);
        progressBar = view.findViewById(R.id.progress_bar);
        fabAddCategory = view.findViewById(R.id.fab_add_category);
    }

    private void setupAdapter() {
        adapter = new CategoryAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupListeners() {
        fabAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCategoryForm(-1);
            }
        });
    }

    private void observeViewModel() {
        // Observe categories
        viewModel.getCategoriesLiveData().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                if (categories.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvNoCategories.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvNoCategories.setVisibility(View.GONE);
                    adapter.updateCategoryList(categories);
                }
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
    }

    private void navigateToCategoryForm(int categoryId) {
        // In a real app with Navigation Component, you would use:
        // Bundle args = new Bundle();
        // if (categoryId > 0) {
        //     args.putInt("category_id", categoryId);
        // }
        // Navigation.findNavController(requireView()).navigate(R.id.action_to_category_form, args);

        // For now, we'll just create a temporary implementation:
        Toast.makeText(requireContext(), categoryId > 0 ? "Edit category: " + categoryId
                : "Add new category", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh categories when returning to the fragment
        viewModel.loadAllCategories();
    }

    @Override
    public void onCategoryClick(int position) {
        List<Category> categories = viewModel.getCategoriesLiveData().getValue();
        if (categories != null && position < categories.size()) {
            Category category = categories.get(position);
            navigateToCategoryForm(category.getId());
        }
    }

    @Override
    public void onCategoryLongClick(int position) {
        List<Category> categories = viewModel.getCategoriesLiveData().getValue();
        if (categories != null && position < categories.size()) {
            Category category = categories.get(position);

            // Only allow deletion of user-defined categories
            if (category.getUserId() > 0) {
                showDeleteCategoryDialog(category);
            } else {
                Toast.makeText(requireContext(), "Default categories cannot be deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteCategoryDialog(Category category) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete the category \"" + category.getName() + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCategory(category.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCategory(int categoryId) {
        viewModel.deleteCategory(categoryId).observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer result) {
                if (result > 0) {
                    Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}