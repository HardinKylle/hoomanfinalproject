package com.example.hoomanfinalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {

    private RecyclerView dogRecyclerView;
    private DogAdapter dogAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Dog> dogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        dogRecyclerView = findViewById(R.id.dogRecyclerView);
        dbHelper = new DatabaseHelper(this);
        dogList = new ArrayList<>();

        // Fetch adoptable dogs from the database
        fetchDogs();

        // Set up RecyclerView
        dogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dogAdapter = new DogAdapter(dogList, this);
        dogRecyclerView.setAdapter(dogAdapter);
    }

    private void fetchDogs() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_DOGS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int dogId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_ID));
                String dogName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_NAME));
                String dogBreed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_BREED));
                String dogAge = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_AGE));
                String dogDescription = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_DESCRIPTION));
                String dogImage = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DOG_IMAGE));

                dogList.add(new Dog(dogId, dogName, dogBreed, dogAge, dogDescription, dogImage));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    public void markAsInterested(Dog dog) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dog_id", dog.getDogId());

        long result = db.insert("interested_dogs", null, values);
        if (result != -1) {
            Toast.makeText(this, "Marked as Interested", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to mark as Interested", Toast.LENGTH_SHORT).show();
        }
    }
}
