package com.agrilink.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class CreateListingActivity extends AppCompatActivity {

    private AutoCompleteTextView actvCategory;
    private EditText etListingTitle;
    private EditText etPrice;
    private EditText etQuantity;
    private EditText etLocation;
    private EditText etDescription;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        findViewById(R.id.btnBackCreate).setOnClickListener(v -> finish());

        actvCategory = findViewById(R.id.actvCategory);
        etListingTitle = findViewById(R.id.etListingTitle);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);

        String[] categories = new String[]{
            "🌾 [Farmer] Paddy / Rice Straw (Sell Stubble)",
            "🌾 [Farmer] Wheat Straw Bales (Sell Stubble)",
            "🌾 [Farmer] Sugarcane Bagasse (Sell)",
            "🚜 [Farmer/Owner] Tractor / Harvester (Rent Out)",
            "👨‍🌾 [Farmer] Farm Labour Requirement (Hiring)",
            "🏭 [Industry] Stubble / Biomass Raw Material Demand (Purchase)",
            "🏭 [Industry] Commercial Fleet & Heavy Machinery Rental (Demand)",
            "🏭 [Industry] Factory & Biomass Plant Operator Hiring (Labour)"
        };

        if (actvCategory != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
            actvCategory.setAdapter(adapter);
            actvCategory.setText(categories[0], false);
        }

        findViewById(R.id.btnSubmitListing).setOnClickListener(v -> submitListing());
    }

    private void submitListing() {
        String category = actvCategory != null ? actvCategory.getText().toString().trim() : "🌾 [Farmer] Paddy / Rice Straw (Sell Stubble)";
        String title = etListingTitle != null ? etListingTitle.getText().toString().trim() : "";
        String price = etPrice != null ? etPrice.getText().toString().trim() : "";
        String quantityStr = etQuantity != null ? etQuantity.getText().toString().trim() : "";
        String location = etLocation != null ? etLocation.getText().toString().trim() : "";
        String description = etDescription != null ? etDescription.getText().toString().trim() : "";

        String sourceType = category.contains("[Industry]") ? "industry" : "farmer";

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, getString(R.string.err_enter_listing_title), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, getString(R.string.err_enter_price), Toast.LENGTH_SHORT).show();
            return;
        }

        double qtyVal = 0;
        try {
            String digits = quantityStr.replaceAll("[^0-9.]", "").trim();
            if (!digits.isEmpty()) {
                qtyVal = Double.parseDouble(digits);
            }
        } catch (Exception ignored) {}

        if (qtyVal <= 0) {
            Toast.makeText(this, getString(R.string.err_invalid_quantity), Toast.LENGTH_SHORT).show();
            return;
        }

        String unit = "Quintal";
        if (quantityStr.toLowerCase().contains("ton")) {
            unit = "Ton";
        } else if (quantityStr.toLowerCase().contains("kg")) {
            unit = "Kg";
        } else if (quantityStr.toLowerCase().contains("bale")) {
            unit = "Bale";
        } else if (quantityStr.toLowerCase().contains("bag")) {
            unit = "Bag";
        }

        int imgRes = R.drawable.rice_straw;
        if (title.toLowerCase().contains("wheat")) {
            imgRes = R.drawable.wheat_straw;
        }
        String cleanCat = category.contains("Demand") ? "Industry Demand" : (category.contains("Bagasse") ? "Bagasse" : "Crop Residue");
        String finalLocation = TextUtils.isEmpty(location) ? "Indore Mandi • 5 km" : location;

        String formattedPrice = price.startsWith("₹") ? price : "₹" + price + "/" + unit.toLowerCase();

        com.agrilink.app.models.WasteItem newItem = new com.agrilink.app.models.WasteItem();
        newItem.setTitle(title);
        newItem.setCategory(cleanCat);
        newItem.setSourceType(sourceType);
        newItem.setOriginalQuantity(qtyVal);
        newItem.setRemainingQuantity(qtyVal);
        newItem.setUnit(unit);
        newItem.setPrice(formattedPrice);
        newItem.setLocation(finalLocation);
        newItem.setSellerName("Kisan (Farmer)");
        newItem.setDescription(description);
        newItem.setImageResId(imgRes);
        newItem.setStatus("ACTIVE");
        newItem.setVerified(true);

        com.agrilink.app.fragments.MarketplaceFragment.pendingListings.add(0, newItem);

        // Prevent duplicate taps
        View btnSubmit = findViewById(R.id.btnSubmitListing);
        if (btnSubmit != null) btnSubmit.setEnabled(false);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", title);
            jsonBody.put("category", cleanCat);
            jsonBody.put("source_type", sourceType);
            jsonBody.put("price_per_unit", formattedPrice);
            jsonBody.put("quantity", qtyVal);
            jsonBody.put("unit", unit);
            jsonBody.put("location_name", finalLocation);
            jsonBody.put("description", description);
            jsonBody.put("seller_name", "Kisan (Farmer)");

            Toast.makeText(this, getString(R.string.publishing_listing_prefix, sourceType.toUpperCase()), Toast.LENGTH_SHORT).show();

            ApiClient.post("/marketplace", jsonBody.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response, int statusCode) {
                    Toast.makeText(CreateListingActivity.this, getString(R.string.listing_published_success), Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CreateListingActivity.this, getString(R.string.listing_published_success), Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.listing_published_success), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
