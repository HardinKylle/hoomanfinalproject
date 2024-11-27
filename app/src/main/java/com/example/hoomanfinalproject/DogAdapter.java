package com.example.hoomanfinalproject;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {

    private List<Dog> dogsList;
    private Context context;

    public DogAdapter(List<Dog> dogsList, Context context) {
        this.dogsList = dogsList;
        this.context = context;
    }

    @NonNull
    @Override
    public DogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dog_item, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DogViewHolder holder, int position) {
        Dog dog = dogsList.get(position);

        holder.dogNameTextView.setText(dog.getName());
        holder.dogBreedTextView.setText("Breed: " + dog.getBreed());
        holder.dogAgeTextView.setText("Age: " + dog.getAge());
        holder.dogDescriptionTextView.setText("Description: " + dog.getDescription());
        holder.dogShelterTextView.setText("Shelter: " + dog.getShelterName());

        // Set the image
        if (dog.getImagePath() != null && !dog.getImagePath().isEmpty()) {
            File imgFile = new File(dog.getImagePath());
            if (imgFile.exists()) {
                Uri imageUri = Uri.fromFile(imgFile);
                holder.dogImageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dogsList.size();
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        TextView dogNameTextView, dogBreedTextView, dogAgeTextView, dogDescriptionTextView, dogShelterTextView;
        ImageView dogImageView;

        public DogViewHolder(View itemView) {
            super(itemView);
            dogNameTextView = itemView.findViewById(R.id.dogNameTextView);
            dogBreedTextView = itemView.findViewById(R.id.dogBreedTextView);
            dogAgeTextView = itemView.findViewById(R.id.dogAgeTextView);
            dogDescriptionTextView = itemView.findViewById(R.id.dogDescriptionTextView);
            dogShelterTextView = itemView.findViewById(R.id.dogShelterTextView);
            dogImageView = itemView.findViewById(R.id.dogImageView);
        }
    }
}

