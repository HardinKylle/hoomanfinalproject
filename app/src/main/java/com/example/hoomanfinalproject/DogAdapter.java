package com.example.hoomanfinalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DogAdapter extends RecyclerView.Adapter<DogAdapter.DogViewHolder> {

    private ArrayList<Dog> dogList;
    private Context context;

    public DogAdapter(ArrayList<Dog> dogList, Context context) {
        this.dogList = dogList;
        this.context = context;
    }

    @Override
    public DogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dog_item, parent, false);
        return new DogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DogViewHolder holder, int position) {
        Dog dog = dogList.get(position);
        holder.dogName.setText(dog.getDogName());
        holder.dogBreed.setText(dog.getDogBreed());
        holder.dogDescription.setText(dog.getDogDescription());
        // Load image here using an image loader (e.g., Picasso, Glide)

        holder.interestButton.setOnClickListener(v -> {
            ((UserHomeActivity) context).markAsInterested(dog);
        });
    }

    @Override
    public int getItemCount() {
        return dogList.size();
    }

    public static class DogViewHolder extends RecyclerView.ViewHolder {
        TextView dogName, dogBreed, dogDescription;
        ImageView dogImage;
        Button interestButton;

        public DogViewHolder(View itemView) {
            super(itemView);
            dogName = itemView.findViewById(R.id.dogName);
            dogBreed = itemView.findViewById(R.id.dogBreed);
            dogDescription = itemView.findViewById(R.id.dogDescription);
            dogImage = itemView.findViewById(R.id.dogImage);
            interestButton = itemView.findViewById(R.id.interestButton);
        }
    }
}
