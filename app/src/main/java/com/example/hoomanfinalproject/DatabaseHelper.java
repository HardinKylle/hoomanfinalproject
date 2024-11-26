package com.example.hoomanfinalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "adoption.db";
    public static final int DATABASE_VERSION = 5; // Incremented version to reflect schema changes

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type"; // Adopter or Shelter
    public static final String COLUMN_AGE = "age"; // For Adopter Users
    public static final String COLUMN_ADDRESS = "address"; // For Adopter Users
    public static final String COLUMN_SHELTER_NAME = "shelterName"; // For Shelter Accounts
    public static final String COLUMN_SHELTER_LOCATION = "shelterLocation"; // For Shelter Accounts

    // New columns for contact info
    public static final String COLUMN_PHONE = "shelterPhone"; // New column for phone
    public static final String COLUMN_EMAIL = "shelterEmail"; // New column for email

    // Dogs Table
    public static final String TABLE_DOGS = "dogs";
    public static final String COLUMN_DOG_ID = "dog_id";
    public static final String COLUMN_DOG_NAME = "dog_name";
    public static final String COLUMN_DOG_BREED = "dog_breed";
    public static final String COLUMN_DOG_AGE = "dog_age";
    public static final String COLUMN_DOG_DESCRIPTION = "dog_description";
    public static final String COLUMN_DOG_IMAGE = "dog_image"; // To store image URL or base64 string (optional)
    public static final String COLUMN_SHELTER_USERNAME = "shelter_username"; // New column to associate dog with shelter

    // New Table to Store Interested Dogs
    public static final String TABLE_INTERESTED_DOGS = "interested_dogs";
    public static final String COLUMN_INTERESTED_ID = "id";
    public static final String COLUMN_USER_ID = "user_id"; // Reference to user


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table with new contact columns (no website)
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_TYPE + " TEXT NOT NULL, " +
                COLUMN_AGE + " INTEGER, " + // Nullable for Shelter Accounts
                COLUMN_ADDRESS + " TEXT, " + // Nullable for Shelter Accounts
                COLUMN_SHELTER_NAME + " TEXT, " + // Nullable for Adopter Users
                COLUMN_SHELTER_LOCATION + " TEXT, " + // Nullable for Adopter Users
                COLUMN_PHONE + " TEXT, " + // New column for phone
                COLUMN_EMAIL + " TEXT)"; // New column for email
        db.execSQL(CREATE_USERS_TABLE);

        // Create Dogs Table with new column shelter_username
        String CREATE_DOGS_TABLE = "CREATE TABLE " + TABLE_DOGS + " (" +
                COLUMN_DOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DOG_NAME + " TEXT NOT NULL, " +
                COLUMN_DOG_BREED + " TEXT, " +
                COLUMN_DOG_AGE + " INTEGER, " +
                COLUMN_DOG_DESCRIPTION + " TEXT, " +
                COLUMN_DOG_IMAGE + " TEXT, " +
                COLUMN_SHELTER_USERNAME + " TEXT NOT NULL)"; // Added column to link dog to shelter
        db.execSQL(CREATE_DOGS_TABLE);

        // Create the interested_dogs table
        String CREATE_INTERESTED_DOGS_TABLE = "CREATE TABLE " + TABLE_INTERESTED_DOGS + " (" +
                COLUMN_INTERESTED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_DOG_ID + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + " (" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_DOG_ID + ") REFERENCES " + TABLE_DOGS + " (" + COLUMN_DOG_ID + "))";
        db.execSQL(CREATE_INTERESTED_DOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns for version 2
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_AGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_ADDRESS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SHELTER_NAME + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_SHELTER_LOCATION + " TEXT");
        }
        if (oldVersion < 3) {
            // Create the dogs table for version 3
            String CREATE_DOGS_TABLE = "CREATE TABLE " + TABLE_DOGS + " (" +
                    COLUMN_DOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DOG_NAME + " TEXT NOT NULL, " +
                    COLUMN_DOG_BREED + " TEXT, " +
                    COLUMN_DOG_AGE + " INTEGER, " +
                    COLUMN_DOG_DESCRIPTION + " TEXT, " +
                    COLUMN_DOG_IMAGE + " TEXT)"; // Original dogs table
            db.execSQL(CREATE_DOGS_TABLE);
        }
        if (oldVersion < 4) {
            // Add the shelter_username column to the dogs table for version 4
            db.execSQL("ALTER TABLE " + TABLE_DOGS + " ADD COLUMN " + COLUMN_SHELTER_USERNAME + " TEXT NOT NULL");
        }
        if (oldVersion < 5) {
            // Add new columns for contact info in version 5 (without website)
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PHONE + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_EMAIL + " TEXT");
        }
    }
}
