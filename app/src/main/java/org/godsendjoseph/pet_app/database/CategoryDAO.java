package org.godsendjoseph.pet_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.godsendjoseph.pet_app.models.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category-related database operations.
 * Implements the DAO pattern for Category model.
 */
public class CategoryDAO {
    private static final String TAG = "CategoryDAO";

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Constructor
    public CategoryDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Open database connection
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Close database connection
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    /**
     * Insert a new category into the database
     * @param category Category object to insert
     * @return ID of the newly inserted category, or -1 if insertion failed
     */
    public long insertCategory(Category category) {
        long categoryId = -1;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME, category.getName());
            values.put(DatabaseHelper.COLUMN_DESCRIPTION, category.getDescription());
            values.put(DatabaseHelper.COLUMN_COLOR, category.getColor());
            values.put(DatabaseHelper.COLUMN_USER_ID, category.getUserId());

            categoryId = database.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting category: " + e.getMessage());
        } finally {
            close();
        }

        return categoryId;
    }

    /**
     * Update an existing category in the database
     * @param category Category object with updated values
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateCategory(Category category) {
        int rowsAffected = 0;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NAME, category.getName());
            values.put(DatabaseHelper.COLUMN_DESCRIPTION, category.getDescription());
            values.put(DatabaseHelper.COLUMN_COLOR, category.getColor());

            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(category.getId())};

            rowsAffected = database.update(DatabaseHelper.TABLE_CATEGORIES, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error updating category: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Delete a category from the database
     * @param categoryId ID of the category to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    public int deleteCategory(int categoryId) {
        int rowsAffected = 0;

        try {
            open();

            // Only allow deletion of user-created categories (where user_id is not null)
            String whereClause = DatabaseHelper.COLUMN_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_USER_ID + " IS NOT NULL";
            String[] whereArgs = {String.valueOf(categoryId)};

            rowsAffected = database.delete(DatabaseHelper.TABLE_CATEGORIES, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Get a category by ID
     * @param categoryId ID of the category to retrieve
     * @return Category object if found, null otherwise
     */
    public Category getCategoryById(int categoryId) {
        Category category = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_DESCRIPTION,
                    DatabaseHelper.COLUMN_COLOR,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(categoryId)};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                category = cursorToCategory(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category by ID: " + e.getMessage());
        } finally {
            close();
        }

        return category;
    }

    /**
     * Get all default categories (where user_id is null)
     * @return List of default categories
     */
    public List<Category> getDefaultCategories() {
        List<Category> categories = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_DESCRIPTION,
                    DatabaseHelper.COLUMN_COLOR,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " IS NULL";

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    columns,
                    selection,
                    null,
                    null,
                    null,
                    DatabaseHelper.COLUMN_NAME + " ASC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Category category = cursorToCategory(cursor);
                    categories.add(category);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting default categories: " + e.getMessage());
        } finally {
            close();
        }

        return categories;
    }

    /**
     * Get all user-defined categories for a specific user
     * @param userId ID of the user whose categories to retrieve
     * @return List of user-defined categories
     */
    public List<Category> getUserCategories(int userId) {
        List<Category> categories = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_DESCRIPTION,
                    DatabaseHelper.COLUMN_COLOR,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DatabaseHelper.COLUMN_NAME + " ASC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Category category = cursorToCategory(cursor);
                    categories.add(category);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user categories: " + e.getMessage());
        } finally {
            close();
        }

        return categories;
    }

    /**
     * Get all categories (both default and user-defined) available to a user
     * @param userId ID of the user
     * @return List of all available categories
     */
    public List<Category> getAllCategories(int userId) {
        List<Category> categories = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_NAME,
                    DatabaseHelper.COLUMN_DESCRIPTION,
                    DatabaseHelper.COLUMN_COLOR,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " IS NULL OR " +
                    DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_CATEGORIES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    DatabaseHelper.COLUMN_NAME + " ASC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Category category = cursorToCategory(cursor);
                    categories.add(category);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all categories: " + e.getMessage());
        } finally {
            close();
        }

        return categories;
    }

    /**
     * Convert cursor to Category object
     * @param cursor Database cursor positioned at the row to convert
     * @return Category object populated from cursor data
     */
    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();

        category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
        category.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
        category.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COLOR)));

        int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID);
        if (!cursor.isNull(userIdIndex)) {
            category.setUserId(cursor.getInt(userIdIndex));
        } else {
            category.setUserId(0); // Use 0 to represent default/system categories
        }

        return category;
    }
}
