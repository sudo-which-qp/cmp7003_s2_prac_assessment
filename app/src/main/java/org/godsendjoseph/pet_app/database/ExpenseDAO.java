package org.godsendjoseph.pet_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.godsendjoseph.pet_app.models.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Expense-related database operations.
 * Implements the DAO pattern for Expense model.
 */
public class ExpenseDAO {
    private static final String TAG = "ExpenseDAO";

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Constructor
    public ExpenseDAO(Context context) {
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
     * Insert a new expense into the database
     * @param expense Expense object to insert
     * @return ID of the newly inserted expense, or -1 if insertion failed
     */
    public long insertExpense(Expense expense) {
        long expenseId = -1;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, expense.getUserId());
            values.put(DatabaseHelper.COLUMN_TITLE, expense.getTitle());
            values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
            values.put(DatabaseHelper.COLUMN_DATE, expense.getDate());
            values.put(DatabaseHelper.COLUMN_TIME, expense.getTime());
            values.put(DatabaseHelper.COLUMN_LOCATION, expense.getLocation());
            values.put(DatabaseHelper.COLUMN_CATEGORY_ID, expense.getCategoryId());
            values.put(DatabaseHelper.COLUMN_NOTES, expense.getNotes());

            expenseId = database.insert(DatabaseHelper.TABLE_EXPENSES, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting expense: " + e.getMessage());
        } finally {
            close();
        }

        return expenseId;
    }

    /**
     * Update an existing expense in the database
     * @param expense Expense object with updated values
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateExpense(Expense expense) {
        int rowsAffected = 0;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_TITLE, expense.getTitle());
            values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
            values.put(DatabaseHelper.COLUMN_DATE, expense.getDate());
            values.put(DatabaseHelper.COLUMN_TIME, expense.getTime());
            values.put(DatabaseHelper.COLUMN_LOCATION, expense.getLocation());
            values.put(DatabaseHelper.COLUMN_CATEGORY_ID, expense.getCategoryId());
            values.put(DatabaseHelper.COLUMN_NOTES, expense.getNotes());

            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(expense.getId())};

            rowsAffected = database.update(DatabaseHelper.TABLE_EXPENSES, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error updating expense: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Delete an expense from the database
     * @param expenseId ID of the expense to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    public int deleteExpense(int expenseId) {
        int rowsAffected = 0;

        try {
            open();

            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(expenseId)};

            rowsAffected = database.delete(DatabaseHelper.TABLE_EXPENSES, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting expense: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Get an expense by ID
     * @param expenseId ID of the expense to retrieve
     * @return Expense object if found, null otherwise
     */
    public Expense getExpenseById(int expenseId) {
        Expense expense = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_TIME,
                    DatabaseHelper.COLUMN_LOCATION,
                    DatabaseHelper.COLUMN_CATEGORY_ID,
                    DatabaseHelper.COLUMN_NOTES,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(expenseId)};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                expense = cursorToExpense(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting expense by ID: " + e.getMessage());
        } finally {
            close();
        }

        return expense;
    }

    /**
     * Get all expenses for a specific user
     * @param userId ID of the user whose expenses to retrieve
     * @return List of expenses for the user
     */
    public List<Expense> getExpensesByUserId(int userId) {
        List<Expense> expenses = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_TIME,
                    DatabaseHelper.COLUMN_LOCATION,
                    DatabaseHelper.COLUMN_CATEGORY_ID,
                    DatabaseHelper.COLUMN_NOTES,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            String orderBy = DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_TIME + " DESC";

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Expense expense = cursorToExpense(cursor);
                    expenses.add(expense);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting expenses by user ID: " + e.getMessage());
        } finally {
            close();
        }

        return expenses;
    }

    /**
     * Get expenses for a user filtered by category
     * @param userId ID of the user
     * @param categoryId ID of the category to filter by
     * @return List of expenses matching the filter
     */
    public List<Expense> getExpensesByCategory(int userId, int categoryId) {
        List<Expense> expenses = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_TIME,
                    DatabaseHelper.COLUMN_LOCATION,
                    DatabaseHelper.COLUMN_CATEGORY_ID,
                    DatabaseHelper.COLUMN_NOTES,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_CATEGORY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId), String.valueOf(categoryId)};
            String orderBy = DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_TIME + " DESC";

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Expense expense = cursorToExpense(cursor);
                    expenses.add(expense);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting expenses by category: " + e.getMessage());
        } finally {
            close();
        }

        return expenses;
    }

    /**
     * Get expenses for a user filtered by date range
     * @param userId ID of the user
     * @param startDate Start date in format "yyyy-MM-dd"
     * @param endDate End date in format "yyyy-MM-dd"
     * @return List of expenses within the date range
     */
    public List<Expense> getExpensesByDateRange(int userId, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_TIME,
                    DatabaseHelper.COLUMN_LOCATION,
                    DatabaseHelper.COLUMN_CATEGORY_ID,
                    DatabaseHelper.COLUMN_NOTES,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_DATE + " BETWEEN ? AND ?";
            String[] selectionArgs = {String.valueOf(userId), startDate, endDate};
            String orderBy = DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_TIME + " DESC";

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Expense expense = cursorToExpense(cursor);
                    expenses.add(expense);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting expenses by date range: " + e.getMessage());
        } finally {
            close();
        }

        return expenses;
    }

    /**
     * Get expenses for a user filtered by location
     * @param userId ID of the user
     * @param location Location to filter by
     * @return List of expenses at the specified location
     */
    public List<Expense> getExpensesByLocation(int userId, String location) {
        List<Expense> expenses = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_ID,
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_TIME,
                    DatabaseHelper.COLUMN_LOCATION,
                    DatabaseHelper.COLUMN_CATEGORY_ID,
                    DatabaseHelper.COLUMN_NOTES,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_LOCATION + " LIKE ?";
            String[] selectionArgs = {String.valueOf(userId), "%" + location + "%"};
            String orderBy = DatabaseHelper.COLUMN_DATE + " DESC, " + DatabaseHelper.COLUMN_TIME + " DESC";

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_EXPENSES,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Expense expense = cursorToExpense(cursor);
                    expenses.add(expense);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting expenses by location: " + e.getMessage());
        } finally {
            close();
        }

        return expenses;
    }

    /**
     * Get total expenses for a user
     * @param userId ID of the user
     * @return Total amount of all expenses
     */
    public double getTotalExpenses(int userId) {
        double total = 0;

        try {
            open();

            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " +
                    DatabaseHelper.TABLE_EXPENSES + " WHERE " +
                    DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total expenses: " + e.getMessage());
        } finally {
            close();
        }

        return total;
    }

    /**
     * Get total expenses for a user by category
     * @param userId ID of the user
     * @param categoryId ID of the category
     * @return Total amount of expenses in the category
     */
    public double getTotalExpensesByCategory(int userId, int categoryId) {
        double total = 0;

        try {
            open();

            String query = "SELECT SUM(" + DatabaseHelper.COLUMN_AMOUNT + ") FROM " +
                    DatabaseHelper.TABLE_EXPENSES + " WHERE " +
                    DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_CATEGORY_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId), String.valueOf(categoryId)};

            Cursor cursor = database.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total expenses by category: " + e.getMessage());
        } finally {
            close();
        }

        return total;
    }

    /**
     * Convert cursor to Expense object
     * @param cursor Database cursor positioned at the row to convert
     * @return Expense object populated from cursor data
     */
    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();

        expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        expense.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        expense.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
        expense.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));
        expense.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME)));
        expense.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION)));
        expense.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID)));
        expense.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES)));
        expense.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));

        return expense;
    }
}
