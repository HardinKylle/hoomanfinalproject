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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

                shelterNameTextView.setText(shelterName);
                shelterLocationTextView.setText("Location: " + shelterLocation);
                shelterUsernameTextView.setText("Username: " + shelterUsername);
                shelterContactTextView.setText("Contact Info:\n Phone: " + shelterPhone + "\n Email: " + shelterEmail);
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
            do {
                String dogName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME));
                dogNames.add(dogName + " 🐾");
                Log.d("loadDogs", "Loaded dog: " + dogName);  // Debugging
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d("loadDogs", "No dogs found for shelter: " + shelterUsername);
        }

        dogsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dogNames);
        dogsListView.setAdapter(dogsAdapter);  // Refresh ListView
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
        values.put(DatabaseHelper.COLUMN_DOG_IMAGE, dogImagePath);  // Save the path instead of URI
        values.put(DatabaseHelper.COLUMN_SHELTER_USERNAME, shelterUsername);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert(DatabaseHelper.TABLE_DOGS, null, values);

        if (result != -1) {
            Toast.makeText(this, "Dog profile saved", Toast.LENGTH_SHORT).show();
            // Reset fields
            resetDogForm();
            addDogLayout.setVisibility(View.GONE);
            dogsListView.setVisibility(View.VISIBLE);
            addDogButton.setVisibility(View.VISIBLE);
            loadDogs(shelterUsername);  // Refresh the dog list for the shelter
        } else {
            Toast.makeText(this, "Error saving dog profile", Toast.LENGTH_SHORT).show();
        }
    }


    private void resetDogForm() {
        // Clear all input fields
        dogNameEditText.setText("");
        dogBreedEditText.setText("");
        dogAgeEditText.setText("");
        dogDescriptionEditText.setText("");
        dogImageView.setImageResource(R.drawable.ic_placeholder); // Replace with a default image
        dogImagePath = null; // Clear the image path
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
            String breed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED));
            String age = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE));
            String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION));
            String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE));

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
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Uri imageUri = Uri.fromFile(imgFile);
                    dialogDogImage.setImageURI(imageUri);
                }
            } else {
                dialogDogImage.setImageResource(R.drawable.ic_placeholder); // Provide a default image
            }

            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

            // Add Edit and Delete buttons to the dialog
            builder.setNeutralButton("Edit", (dialog, which) -> {
                editDogDetails(dogName);
            });

            builder.setNegativeButton("Delete", (dialog, which) -> {
                deleteDog(dogName);
            });

            builder.create().show();
        } else {
            // Handle case where no dog details are found
            Toast.makeText(this, "Error loading dog details", Toast.LENGTH_SHORT).show();
        }
    }
    private void editDogDetails(String dogName) {
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
            String breed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED));
            String age = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE));
            String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION));
            String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE));

            // Populate the fields in the add dog form with the selected dog's details
            dogNameEditText.setText(dogName);
            dogBreedEditText.setText(breed);
            dogAgeEditText.setText(age);
            dogDescriptionEditText.setText(description);
            dogImagePath = imagePath;

            // Show the add dog layout and hide the list
            addDogLayout.setVisibility(View.VISIBLE);
            dogsListView.setVisibility(View.GONE);
            addDogButton.setVisibility(View.GONE);

            // Change the save button text to "Update Dog"
            saveDogButton.setText("Update Dog");

            // Update the save button action to update instead of saving a new dog
            saveDogButton.setOnClickListener(v -> updateDogProfile(dogName));
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void updateDogProfile(String dogName) {
        String name = dogNameEditText.getText().toString().trim();
        String breed = dogBreedEditText.getText().toString().trim();
        String age = dogAgeEditText.getText().toString().trim();
        String description = dogDescriptionEditText.getText().toString().trim();

        if (name.isEmpty() || breed.isEmpty() || age.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DOG_NAME, name);
        values.put(DatabaseHelper.COLUMN_DOG_BREED, breed);
        values.put(DatabaseHelper.COLUMN_DOG_AGE, age);
        values.put(DatabaseHelper.COLUMN_DOG_DESCRIPTION, description);
        values.put(DatabaseHelper.COLUMN_DOG_IMAGE, dogImagePath);  // Keep the existing image path
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rowsUpdated = db.update(DatabaseHelper.TABLE_DOGS, values,
                DatabaseHelper.COLUMN_DOG_NAME + "=?", new String[]{dogName});

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Dog updated successfully", Toast.LENGTH_SHORT).show();
            loadDogs(getIntent().getStringExtra("shelterUsername"));  // Reload dogs list
            resetDogForm();
            addDogLayout.setVisibility(View.GONE);
            dogsListView.setVisibility(View.VISIBLE);
            addDogButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Error updating dog", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDog(String dogName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Dog")
                .setMessage("Are you sure you want to delete this dog?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    int rowsDeleted = db.delete(DatabaseHelper.TABLE_DOGS,
                            DatabaseHelper.COLUMN_DOG_NAME + "=?",
                            new String[]{dogName});

                    if (rowsDeleted > 0) {
                        Toast.makeText(ShelterProfileActivity.this, "Dog deleted", Toast.LENGTH_SHORT).show();
                        loadDogs(getIntent().getStringExtra("shelterUsername"));  // Reload dog list
                    } else {
                        Toast.makeText(ShelterProfileActivity.this, "Error deleting dog", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
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

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    // Get the file from URI
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    String filename = "dog_image_" + System.currentTimeMillis() + ".jpg";  // Unique file name
                    File file = new File(getFilesDir(), filename);  // Save file to internal storage
                    FileOutputStream outputStream = new FileOutputStream(file);

                    // Copy image from InputStream to FileOutputStream
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }

                    // Close streams
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    // Store the file path in dogImagePath
                    dogImagePath = file.getAbsolutePath();
                    dogImageView.setImageURI(Uri.fromFile(file));  // Show image in the ImageView

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save the image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to select an image", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void logout() {
        // Logout functionality: Redirect to login page
        Intent loginIntent = new Intent(ShelterProfileActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
