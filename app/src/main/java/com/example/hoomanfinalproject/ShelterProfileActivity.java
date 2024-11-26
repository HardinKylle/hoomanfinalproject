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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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

        // Get shelter username passed via Intent
        Intent intent = getIntent();
        String shelterUsername = intent.getStringExtra("shelterUsername");

        if (shelterUsername == null) {
            // If no username is passed, redirect to login
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(ShelterProfileActivity.this, MainActivity.class);
            startActivity(loginIntent);
            finish();
            return; // Prevent further execution
        }

        // Set up other UI elements
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
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

        // Load shelter info and dogs
        loadShelterInfo(shelterUsername);
        loadDogs(shelterUsername);  // Load dogs by shelter username

        addDogButton.setOnClickListener(v -> {
            addDogLayout.setVisibility(View.VISIBLE);
            dogsListView.setVisibility(View.GONE);
            addDogButton.setVisibility(View.GONE);
        });

        saveDogButton.setOnClickListener(v -> saveDogProfile(shelterUsername));

        selectImageButton.setOnClickListener(v -> selectImage());

        dogsListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDogName = (String) parent.getItemAtPosition(position);
            showDogDetailsDialog(selectedDogName.split(" 🐾")[0]);
        });

        // Handle logout
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logout());
    }


    private void loadShelterInfo(String shelterUsername) {
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
                shelterUsernameTextView.setText("Username: " + shelterUsername);
                shelterContactTextView.setText("Contact Info:\nPhone: " + shelterPhone + "\nEmail: " + shelterEmail);
            }

            cursor.close();
        } else {
            Toast.makeText(this, "Error loading shelter info", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDogs(String shelterUsername) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                new String[]{DatabaseHelper.COLUMN_DOG_NAME},
                DatabaseHelper.COLUMN_SHELTER_USERNAME + "=?",
                new String[]{shelterUsername}, null, null, null);

        ArrayList<String> dogNames = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            // Check the column index before accessing it
            int dogNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME);

            if (dogNameIndex != -1) {  // Only proceed if the column exists
                do {
                    String dogName = cursor.getString(dogNameIndex);
                    dogNames.add(dogName + " 🐾");
                } while (cursor.moveToNext());
            } else {
                // Handle the case where the column is not found
                Log.e("loadDogs", "Error: Column '" + DatabaseHelper.COLUMN_DOG_NAME + "' not found in the database.");
            }
            cursor.close();
        } else {
            Log.d("loadDogs", "No dogs found for shelter: " + shelterUsername);
        }

        // If no dogs are found, handle it gracefully
        if (dogNames.isEmpty()) {
            Toast.makeText(this, "No dogs found for this shelter.", Toast.LENGTH_SHORT).show();
        }

        dogsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dogNames);
        dogsListView.setAdapter(dogsAdapter);
    }

    private void saveDogProfile(String shelterUsername) {
        String name = dogNameEditText.getText().toString().trim();
        String breed = dogBreedEditText.getText().toString().trim();
        String age = dogAgeEditText.getText().toString().trim();
        String description = dogDescriptionEditText.getText().toString().trim();

        if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || description.isEmpty() || dogImagePath == null) {
            Toast.makeText(this, "All fields must be filled, including the image", Toast.LENGTH_SHORT).show();
            return;
        }

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
            loadDogs(shelterUsername);  // Refresh the dog list for the shelter
        } else {
            Toast.makeText(this, "Error saving dog profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDogDetailsDialog(String dogName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query for the dog details using the dog's name
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                new String[]{
                        DatabaseHelper.COLUMN_DOG_NAME,
                        DatabaseHelper.COLUMN_DOG_BREED,
                        DatabaseHelper.COLUMN_DOG_AGE,
                        DatabaseHelper.COLUMN_DOG_DESCRIPTION,
                        DatabaseHelper.COLUMN_DOG_IMAGE
                },
                DatabaseHelper.COLUMN_DOG_NAME + "=?",
                new String[]{dogName}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Check column indices before accessing the data
            int dogNameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME);
            int dogBreedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED);
            int dogAgeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE);
            int dogDescriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION);
            int dogImageIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE);

            if (dogNameIndex != -1 && dogBreedIndex != -1 && dogAgeIndex != -1 &&
                    dogDescriptionIndex != -1 && dogImageIndex != -1) {

                // Only proceed if all columns are valid
                String breed = cursor.getString(dogBreedIndex);
                String age = cursor.getString(dogAgeIndex);
                String description = cursor.getString(dogDescriptionIndex);
                String imagePath = cursor.getString(dogImageIndex);

                // Close the cursor as we have the data we need
                cursor.close();

                // Create and show the dialog
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_dog_details, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(dialogView);

                // Find the views inside the dialog
                ImageView dialogDogImage = dialogView.findViewById(R.id.dialogDogImage);
                TextView dialogDogName = dialogView.findViewById(R.id.dialogDogName);
                TextView dialogDogBreed = dialogView.findViewById(R.id.dialogDogBreed);
                TextView dialogDogAge = dialogView.findViewById(R.id.dialogDogAge);
                TextView dialogDogDescription = dialogView.findViewById(R.id.dialogDogDescription);

                // Set the data to the dialog views
                dialogDogName.setText(dogName);
                dialogDogBreed.setText("Breed: " + breed);
                dialogDogAge.setText("Age: " + age);
                dialogDogDescription.setText("Description: " + description);

                // Set the dog image if available
                if (imagePath != null && !imagePath.isEmpty()) {
                    Uri imageUri = Uri.parse(imagePath);
                    dialogDogImage.setImageURI(imageUri);
                } else {
                    // If no image is found, set a default image or leave it empty
                    dialogDogImage.setImageResource(R.drawable.ic_placeholder); // Provide a default image
                }

                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            } else {
                // Handle the case where one or more columns are not found
                Toast.makeText(this, "Error: Missing data in dog details", Toast.LENGTH_SHORT).show();
                cursor.close();
            }
        } else {
            // Handle case where no dog details are found
            Toast.makeText(this, "Error loading dog details", Toast.LENGTH_SHORT).show();
        }
    }


    private void selectImage() {
        // Create an Intent to pick an image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");  // Only allow images
        startActivityForResult(intent, 100);  // 100 is the request code
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Get the image URI from the intent
            Uri imageUri = data.getData();

            // Display the selected image in the ImageView
            dogImageView.setImageURI(imageUri);

            // Optionally, store the image path or URI for later use
            dogImagePath = imageUri.toString();
        }
    }

    private void logout() {
        // Logout functionality: Redirect to login page
        Intent loginIntent = new Intent(ShelterProfileActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
