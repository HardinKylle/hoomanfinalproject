package com.example.hoomanfinalproject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ShelterDashboardActivity extends AppCompatActivity {
    private Button addDogButton, editDogButton;
    private ListView dogsListView;
    private ArrayAdapter<String> dogsAdapter;
    private DatabaseHelper dbHelper;
    private LinearLayout addDogLayout;
    private EditText dogNameEditText, dogBreedEditText, dogAgeEditText, dogDescriptionEditText;
    private Button saveDogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelter_dashboard);

        dbHelper = new DatabaseHelper(this);

        addDogButton = findViewById(R.id.addDogButton);
        editDogButton = findViewById(R.id.editDogButton);
        dogsListView = findViewById(R.id.dogsListView);
        addDogLayout = findViewById(R.id.addDogLayout);
        dogNameEditText = findViewById(R.id.dogName);
        dogBreedEditText = findViewById(R.id.dogBreed);
        dogAgeEditText = findViewById(R.id.dogAge);
        dogDescriptionEditText = findViewById(R.id.dogDescription);
        saveDogButton = findViewById(R.id.saveDogButton);

        // Display the list of dogs
        loadDogsList();

        addDogButton.setOnClickListener(v -> {
            addDogLayout.setVisibility(View.VISIBLE);
            dogsListView.setVisibility(View.GONE);
            addDogButton.setVisibility(View.GONE);
            editDogButton.setVisibility(View.GONE);
        });

        saveDogButton.setOnClickListener(v -> saveDogProfile());

        // On item click of list view, allow editing
        dogsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDog = (String) parent.getItemAtPosition(position);
            editDogProfile(selectedDog);
        });
    }

    // Load dogs into list view filtered by the logged-in shelter
    private void loadDogsList() {
        // Get the logged-in shelter's username from SharedPreferences
        String shelterUsername = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", null);

        if (shelterUsername == null) {
            Toast.makeText(this, "Error: No shelter logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                new String[]{DatabaseHelper.COLUMN_DOG_NAME},
                DatabaseHelper.COLUMN_SHELTER_USERNAME + "=?",
                new String[]{shelterUsername}, null, null, null);

        // Create an adapter with the dog names
        dogsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Check if the cursor is not null and contains data
        if (cursor != null && cursor.getCount() > 0) {
            int dogNameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME);

            // Ensure the column index is valid
            if (dogNameColumnIndex >= 0) {
                while (cursor.moveToNext()) {
                    String dogName = cursor.getString(dogNameColumnIndex);
                    dogsAdapter.add(dogName);
                }
            } else {
                Log.e("ShelterDashboard", "COLUMN_DOG_NAME not found in cursor");
            }
        } else {
            Log.e("ShelterDashboard", "No dogs found in database or cursor is null");
        }

        dogsListView.setAdapter(dogsAdapter);
        if (cursor != null) {
            cursor.close();
        }
    }

    // Save or update a dog profile and associate it with the shelter
    private void saveDogProfile() {
        String name = dogNameEditText.getText().toString().trim();
        String breed = dogBreedEditText.getText().toString().trim();
        String age = dogAgeEditText.getText().toString().trim();
        String description = dogDescriptionEditText.getText().toString().trim();

        if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the logged-in shelter's username from SharedPreferences
        String shelterUsername = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("username", null);

        if (shelterUsername == null) {
            Toast.makeText(this, "Error: No shelter logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert or update the dog data, associating it with the shelter
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DOG_NAME, name);
        values.put(DatabaseHelper.COLUMN_DOG_BREED, breed);
        values.put(DatabaseHelper.COLUMN_DOG_AGE, age);
        values.put(DatabaseHelper.COLUMN_DOG_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_SHELTER_USERNAME, shelterUsername);  // Associate dog with shelter

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert(DatabaseHelper.TABLE_DOGS, null, values);

        if (result != -1) {
            Toast.makeText(this, "Dog profile saved", Toast.LENGTH_SHORT).show();
            loadDogsList(); // Refresh the dog list
            addDogLayout.setVisibility(View.GONE);
            dogsListView.setVisibility(View.VISIBLE);
            addDogButton.setVisibility(View.VISIBLE);
            editDogButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Error saving dog profile", Toast.LENGTH_SHORT).show();
        }
    }

    // Edit dog profile (this would pre-fill the fields with existing dog data)
    private void editDogProfile(String dogName) {
        // Retrieve dog details and fill the form to allow editing
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                new String[]{DatabaseHelper.COLUMN_DOG_NAME, DatabaseHelper.COLUMN_DOG_BREED, DatabaseHelper.COLUMN_DOG_AGE, DatabaseHelper.COLUMN_DOG_DESCRIPTION},
                DatabaseHelper.COLUMN_DOG_NAME + "=?", new String[]{dogName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME);
            int breedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED);
            int ageIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE);
            int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION);

            // Ensure the column index is valid
            if (nameIndex >= 0) dogNameEditText.setText(cursor.getString(nameIndex));
            if (breedIndex >= 0) dogBreedEditText.setText(cursor.getString(breedIndex));
            if (ageIndex >= 0) dogAgeEditText.setText(cursor.getString(ageIndex));
            if (descriptionIndex >= 0) dogDescriptionEditText.setText(cursor.getString(descriptionIndex));
        }

        if (cursor != null) {
            cursor.close();
        }

        addDogLayout.setVisibility(View.VISIBLE);
        dogsListView.setVisibility(View.GONE);
        addDogButton.setVisibility(View.GONE);
        editDogButton.setVisibility(View.GONE);
    }
}
