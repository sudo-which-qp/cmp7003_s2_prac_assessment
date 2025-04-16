package org.godsendjoseph.pet_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.godsendjoseph.pet_app.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User-related database operations.
 * Implements the DAO pattern for User model.
 */
public class UserDAO {
    private static final String TAG = "UserDAO";

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Constructor
    public UserDAO(Context context) {
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
     * Insert a new user into the database
     * @param user User object to insert
     * @return ID of the newly inserted user, or -1 if insertion failed
     */
    public long insertUser(User user) {
        long userId = -1;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COLUMN_FULL_NAME, user.getFullName());

            userId = database.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage());
        } finally {
            close();
        }

        return userId;
    }

    /**
     * Update an existing user in the database
     * @param user User object with updated values
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateUser(User user) {
        int rowsAffected = 0;

        try {
            open();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COLUMN_FULL_NAME, user.getFullName());

            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(user.getId())};

            rowsAffected = database.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Delete a user from the database
     * @param userId ID of the user to delete
     * @return Number of rows affected (should be 1 if successful)
     */
    public int deleteUser(int userId) {
        int rowsAffected = 0;

        try {
            open();

            String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            rowsAffected = database.delete(DatabaseHelper.TABLE_USERS, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
        } finally {
            close();
        }

        return rowsAffected;
    }

    /**
     * Get a user by ID
     * @param userId ID of the user to retrieve
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        User user = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USERNAME,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_FULL_NAME,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage());
        } finally {
            close();
        }

        return user;
    }

    /**
     * Get a user by username
     * @param username Username of the user to retrieve
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        User user = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USERNAME,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_FULL_NAME,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by username: " + e.getMessage());
        } finally {
            close();
        }

        return user;
    }

    /**
     * Get a user by email
     * @param email Email of the user to retrieve
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        User user = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USERNAME,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_FULL_NAME,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            String selection = DatabaseHelper.COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by email: " + e.getMessage());
        } finally {
            close();
        }

        return user;
    }

    /**
     * Authenticate a user by username/email and password
     * @param usernameOrEmail Username or email of the user
     * @param password Password of the user
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        User user = null;

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USERNAME,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_FULL_NAME,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            // Check if the input is an email or username
            String selection = "(" + DatabaseHelper.COLUMN_USERNAME + " = ? OR " +
                    DatabaseHelper.COLUMN_EMAIL + " = ?) AND " +
                    DatabaseHelper.COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {usernameOrEmail, usernameOrEmail, password};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
        } finally {
            close();
        }

        return user;
    }

    /**
     * Get all users from the database
     * @return List of all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            open();

            String[] columns = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USERNAME,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_FULL_NAME,
                    DatabaseHelper.COLUMN_CREATED_AT
            };

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    User user = cursorToUser(cursor);
                    users.add(user);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage());
        } finally {
            close();
        }

        return users;
    }

    /**
     * Check if a username already exists
     * @param username Username to check
     * @return true if the username exists, false otherwise
     */
    public boolean isUsernameExists(String username) {
        boolean exists = false;

        try {
            open();

            String[] columns = {DatabaseHelper.COLUMN_ID};
            String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if username exists: " + e.getMessage());
        } finally {
            close();
        }

        return exists;
    }

    /**
     * Check if an email already exists
     * @param email Email to check
     * @return true if the email exists, false otherwise
     */
    public boolean isEmailExists(String email) {
        boolean exists = false;

        try {
            open();

            String[] columns = {DatabaseHelper.COLUMN_ID};
            String selection = DatabaseHelper.COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if email exists: " + e.getMessage());
        } finally {
            close();
        }

        return exists;
    }

    /**
     * Convert cursor to User object
     * @param cursor Database cursor positioned at the row to convert
     * @return User object populated from cursor data
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FULL_NAME)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));

        return user;
    }
}
