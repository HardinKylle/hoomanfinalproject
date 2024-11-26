package com.example.hoomanfinalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "adoption.db";
    public static final int DATABASE_VERSION = 2; // Incremented version to reflect schema changes

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type"; // Normal or Shelter
    public static final String COLUMN_AGE = "age"; // For Normal Users
    public static final String COLUMN_ADDRESS = "address"; // For Normal Users
    public static final String COLUMN_SHELTER_NAME = "shelterName"; // For Shelter Accounts
    public static final String COLUMN_SHELTER_LOCATION = "shelterLocation"; // For Shelter Accounts

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Updated table creation to include all columns
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_TYPE + " TEXT NOT NULL, " +
                COLUMN_AGE + " INTEGER, " + // Nullable for Shelter Accounts
                COLUMN_ADDRESS + " TEXT, " + // Nullable for Shelter Accounts
                COLUMN_SHELTER_NAME + " TEXT, " + // Nullable for Normal Users
                COLUMN_SHELTER_LOCATION + " TEXT)"; // Nullable for Normal Users
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades
        if (oldVersion < 2) {
            // Add new columns for version 2
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_AGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SHELTER_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SHELTER_LOCATION + " TEXT");
        }
    }
}
