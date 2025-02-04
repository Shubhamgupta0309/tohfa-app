package com.project.tohfa;

import java.util.List;

public class Product {
    private String id;
    private String name;
    private String price;
    private String description;
    private List<String> imageUrls;
    private List<String> categories;

    // Constructor
    public Product(String id, String name, String price, String description, List<String> imageUrls, List<String> categories) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls;
        this.categories = categories;
    }

    // Empty constructor for Firestore
    public Product() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getCategory() {
        if (categories != null && !categories.isEmpty()) {
            return categories.get(0); // Assuming the first category is the main category
        }
        return "";
    }

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0); // Assuming the first image is the main image
        }
        return "";
    }
}