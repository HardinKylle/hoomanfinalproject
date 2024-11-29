package com.example.hoomanfinalproject;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private EditText ageEditText, addressEditText; // Adopter user fields
    private EditText shelterNameEditText, shelterLocationEditText; // Shelter fields
    private EditText emailEditText, phoneEditText; // New fields
    private Spinner userTypeSpinner;
    private Button signUpButton;
    private DatabaseHelper dbHelper;
    private LinearLayout adopterUserLayout, shelterLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        // Initialize common fields
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        emailEditText = findViewById(R.id.email); // New field
        phoneEditText = findViewById(R.id.phone); // New field
        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        signUpButton = findViewById(R.id.signupButton);

        // Initialize additional fields
        ageEditText = findViewById(R.id.age);
        addressEditText = findViewById(R.id.address);
        shelterNameEditText = findViewById(R.id.shelterName);
        shelterLocationEditText = findViewById(R.id.shelterLocation);

        // Initialize layouts
        adopterUserLayout = findViewById(R.id.adopterUserLayout);
        shelterLayout = findViewById(R.id.shelterLayout);

        // Set up the Spinner for user types
        String[] userTypes = {"Select User Type", "Adopter", "Shelter"};  // Add "Select User Type" as the first item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  // Set dropdown style
        userTypeSpinner.setAdapter(adapter);  // Set the adapter to the Spinner

        // Set default selection to "Select User Type"
        userTypeSpinner.setSelection(0); // Select the first item by default

        // Toggle visibility based on user type selection
        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userType = userTypeSpinner.getSelectedItem().toString();
                if (userType.equals("Adopter")) {
                    adopterUserLayout.setVisibility(View.VISIBLE);
                    shelterLayout.setVisibility(View.GONE);
                } else if (userType.equals("Shelter")) {
                    adopterUserLayout.setVisibility(View.GONE);
                    shelterLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Sign-Up button click
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String userType = userTypeSpinner.getSelectedItem().toString(); // Get selected user type

            // Regular expressions for validation
            String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
            String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            String phonePattern = "^\\+?[0-9]{1,4}?[-.\\s]?[0-9]{1,15}$";

            // Check if the fields are empty
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate phone
            Pattern phonePat = Pattern.compile(phonePattern);
            Matcher phoneMatcher = phonePat.matcher(phone);
            if (!phoneMatcher.matches()) {
                Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email
            Pattern emailPat = Pattern.compile(emailPattern);
            Matcher emailMatcher = emailPat.matcher(email);
            if (!emailMatcher.matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password
            Pattern passwordPat = Pattern.compile(passwordPattern);
            Matcher passwordMatcher = passwordPat.matcher(password);
            if (!passwordMatcher.matches()) {
                Toast.makeText(this, "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, username);
            values.put(DatabaseHelper.COLUMN_PASSWORD, password);
            values.put(DatabaseHelper.COLUMN_EMAIL, email);
            values.put(DatabaseHelper.COLUMN_PHONE, phone);
            values.put(DatabaseHelper.COLUMN_USER_TYPE, userType);

            if (userType.equals("Adopter")) {
                String age = ageEditText.getText().toString().trim();
                String address = addressEditText.getText().toString().trim();

                if (age.isEmpty() || address.isEmpty()) {
                    Toast.makeText(this, "Please fill out all fields for Adopter user", Toast.LENGTH_SHORT).show();
                    return;
                }

                values.put("age", age);
                values.put("address", address);

            } else if (userType.equals("Shelter")) {
                String shelterName = shelterNameEditText.getText().toString().trim();
                String shelterLocation = shelterLocationEditText.getText().toString().trim();

                if (shelterName.isEmpty() || shelterLocation.isEmpty()) {
                    Toast.makeText(this, "Please fill out all fields for Shelter", Toast.LENGTH_SHORT).show();
                    return;
                }

                values.put("shelterName", shelterName);
                values.put("shelterLocation", shelterLocation);
            }

            // Insert into the database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);

            if (result != -1) {
                Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Sign-Up Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
