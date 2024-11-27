package com.example.hoomanfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {
    private TextView usernameTextView, ageTextView, addressTextView, userTypeTextView, contactInfoTextView;
    private Button logoutButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Get user username passed via Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (username == null) {
            // Handle the case where no username is passed
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(UserProfileActivity.this, MainActivity.class);
            startActivity(loginIntent);
            finish();
            return; // Prevent further execution
        }

        // Set up UI elements
        dbHelper = new DatabaseHelper(this);
        usernameTextView = findViewById(R.id.usernameTextView);
        ageTextView = findViewById(R.id.ageTextView);
        addressTextView = findViewById(R.id.addressTextView);
        userTypeTextView = findViewById(R.id.userTypeTextView);
        contactInfoTextView = findViewById(R.id.contactInfoTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Load user info from the database
        loadUserInfo(username);

        // Handle logout
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserInfo(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_AGE,
                        DatabaseHelper.COLUMN_ADDRESS,
                        DatabaseHelper.COLUMN_USER_TYPE,
                        DatabaseHelper.COLUMN_PHONE,   // For phone
                        DatabaseHelper.COLUMN_EMAIL    // For email
                },
                DatabaseHelper.COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int usernameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME);
            int ageIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE);
            int addressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS);
            int userTypeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_TYPE);
            int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
            int emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);

            if (usernameIndex != -1 && ageIndex != -1 && addressIndex != -1 && userTypeIndex != -1 && phoneIndex != -1 && emailIndex != -1) {
                String usernameValue = cursor.getString(usernameIndex);
                String ageValue = cursor.getString(ageIndex);
                String addressValue = cursor.getString(addressIndex);
                String userTypeValue = cursor.getString(userTypeIndex);
                String phoneValue = cursor.getString(phoneIndex);
                String emailValue = cursor.getString(emailIndex);

                // Set the data into respective views
                usernameTextView.setText("Username: " + usernameValue);
                ageTextView.setText("Age: " + ageValue);
                addressTextView.setText("Address: " + addressValue);
                userTypeTextView.setText("User Type: " + userTypeValue);
                contactInfoTextView.setText("Contact Info:\nPhone: " + phoneValue + "\nEmail: " + emailValue);
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        // Logout functionality: Redirect to login page
        Intent loginIntent = new Intent(UserProfileActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
