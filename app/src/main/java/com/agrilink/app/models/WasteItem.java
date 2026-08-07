package com.agrilink.app.models;

public class WasteItem {
    private String title;
    private String category;
    private String price;
    private String location;
    private String sellerName;
    private boolean isVerified;
    private int imageResId;
    private String quantity;
    private String distance;

    public WasteItem(String title, String category, String price, String location, String sellerName, boolean isVerified, int imageResId) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.location = location;
        this.sellerName = sellerName;
        this.isVerified = isVerified;
        this.imageResId = imageResId;
    }

    public WasteItem(String title, String quantity, String price, String location, String distance) {
        this.title = title;
        this.quantity = quantity;
        this.price = price;
        this.location = location;
        this.distance = distance;
        this.category = "Crop Residue";
        this.sellerName = "Verified Farmer";
        this.isVerified = true;
        this.imageResId = 0;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category != null ? category : "Crop Residue"; }
    public String getPrice() { return price; }
    public String getLocation() { return location; }
    public String getSellerName() { return sellerName != null ? sellerName : "Verified Farmer"; }
    public boolean isVerified() { return isVerified; }
    public int getImageResId() { return imageResId; }
    public String getQuantity() { return quantity; }
    public String getDistance() { return distance; }
}
