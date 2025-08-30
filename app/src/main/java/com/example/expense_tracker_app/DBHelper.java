package com.example.expense_tracker_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ExpenseTrackerDB.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DBHelper";

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Expenses table
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_EXPENSE_ID = "id";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DESCRIPTION = "description";

    // SQL statements to create tables
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USERNAME + " TEXT NOT NULL, " +
            COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
            COLUMN_PASSWORD + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_EXPENSES = "CREATE TABLE " + TABLE_EXPENSES + " (" +
            COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
            COLUMN_AMOUNT + " REAL NOT NULL, " +
            COLUMN_CATEGORY + " TEXT NOT NULL, " +
            COLUMN_DATE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            "FOREIGN KEY (" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + "));";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating users table...");
        db.execSQL(CREATE_TABLE_USERS);
        Log.d(TAG, "Creating expenses table...");
        db.execSQL(CREATE_TABLE_EXPENSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Dropping tables for upgrade...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User-related methods
    public boolean insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        Log.d(TAG, "Insert user result: " + (result != -1));
        return result != -1;
    }

    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        Log.d(TAG, "Check user login for " + email + ": " + exists);
        return exists;
    }

    public String getUsernameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        String username = "";
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Username for " + email + ": " + username);
        return username;
    }

    // Expense-related methods
    public boolean insertExpense(String userEmail, double amount, String category, String date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);

        long result = db.insert(TABLE_EXPENSES, null, values);
        db.close();
        Log.d(TAG, "Insert expense result: " + (result != -1));
        return result != -1;
    }

    public List<Expense> getExpensesByUser(String userEmail) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_USER_EMAIL + " = ? ORDER BY " + COLUMN_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                expenses.add(new Expense(id, userEmail, amount, category, date, description));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Retrieved " + expenses.size() + " expenses for " + userEmail);
        return expenses;
    }

    public double getTotalExpensesByUser(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") as total FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_USER_EMAIL + " = ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{userEmail});
            double total = 0.0;
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            }
            Log.d(TAG, "Total expenses for " + userEmail + ": " + total);
            return total;
        } catch (Exception e) {
            Log.e(TAG, "Error in getTotalExpensesByUser: " + e.getMessage());
            return 0.0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    // Expense model class
    public static class Expense {
        private int id;
        private String userEmail;
        private double amount;
        private String category;
        private String date;
        private String description;

        public Expense(int id, String userEmail, double amount, String category, String date, String description) {
            this.id = id;
            this.userEmail = userEmail;
            this.amount = amount;
            this.category = category;
            this.date = date;
            this.description = description;
        }

        public int getId() { return id; }
        public String getUserEmail() { return userEmail; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
        public String getDescription() { return description; }
    }
}