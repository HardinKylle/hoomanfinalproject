    package com.example.hoomanfinalproject;

    import android.content.ContentValues;
    import android.content.Intent;
    import android.database.Cursor;
    import android.database.sqlite.SQLiteDatabase;
    import android.net.Uri;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.RecyclerView;

    import java.io.File;
    import java.util.ArrayList;
    import java.util.List;

    import java.util.Collections;
    import java.util.List;
    import java.util.Random;

    public class UserHomeActivity extends AppCompatActivity {
        private int currentDogIndex = 0; // To keep track of the currently displayed dog
        private List<Dog> dogsList;
        private DatabaseHelper dbHelper;

        private TextView dogNameTextView, dogBreedTextView, dogAgeTextView, dogDescriptionTextView, dogShelterTextView;
        private ImageView dogImageView;
        private Button nextButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_home);

            dbHelper = new DatabaseHelper(this);
            dogsList = new ArrayList<>();

            dogNameTextView = findViewById(R.id.dogNameTextView);
            dogBreedTextView = findViewById(R.id.dogBreedTextView);
            dogAgeTextView = findViewById(R.id.dogAgeTextView);
            dogDescriptionTextView = findViewById(R.id.dogDescriptionTextView);
            dogShelterTextView = findViewById(R.id.dogShelterTextView);
            dogImageView = findViewById(R.id.dogImageView);
            nextButton = findViewById(R.id.nextButton);

            // Load adoptable dogs from multiple shelters
            loadAdoptableDogs();

            // Shuffle the dogs list to randomize the order
            Collections.shuffle(dogsList);

            // Randomly select the initial dog index
            Random random = new Random();
            currentDogIndex = random.nextInt(dogsList.size());

            // Display the first dog (which is now randomized)
            displayDog(currentDogIndex);

            // Set the "Next" button click listener
            nextButton.setOnClickListener(v -> {
                // Increment the index and wrap around if it's the last dog
                currentDogIndex = (currentDogIndex + 1) % dogsList.size();
                displayDog(currentDogIndex);
            });

            // Set up the Profile Icon click listener
            ImageView profileIcon = findViewById(R.id.profileIcon);
            profileIcon.setOnClickListener(v -> openUserProfile());


        }

        private void openUserProfile() {
            // Retrieve the logged-in username
            Intent intent = getIntent();
            String username = intent.getStringExtra("username");

            if (username != null) {
                // Pass the username to UserProfileActivity
                Intent profileIntent = new Intent(UserHomeActivity.this, UserProfileActivity.class);
                profileIntent.putExtra("username", username);
                startActivity(profileIntent); // Starts the UserProfileActivity
            } else {
                // Handle the case where username is null
                Toast.makeText(this, "Error: User session not found.", Toast.LENGTH_SHORT).show();
            }
        }


        private void loadAdoptableDogs() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS,
                    new String[]{DatabaseHelper.COLUMN_DOG_NAME, DatabaseHelper.COLUMN_DOG_BREED,
                            DatabaseHelper.COLUMN_DOG_AGE, DatabaseHelper.COLUMN_DOG_DESCRIPTION,
                            DatabaseHelper.COLUMN_DOG_IMAGE, DatabaseHelper.COLUMN_SHELTER_USERNAME},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME));
                    String breed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED));
                    String age = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE));
                    String description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION));
                    String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE));
                    String shelterName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SHELTER_USERNAME));

                    dogsList.add(new Dog(name, breed, age, description, imagePath, shelterName));
                } while (cursor.moveToNext());

                cursor.close();
            }
        }

        private void displayDog(int index) {
            if (dogsList != null && !dogsList.isEmpty()) {
                Dog dog = dogsList.get(index);

                // Update the UI with the dog's details
                dogNameTextView.setText(dog.getName());
                dogBreedTextView.setText("Breed: " + dog.getBreed());
                dogAgeTextView.setText("Age: " + dog.getAge());
                dogDescriptionTextView.setText("Description: " + dog.getDescription());
                dogShelterTextView.setText("Shelter: " + dog.getShelterName());

                // Set the image
                if (dog.getImagePath() != null && !dog.getImagePath().isEmpty()) {
                    File imgFile = new File(dog.getImagePath());
                    if (imgFile.exists()) {
                        Uri imageUri = Uri.fromFile(imgFile);
                        dogImageView.setImageURI(imageUri);
                    }
                }
            }
        }


    }

