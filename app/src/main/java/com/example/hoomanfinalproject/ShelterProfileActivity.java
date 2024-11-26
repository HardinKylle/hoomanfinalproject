package com.example.hoomanfinalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ShelterProfileActivity extends AppCompatActivity {
    private Button addDogButton, saveDogButton, selectImageButton;
    private ListView dogsListView;
    private LinearLayout addDogLayout;
    private EditText dogNameEditText, dogBreedEditText, dogAgeEditText, dogDescriptionEditText;
    private ImageView dogImageView;
    private String dogImagePath = null; // To store the selected image path
    private ArrayAdapter<String> dogsAdapter;
    private DatabaseHelper dbHelper;
    private TextView shelterNameTextView, shelterLocationTextView, shelterUsernameTextView, shelterContactTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelter_profile);

        dbHelper = new DatabaseHelper(this);

        shelterNameTextView = findViewById(R.id.shelterNameTextView);
        shelterLocationTextView = findViewById(R.id.shelterLocationTextView);
        shelterUsernameTextView = findViewById(R.id.shelterUsernameTextView);
        shelterContactTextView = findViewById(R.id.shelterContactTextView);
        addDogButton = findViewById(R.id.addDogButton);
        dogsListView = findViewById(R.id.dogsListView);
        addDogLayout = findViewById(R.id.addDogLayout);
        dogNameEditText = findViewById(R.id.dogName);
        dogBreedEditText = findViewById(R.id.dogBreed);
        dogAgeEditText = findViewById(R.id.dogAge);
        dogDescriptionEditText = findViewById(R.id.dogDescription);
        saveDogButton = findViewById(R.id.saveDogButton);
        dogImageView = findViewById(R.id.dogImageView);
        selectImageButton = findViewById(R.id.selectImageButton);

        loadShelterInfo();
        loadDogsList();

        addDogButton.setOnClickListener(v -> {
            addDogLayout.setVisibility(View.VISIBLE);
            dogsListView.setVisibility(View.GONE);
            addDogButton.setVisibility(View.GONE);
        });

        saveDogButton.setOnClickListener(v -> saveDogProfile());

        selectImageButton.setOnClickListener(v -> selectImage());
    }

// Inside loadShelterInfo method:

    private void loadShelterInfo() {
        String shelterUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", null);

        if (shelterUsername == null) {
            Toast.makeText(this, "Error: No shelter logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COLUMN_SHELTER_NAME,
                        DatabaseHelper.COLUMN_SHELTER_LOCATION,
                        DatabaseHelper.COLUMN_PHONE,   // Fetch phone number
                        DatabaseHelper.COLUMN_EMAIL   // Fetch email
                },
                DatabaseHelper.COLUMN_USERNAME + "=?",
                new String[]{shelterUsername}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Ensure column indexes are valid
            int shelterNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SHELTER_NAME);
            int shelterLocationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SHELTER_LOCATION);
            int shelterPhoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE);
            int shelterEmailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL);

            if (shelterNameIndex != -1 && shelterLocationIndex != -1 && shelterPhoneIndex != -1 && shelterEmailIndex != -1) {
                String shelterName = cursor.getString(shelterNameIndex);
                String shelterLocation = cursor.getString(shelterLocationIndex);
                String shelterPhone = cursor.getString(shelterPhoneIndex);
                String shelterEmail = cursor.getString(shelterEmailIndex);

                shelterNameTextView.setText("Shelter Name: " + shelterName);
                shelterLocationTextView.setText("Location: " + shelterLocation);
                shelterUsernameTextView.setText("Username: " + shelterUsername); // Could be email or username
                shelterContactTextView.setText("Contact Info:\nPhone: " + shelterPhone + "\nEmail: " + shelterEmail);
            } else {
                Log.e("ShelterProfile", "Column index is invalid. Check column names in the query.");
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Error loading shelter info", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDogsList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String shelterUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", null);

        if (shelterUsername == null) {
            Toast.makeText(this, "Error: No shelter logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                new String[]{DatabaseHelper.COLUMN_DOG_NAME, DatabaseHelper.COLUMN_DOG_IMAGE},
                DatabaseHelper.COLUMN_SHELTER_USERNAME + "=?",
                new String[]{shelterUsername}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            dogsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

            do {
                String dogName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME));
                String dogImage = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE));
                dogsAdapter.add(dogName + (dogImage != null ? " 🐾" : "")); // Add emoji if image exists
            } while (cursor.moveToNext());

            dogsListView.setAdapter(dogsAdapter);
            cursor.close();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            dogImageView.setImageURI(imageUri);
            dogImagePath = imageUri.toString();
        }
    }

    private void saveDogProfile() {
        String name = dogNameEditText.getText().toString().trim();
        String breed = dogBreedEditText.getText().toString().trim();
        String age = dogAgeEditText.getText().toString().trim();
        String description = dogDescriptionEditText.getText().toString().trim();

        if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || description.isEmpty() || dogImagePath == null) {
            Toast.makeText(this, "All fields must be filled, including the image", Toast.LENGTH_SHORT).show();
            return;
        }

        String shelterUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", null);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DOG_NAME, name);
        values.put(DatabaseHelper.COLUMN_DOG_BREED, breed);
        values.put(DatabaseHelper.COLUMN_DOG_AGE, age);
        values.put(DatabaseHelper.COLUMN_DOG_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_DOG_IMAGE, dogImagePath);
        values.put(DatabaseHelper.COLUMN_SHELTER_USERNAME, shelterUsername);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert(DatabaseHelper.TABLE_DOGS, null, values);

        if (result != -1) {
            Toast.makeText(this, "Dog profile saved", Toast.LENGTH_SHORT).show();
            addDogLayout.setVisibility(View.GONE);
            dogsListView.setVisibility(View.VISIBLE);
            addDogButton.setVisibility(View.VISIBLE);
            loadDogsList();
        } else {
            Toast.makeText(this, "Error saving dog profile", Toast.LENGTH_SHORT).show();
        }
    }
}