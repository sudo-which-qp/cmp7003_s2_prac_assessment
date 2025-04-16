package org.godsendjoseph.pet_app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite Database Helper class for the Personal Expense Tracker application.
 * Creates and manages the database schema and versions.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Information
    private static final String DATABASE_NAME = "expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_CATEGORIES = "categories";

    // Common Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Users Table Columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULL_NAME = "full_name";

    // Expenses Table Columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_NOTES = "notes";

    // Categories Table Columns
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COLOR = "color";

    // Create Table Statements
    // Users table create statement
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
            + COLUMN_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_FULL_NAME + " TEXT, "
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    // Expenses table create statement
    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_AMOUNT + " REAL NOT NULL, "
            + COLUMN_DATE + " TEXT NOT NULL, "
            + COLUMN_TIME + " TEXT NOT NULL, "
            + COLUMN_LOCATION + " TEXT, "
            + COLUMN_CATEGORY_ID + " INTEGER, "
            + COLUMN_NOTES + " TEXT, "
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY (" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE SET NULL"
            + ")";

    // Categories table create statement
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_COLOR + " TEXT, "
            + COLUMN_USER_ID + " INTEGER, "
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // Singleton instance
    private static DatabaseHelper instance;

    // Singleton pattern to prevent multiple database connections
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Private constructor to enforce singleton pattern
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_EXPENSES);

        // Insert default categories
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop tables on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate tables
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Insert default expense categories into the database
     */
    private void insertDefaultCategories(SQLiteDatabase db) {
        // Array of default categories with their colors
        String[][] defaultCategories = {
                {"Food & Dining", "Expenses related to food and dining", "#4CAF50"},
                {"Transportation", "Expenses related to travel and transportation", "#2196F3"},
                {"Housing", "Rent, mortgage, and home maintenance", "#FFC107"},
                {"Entertainment", "Movies, games, and leisure activities", "#9C27B0"},
                {"Healthcare", "Medical expenses and health insurance", "#F44336"},
                {"Education", "Tuition, books, and educational expenses", "#3F51B5"},
                {"Shopping", "Clothing, electronics, and other purchases", "#FF5722"},
                {"Utilities", "Electricity, water, internet, and phone bills", "#607D8B"},
                {"Other", "Miscellaneous expenses", "#795548"}
        };

        for (String[] category : defaultCategories) {
            db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " ("
                            + COLUMN_NAME + ", "
                            + COLUMN_DESCRIPTION + ", "
                            + COLUMN_COLOR + ", "
                            + COLUMN_USER_ID
                            + ") VALUES (?, ?, ?, NULL)",
                    new Object[]{category[0], category[1], category[2]});
        }
    }
}
