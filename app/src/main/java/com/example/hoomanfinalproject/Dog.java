package com.example.hoomanfinalproject;

public class Dog {
    private int dogId;
    private String name;
    private String breed;
    private String age;
    private String description;
    private String imagePath;
    private String shelterName;

    // Constructor, getters, and setters
    public Dog(int dogId, String name, String breed, String age, String description, String imagePath, String shelterName) {
        this.dogId = dogId;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.description = description;
        this.imagePath = imagePath;
        this.shelterName = shelterName;
    }

    public int getDogId() {
        return dogId;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getShelterName() {
        return shelterName;
    }
}
