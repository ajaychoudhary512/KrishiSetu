package com.agrilink.app.models;

public class EquipmentItem {
    private String title;
    private String rating;
    private String location;
    private String price;
    private String status;
    private boolean isAvailable;
    private int imageResId;
    private String subText;

    public EquipmentItem(String title, String rating, String location, String price, String status, boolean isAvailable, int imageResId) {
        this.title = title;
        this.rating = rating;
        this.location = location;
        this.price = price;
        this.status = status;
        this.isAvailable = isAvailable;
        this.imageResId = imageResId;
    }

    public EquipmentItem(String title, String subText, String price, String rating) {
        this.title = title;
        this.subText = subText;
        this.price = price;
        this.rating = rating;
        this.location = "Indore, MP";
        this.status = "Available";
        this.isAvailable = true;
        this.imageResId = 0;
    }

    public String getTitle() { return title; }
    public String getRating() { return rating; }
    public String getLocation() { return location != null ? location : "Indore, MP"; }
    public String getPrice() { return price; }
    public String getStatus() { return status != null ? status : "Available"; }
    public boolean isAvailable() { return isAvailable; }
    public int getImageResId() { return imageResId; }
    public String getSubText() { return subText; }
}
