package com.agrilink.app.models;

import java.io.Serializable;

public class WasteItem implements Serializable {
    private int id;
    private String sellerId;
    private String sellerName;
    private String sellerPhone;
    private String title;
    private String category;
    private String sourceType; // "farmer" or "industry"
    private String description;
    private double originalQuantity;
    private double remainingQuantity;
    private String unit;
    private String price;
    private String priceUnit;
    private String location;
    private String distance;
    private String imageUrl;
    private int imageResId;
    private String status; // "ACTIVE", "PARTIALLY_SOLD", "SOLD_OUT", "PAUSED", "CANCELLED"
    private boolean isVerified;
    private String createdAt;

    public WasteItem() {
        this.unit = "Quintal";
        this.priceUnit = "Quintal";
        this.status = "ACTIVE";
        this.isVerified = true;
    }

    public WasteItem(String title, String category, String price, String location, String sellerName, boolean isVerified, int imageResId) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.location = location;
        this.sellerName = sellerName;
        this.isVerified = isVerified;
        this.imageResId = imageResId;
        this.unit = "Quintal";
        this.priceUnit = "Quintal";
        this.status = "ACTIVE";
        this.sourceType = "farmer";
        
        // Extract quantity from title if present e.g. "Rice Straw (50 Qtl)" -> 50
        extractQuantityFromTitle(title);
    }

    public WasteItem(String title, String quantity, String price, String location, String distance) {
        this.title = title;
        this.price = price;
        this.location = location;
        this.distance = distance;
        this.category = "Crop Residue";
        this.sellerName = "Verified Farmer";
        this.isVerified = true;
        this.imageResId = 0;
        this.unit = "Quintal";
        this.priceUnit = "Quintal";
        this.status = "ACTIVE";
        this.sourceType = "farmer";
        parseQuantityString(quantity);
    }

    public WasteItem(int id, String title, String category, String sourceType, double originalQuantity,
                     double remainingQuantity, String unit, String price, String priceUnit,
                     String location, String sellerName, String sellerPhone, String description,
                     boolean isVerified, int imageResId, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.sourceType = sourceType;
        this.originalQuantity = originalQuantity;
        this.remainingQuantity = remainingQuantity;
        this.unit = unit;
        this.price = price;
        this.priceUnit = priceUnit;
        this.location = location;
        this.sellerName = sellerName;
        this.sellerPhone = sellerPhone;
        this.description = description;
        this.isVerified = isVerified;
        this.imageResId = imageResId;
        this.status = status;
    }

    private void parseQuantityString(String qStr) {
        if (qStr == null || qStr.trim().isEmpty()) {
            this.originalQuantity = 100;
            this.remainingQuantity = 100;
            return;
        }
        try {
            String digits = qStr.replaceAll("[^0-9.]", "").trim();
            if (!digits.isEmpty()) {
                double val = Double.parseDouble(digits);
                this.originalQuantity = val;
                this.remainingQuantity = val;
            }
            if (qStr.toLowerCase().contains("ton")) {
                this.unit = "Ton";
            } else if (qStr.toLowerCase().contains("kg")) {
                this.unit = "Kg";
            } else {
                this.unit = "Quintal";
            }
        } catch (Exception ignored) {
            this.originalQuantity = 100;
            this.remainingQuantity = 100;
        }
    }

    private void extractQuantityFromTitle(String t) {
        if (t != null && t.contains("(") && t.contains(")")) {
            try {
                int start = t.indexOf('(');
                int end = t.indexOf(')');
                String inside = t.substring(start + 1, end).trim();
                parseQuantityString(inside);
                return;
            } catch (Exception ignored) {}
        }
        this.originalQuantity = 100;
        this.remainingQuantity = 100;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName != null ? sellerName : "Verified Farmer"; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category != null ? category : "Crop Residue"; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceType() { return sourceType != null ? sourceType : "farmer"; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public double getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(double originalQuantity) { this.originalQuantity = originalQuantity; }

    public double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getUnit() { return unit != null ? unit : "Quintal"; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getPrice() { return price != null ? price : "₹100/quintal"; }
    public void setPrice(String price) { this.price = price; }

    public String getPriceUnit() { return priceUnit != null ? priceUnit : "Quintal"; }
    public void setPriceUnit(String priceUnit) { this.priceUnit = priceUnit; }

    public String getLocation() { return location != null ? location : "Indore Mandi"; }
    public void setLocation(String location) { this.location = location; }

    public String getDistance() { return distance != null ? distance : "5 km"; }
    public void setDistance(String distance) { this.distance = distance; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getStatus() { return status != null ? status : "ACTIVE"; }
    public void setStatus(String status) { this.status = status; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isSoldOut() {
        return "SOLD_OUT".equalsIgnoreCase(status) || remainingQuantity <= 0.0001;
    }

    public String getQuantityDisplay() {
        String numStr = (remainingQuantity == (long) remainingQuantity) ?
                String.format("%d", (long) remainingQuantity) :
                String.format("%.1f", remainingQuantity);
        return numStr + " " + getUnit();
    }

    public String getQuantity() {
        return getQuantityDisplay();
    }
}
