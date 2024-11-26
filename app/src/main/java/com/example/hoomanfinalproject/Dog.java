package com.example.hoomanfinalproject;

public class Dog {

    private int dogId;
    private String dogName;
    private String dogBreed;
    private String dogDescription;
    private String dogImage;

    public Dog(int dogId, String dogName, String dogBreed, String dogDescription, String dogImage, String image) {
        this.dogId = dogId;
        this.dogName = dogName;
        this.dogBreed = dogBreed;
        this.dogDescription = dogDescription;
        this.dogImage = dogImage;
    }

    public int getDogId() {
        return dogId;
    }

    public String getDogName() {
        return dogName;
    }

    public String getDogBreed() {
        return dogBreed;
    }

    public String getDogDescription() {
        return dogDescription;
    }

    public String getDogImage() {
        return dogImage;
    }
}
