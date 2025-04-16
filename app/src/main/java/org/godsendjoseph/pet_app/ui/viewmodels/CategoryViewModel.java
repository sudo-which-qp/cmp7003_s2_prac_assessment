package org.godsendjoseph.pet_app.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.database.CategoryDAO;
import org.godsendjoseph.pet_app.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for category-related data.
 * Handles loading and operations on expense categories.
 */
public class CategoryViewModel extends AndroidViewModel {
    private CategoryDAO categoryDAO;
    private AuthManager authManager;

    private MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        categoryDAO = new CategoryDAO(application);
        authManager = AuthManager.getInstance(application);

        // Initialize with empty list
        categoriesLiveData.setValue(new ArrayList<>());
    }

    /**
     * Load all categories (default + user-defined)
     */
    public void loadAllCategories() {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Category> categories = categoryDAO.getAllCategories(userId);

                    categoriesLiveData.postValue(categories);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading categories: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load only default categories
     */
    public void loadDefaultCategories() {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Category> categories = categoryDAO.getDefaultCategories();

                    categoriesLiveData.postValue(categories);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading categories: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Load only user-defined categories
     */
    public void loadUserCategories() {
        isLoadingLiveData.setValue(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    List<Category> categories = categoryDAO.getUserCategories(userId);

                    categoriesLiveData.postValue(categories);
                    isLoadingLiveData.postValue(false);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error loading categories: " + e.getMessage());
                    isLoadingLiveData.postValue(false);
                }
            }
        }).start();
    }

    /**
     * Add a new category
     * @param name Category name
     * @param description Category description
     * @param color Category color (hex code)
     * @return LiveData with the result ID
     */
    public LiveData<Long> addCategory(String name, String description, String color) {
        MutableLiveData<Long> resultLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int userId = authManager.getCurrentUserId();
                    Category category = new Category(name, description, color, userId);

                    long categoryId = categoryDAO.insertCategory(category);
                    resultLiveData.postValue(categoryId);

                    if (categoryId > 0) {
                        loadAllCategories(); // Refresh list
                    } else {
                        errorMessageLiveData.postValue("Failed to add category");
                    }
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error adding category: " + e.getMessage());
                    resultLiveData.postValue(-1L);
                }
            }
        }).start();

        return resultLiveData;
    }

    /**
     * Update an existing category
     * @param category The category to update
     * @return LiveData with the result (number of rows affected)
     */
    public LiveData<Integer> updateCategory(Category category) {
        MutableLiveData<Integer> resultLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int result = categoryDAO.updateCategory(category);
                    resultLiveData.postValue(result);

                    if (result > 0) {
                        loadAllCategories(); // Refresh list
                    } else {
                        errorMessageLiveData.postValue("Failed to update category");
                    }
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error updating category: " + e.getMessage());
                    resultLiveData.postValue(0);
                }
            }
        }).start();

        return resultLiveData;
    }

    /**
     * Delete a category
     * @param categoryId The ID of the category to delete
     * @return LiveData with the result (number of rows affected)
     */
    public LiveData<Integer> deleteCategory(int categoryId) {
        MutableLiveData<Integer> resultLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int result = categoryDAO.deleteCategory(categoryId);
                    resultLiveData.postValue(result);

                    if (result > 0) {
                        loadAllCategories(); // Refresh list
                    } else {
                        errorMessageLiveData.postValue("Failed to delete category");
                    }
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error deleting category: " + e.getMessage());
                    resultLiveData.postValue(0);
                }
            }
        }).start();

        return resultLiveData;
    }

    /**
     * Get a category by ID
     * @param categoryId The ID of the category to retrieve
     * @return LiveData with the category
     */
    public LiveData<Category> getCategoryById(int categoryId) {
        MutableLiveData<Category> categoryLiveData = new MutableLiveData<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Category category = categoryDAO.getCategoryById(categoryId);
                    categoryLiveData.postValue(category);
                } catch (Exception e) {
                    errorMessageLiveData.postValue("Error getting category: " + e.getMessage());
                }
            }
        }).start();

        return categoryLiveData;
    }

    // Getters for LiveData
    public LiveData<List<Category>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    /**
     * Get the current user ID
     * @return The ID of the current user
     */
    public int getCurrentUserId() {
        return authManager.getCurrentUserId();
    }
}
