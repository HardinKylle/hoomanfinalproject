package com.example.hoomanfinalproject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity {
    private RecyclerView dogsRecyclerView;
    private DogAdapter dogsAdapter;
    private List<Dog> dogsList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        dbHelper = new DatabaseHelper(this);
        dogsRecyclerView = findViewById(R.id.dogsRecyclerView);
        dogsList = new ArrayList<>();

        // Load adoptable dogs from multiple shelters
        loadAdoptableDogs();

        dogsAdapter = new DogAdapter(dogsList, this);
        dogsRecyclerView.setAdapter(dogsAdapter);
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
}
