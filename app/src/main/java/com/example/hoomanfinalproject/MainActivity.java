package com.example.hoomanfinalproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


/**
 * Nov 7,2024
 * @author Bonggay, Hardin, Mangahas, Santos, Teh
 *
 */


public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private Spinner userTypeSpinner;
    private Button loginButton, signupButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a session already exists
        // Removed SharedPreferences logic for username
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        // Login Button Click Listener
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String userType = userTypeSpinner.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Select User Type".equals(userType)) {
                Toast.makeText(this, "Please select a valid user type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check user credentials in the database
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                    DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=? AND " + DatabaseHelper.COLUMN_USER_TYPE + "=?",
                    new String[]{username, password, userType}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Navigate to respective dashboard
                Intent intent;
                if ("Adopter".equals(userType)) {
                    intent = new Intent(this, UserHomeActivity.class);
                    intent.putExtra("username", username); // Add this line to pass the username
                } else if ("Shelter".equals(userType)) {
                    // Pass shelterUsername to ShelterProfileActivity directly
                    intent = new Intent(this, ShelterProfileActivity.class);
                    intent.putExtra("shelterUsername", username); // Pass username as extra
                } else {
                    return;
                }

                // Start the respective activity
                startActivity(intent);
                finish(); // End login activity

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }

            if (cursor != null) {
                cursor.close();
            }
        });

        // Sign Up Button Click Listener
        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }
}
