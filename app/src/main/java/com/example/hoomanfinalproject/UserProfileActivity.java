package com.example.hoomanfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {
    private TextView usernameTextView, ageTextView, addressTextView, userTypeTextView, contactInfoTextView;
    private TextView interestedDogsHeader; // Header TextView for interested dogs
    private Button logoutButton;
    private ListView interestedDogsListView;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> interestedDogsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Get the username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (username == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(UserProfileActivity.this, MainActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Initialize UI elements
        dbHelper = new DatabaseHelper(this);
        usernameTextView = findViewById(R.id.usernameTextView);
        ageTextView = findViewById(R.id.ageTextView);
        addressTextView = findViewById(R.id.addressTextView);
        contactInfoTextView = findViewById(R.id.contactInfoTextView);
        interestedDogsHeader = findViewById(R.id.interestedDogsHeader); // Initialize header
        logoutButton = findViewById(R.id.logoutButton);
        interestedDogsListView = findViewById(R.id.interestedDogsListView);

        interestedDogsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, interestedDogsList);
        interestedDogsListView.setAdapter(adapter);

        // Load user info and interested dogs
        loadUserInfo(username);
        loadInterestedDogs(username);

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
                        DatabaseHelper.COLUMN_PHONE,
                        DatabaseHelper.COLUMN_EMAIL
                },
                DatabaseHelper.COLUMN_USERNAME + "=?",
                new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String usernameValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
            String ageValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE));
            String addressValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS));
            String phoneValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE));
            String emailValue = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));

            usernameTextView.setText("Username: " + usernameValue);
            ageTextView.setText("Age: " + ageValue);
            addressTextView.setText("Address: " + addressValue);
            contactInfoTextView.setText("Contact Info:\nPhone: " + phoneValue + "\nEmail: " + emailValue);

            cursor.close();
        } else {
            Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInterestedDogs(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d." + DatabaseHelper.COLUMN_DOG_NAME + ", d." + DatabaseHelper.COLUMN_DOG_BREED +
                ", d." + DatabaseHelper.COLUMN_DOG_AGE +
                " FROM interested_dogs id " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON id.user_id = u.id " +
                "INNER JOIN " + DatabaseHelper.TABLE_DOGS + " d ON id.dog_id = d.dog_id " +
                "WHERE u." + DatabaseHelper.COLUMN_USERNAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor != null && cursor.moveToFirst()) {
            interestedDogsList.clear();
            do {
                String dogName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME));
                String dogBreed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED));
                String dogAge = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE));

                interestedDogsList.add(dogName + " (" + dogBreed + ", Age: " + dogAge + ")");
            } while (cursor.moveToNext());

            adapter.notifyDataSetChanged();
            cursor.close();
        } else {
            interestedDogsList.add("No interested dogs yet.");
            adapter.notifyDataSetChanged();
        }
    }

    private void logout() {
        Intent loginIntent = new Intent(UserProfileActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}

