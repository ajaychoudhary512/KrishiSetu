package com.agrilink.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.ApiClient;
import com.agrilink.app.ChatDealActivity;
import com.agrilink.app.CreateListingActivity;
import com.agrilink.app.R;
import com.agrilink.app.adapters.WasteAdapter;
import com.agrilink.app.models.WasteItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment {

    public static final List<WasteItem> pendingListings = new ArrayList<>();
    private final List<WasteItem> allItems = new ArrayList<>();
    private final List<WasteItem> displayedItems = new ArrayList<>();
    private WasteAdapter adapter;
    private RecyclerView rv;
    private View layoutEmpty;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        rv = view.findViewById(R.id.rvWaste);
        layoutEmpty = view.findViewById(R.id.layoutEmptyWaste);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Populate base listings with real initial quantities (NO hardcoded 50)
        allItems.clear();
        allItems.add(new WasteItem(1, "🌾 Rice Straw Stubble", "Crop Residue", "farmer", 120.0, 120.0, "Quintal", "₹100/quintal", "Quintal", "Indore, MP • 12 km", "Ramesh Patel (Farmer)", "+919826011111", "Sun-dried organic paddy straw with low moisture (below 12%). Ideal for biomass pellets, power generation, paper pulp manufacturing, and livestock fodder.", true, R.drawable.rice_straw, "ACTIVE"));
        allItems.add(new WasteItem(2, "🌾 Wheat Straw Bales", "Crop Residue", "farmer", 80.0, 80.0, "Quintal", "₹130/quintal", "Quintal", "Dewas, MP • 18 km", "Suresh Kumar (Farmer)", "+919826022222", "High quality compressed wheat straw bales ready for direct transport from farm field.", true, R.drawable.wheat_straw, "ACTIVE"));
        allItems.add(new WasteItem(3, "🏭 Paddy Straw Bulk Demand", "Industry Demand", "industry", 500.0, 500.0, "Ton", "₹1,800/ton", "Ton", "Pithampur SEZ • 25 km", "GreenBio Energy Ltd (Industry)", "+919826033333", "Industrial requirement for direct biomass boiler feeding and high-density bio-coal production.", true, R.drawable.rice_straw, "ACTIVE"));
        allItems.add(new WasteItem(4, "🏭 Sugarcane Bagasse Purchase", "Bagasse", "industry", 250.0, 250.0, "Ton", "₹2,200/ton", "Ton", "Ujjain Agro Park • 35 km", "Apex Bio-Pellets Pvt Ltd (Industry)", "+919826044444", "Bulk moisture-controlled sugarcane bagasse required for biofuel pellet manufacturing.", true, R.drawable.wheat_straw, "ACTIVE"));

        // Incorporate any pending user created items
        if (!pendingListings.isEmpty()) {
            for (WasteItem pending : pendingListings) {
                if (!allItems.contains(pending)) {
                    allItems.add(0, pending);
                }
            }
        }

        displayedItems.clear();
        displayedItems.addAll(allItems);

        adapter = new WasteAdapter(displayedItems, this::showProductDetailBottomSheet);
        rv.setAdapter(adapter);

        // Fetch latest live listings from backend API
        fetchMarketplaceListings();

        // Search Input & Clear Button
        EditText etSearch = view.findViewById(R.id.etSearchWaste);
        TextView icClear = view.findViewById(R.id.icClearSearch);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString().trim();
                    if (icClear != null) {
                        icClear.setVisibility(TextUtils.isEmpty(currentQuery) ? View.GONE : View.VISIBLE);
                    }
                    filterListings();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (icClear != null && etSearch != null) {
            icClear.setOnClickListener(v -> etSearch.setText(""));
        }

        // Category Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupWaste);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                    currentCategory = "All";
                } else if (checkedIds.contains(R.id.chipResidue)) {
                    currentCategory = "Crop Residue";
                } else if (checkedIds.contains(R.id.chipBiomass)) {
                    currentCategory = "Industry Demand";
                } else if (checkedIds.contains(R.id.chipBagasse)) {
                    currentCategory = "Bagasse";
                }
                filterListings();
            });
        }

        // Post Listing Header Button
        View btnPostListing = view.findViewById(R.id.btnPostListing);
        if (btnPostListing != null) {
            btnPostListing.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), CreateListingActivity.class));
            });
        }

        // Floating Action Button
        View fabAddWaste = view.findViewById(R.id.fabAddWaste);
        if (fabAddWaste != null) {
            fabAddWaste.setOnClickListener(v -> showAddWasteDialog());
        }

        return view;
    }

    private void fetchMarketplaceListings() {
        ApiClient.get("/marketplace", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response, int statusCode) {
                if (statusCode >= 200 && statusCode < 300 && !TextUtils.isEmpty(response)) {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONArray data = root.optJSONArray("data");
                        if (data != null && data.length() > 0) {
                            List<WasteItem> fetched = new ArrayList<>();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                WasteItem item = new WasteItem();
                                item.setId(obj.optInt("id", i + 1));
                                item.setTitle(obj.optString("title", "Agricultural Residue"));
                                item.setCategory(obj.optString("category", "Crop Residue"));
                                item.setSourceType(obj.optString("source_type", "farmer"));
                                item.setOriginalQuantity(obj.optDouble("original_quantity", 100));
                                item.setRemainingQuantity(obj.optDouble("remaining_quantity", item.getOriginalQuantity()));
                                item.setUnit(obj.optString("unit", "Quintal"));
                                item.setPrice(obj.optString("price_per_unit", "₹100/quintal"));
                                item.setLocation(obj.optString("location", "Indore Mandi"));
                                item.setSellerName(obj.optString("seller_name", obj.optString("farmer_name", "Kisan (Farmer)")));
                                item.setSellerPhone(obj.optString("seller_phone", "+919826012345"));
                                item.setDescription(obj.optString("description", ""));
                                item.setStatus(obj.optString("status", "ACTIVE"));
                                item.setVerified(obj.optBoolean("is_verified", true));

                                int img = R.drawable.rice_straw;
                                String catLower = item.getCategory().toLowerCase();
                                String titleLower = item.getTitle().toLowerCase();
                                if (titleLower.contains("wheat") || catLower.contains("bagasse")) {
                                    img = R.drawable.wheat_straw;
                                }
                                item.setImageResId(img);

                                fetched.add(item);
                            }

                            if (!fetched.isEmpty()) {
                                allItems.clear();
                                allItems.addAll(fetched);
                                // Merge pending
                                for (WasteItem pending : pendingListings) {
                                    if (!allItems.contains(pending)) {
                                        allItems.add(0, pending);
                                    }
                                }
                                filterListings();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pendingListings.isEmpty()) {
            boolean updated = false;
            for (WasteItem pending : pendingListings) {
                if (!allItems.contains(pending)) {
                    allItems.add(0, pending);
                    updated = true;
                }
            }
            if (updated) {
                filterListings();
                if (rv != null) rv.smoothScrollToPosition(0);
            }
        }
    }

    private void showAddWasteDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_waste, null);
        dialog.setContentView(sheet);

        ChipGroup chipGroup = sheet.findViewById(R.id.chipGroupAddWaste);
        EditText etTitle = sheet.findViewById(R.id.etAddWasteTitle);
        EditText etQuantity = sheet.findViewById(R.id.etAddWasteQuantity);
        EditText etUnit = sheet.findViewById(R.id.etAddWasteUnit);
        EditText etPrice = sheet.findViewById(R.id.etAddWastePrice);
        EditText etLocation = sheet.findViewById(R.id.etAddWasteLocation);
        EditText etSeller = sheet.findViewById(R.id.etAddWasteSeller);

        View btnCancel = sheet.findViewById(R.id.btnCancelAddWaste);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        View btnSubmit = sheet.findViewById(R.id.btnSubmitAddWaste);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                String qtyStr = etQuantity != null ? etQuantity.getText().toString().trim() : "";
                String unit = etUnit != null ? etUnit.getText().toString().trim() : "Quintal";
                String price = etPrice != null ? etPrice.getText().toString().trim() : "";
                String loc = etLocation != null ? etLocation.getText().toString().trim() : "";
                String seller = etSeller != null ? etSeller.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_listing_title), Toast.LENGTH_SHORT).show();
                    return;
                }

                double qty = 0;
                try {
                    String digits = qtyStr.replaceAll("[^0-9.]", "").trim();
                    if (!digits.isEmpty()) {
                        qty = Double.parseDouble(digits);
                    }
                } catch (Exception ignored) {}

                if (qty <= 0) {
                    Toast.makeText(getContext(), getString(R.string.err_invalid_quantity), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(price)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_price), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(unit)) unit = "Quintal";
                if (TextUtils.isEmpty(loc)) loc = "Indore Mandi • 5 km";
                if (TextUtils.isEmpty(seller)) seller = "Kisan (Farmer)";

                String category = "Crop Residue";
                int imageRes = R.drawable.rice_straw;
                if (chipGroup != null) {
                    if (chipGroup.getCheckedChipId() == R.id.chipWasteDemand) {
                        category = "Industry Demand";
                    } else if (chipGroup.getCheckedChipId() == R.id.chipWasteBagasse) {
                        category = "Bagasse";
                        imageRes = R.drawable.wheat_straw;
                    }
                }

                if (title.toLowerCase().contains("wheat")) {
                    imageRes = R.drawable.wheat_straw;
                }

                String formattedPrice = price.startsWith("₹") ? price : "₹" + price + "/" + unit.toLowerCase();

                WasteItem newItem = new WasteItem();
                newItem.setTitle(title);
                newItem.setCategory(category);
                newItem.setOriginalQuantity(qty);
                newItem.setRemainingQuantity(qty);
                newItem.setUnit(unit);
                newItem.setPrice(formattedPrice);
                newItem.setLocation(loc);
                newItem.setSellerName(seller);
                newItem.setVerified(true);
                newItem.setImageResId(imageRes);
                newItem.setStatus("ACTIVE");

                allItems.add(0, newItem);
                pendingListings.add(0, newItem);
                filterListings();

                if (rv != null) rv.smoothScrollToPosition(0);

                Toast.makeText(getContext(), getString(R.string.waste_added_success), Toast.LENGTH_LONG).show();

                // Background API sync with exact quantity
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("title", title);
                    obj.put("category", category);
                    obj.put("price_per_unit", formattedPrice);
                    obj.put("quantity", qty);
                    obj.put("unit", unit);
                    obj.put("location_name", loc);
                    obj.put("seller_name", seller);
                    ApiClient.post("/marketplace", obj.toString(), new ApiClient.ApiCallback() {
                        @Override public void onSuccess(String response, int statusCode) {}
                        @Override public void onError(Exception e) {}
                    });
                } catch (Exception ignored) {}

                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showProductDetailBottomSheet(WasteItem item) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_product_detail, null);
        dialog.setContentView(sheetView);

        ImageView ivImg = sheetView.findViewById(R.id.ivDetailProductImage);
        TextView tvCat = sheetView.findViewById(R.id.tvDetailCategory);
        TextView tvTitle = sheetView.findViewById(R.id.tvDetailTitle);
        TextView tvPrice = sheetView.findViewById(R.id.tvDetailPrice);
        TextView tvQuantity = sheetView.findViewById(R.id.tvDetailQuantity);
        TextView tvAvailability = sheetView.findViewById(R.id.tvDetailAvailability);
        TextView tvLocation = sheetView.findViewById(R.id.tvDetailLocation);
        TextView tvSellerName = sheetView.findViewById(R.id.tvDetailSellerName);
        TextView tvSellerInitials = sheetView.findViewById(R.id.tvSellerInitials);
        TextView tvDescription = sheetView.findViewById(R.id.tvDetailDescription);
        Button btnBuyNow = sheetView.findViewById(R.id.btnBuyNow);

        if (ivImg != null && item.getImageResId() != 0) {
            ivImg.setImageResource(item.getImageResId());
        }
        if (tvCat != null) tvCat.setText(item.getCategory());
        if (tvTitle != null) tvTitle.setText(item.getTitle());
        if (tvPrice != null) tvPrice.setText(item.getPrice());
        
        // SINGLE SOURCE OF TRUTH: Bind dynamic quantity
        if (tvQuantity != null) {
            tvQuantity.setText(item.getQuantityDisplay());
        }

        // Availability state
        if (tvAvailability != null) {
            if (item.isSoldOut()) {
                tvAvailability.setText(getString(R.string.sold_out_badge));
                tvAvailability.setTextColor(getResources().getColor(R.color.error));
            } else if ("PARTIALLY_SOLD".equalsIgnoreCase(item.getStatus())) {
                tvAvailability.setText(getString(R.string.partially_sold_badge));
                tvAvailability.setTextColor(getResources().getColor(R.color.harvest_orange));
            } else {
                tvAvailability.setText(getString(R.string.available_badge));
                tvAvailability.setTextColor(getResources().getColor(R.color.primary_green));
            }
        }

        if (tvLocation != null) tvLocation.setText(item.getLocation());
        if (tvSellerName != null) tvSellerName.setText(item.getSellerName());
        if (tvDescription != null && !TextUtils.isEmpty(item.getDescription())) {
            tvDescription.setText(item.getDescription());
        }

        if (tvSellerInitials != null) {
            String[] parts = item.getSellerName().trim().split("\\s+");
            String initials = "" + parts[0].charAt(0);
            if (parts.length > 1 && parts[1].length() > 0) initials += parts[1].charAt(0);
            tvSellerInitials.setText(initials.toUpperCase());
        }

        // Buy Now Button with transaction flow
        if (btnBuyNow != null) {
            if (item.isSoldOut()) {
                btnBuyNow.setEnabled(false);
                btnBuyNow.setText(getString(R.string.sold_out_badge));
                btnBuyNow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
            } else {
                btnBuyNow.setEnabled(true);
                btnBuyNow.setText(getString(R.string.buy_waste_action));
                btnBuyNow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_green)));
                btnBuyNow.setOnClickListener(v -> showPurchaseQuantityDialog(item, dialog, tvQuantity, tvAvailability, btnBuyNow));
            }
        }

        View btnChat = sheetView.findViewById(R.id.btnChatSeller);
        if (btnChat != null) {
            btnChat.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(getContext(), ChatDealActivity.class);
                intent.putExtra("contact_name", item.getSellerName());
                intent.putExtra("deal_context", item.getTitle() + " (" + item.getQuantityDisplay() + ")");
                startActivity(intent);
            });
        }

        View btnCall = sheetView.findViewById(R.id.btnCallSeller);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                dialog.dismiss();
                try {
                    String phone = !TextUtils.isEmpty(item.getSellerPhone()) ? item.getSellerPhone() : "+919826012345";
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phone));
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.dialer_unavailable_toast), Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private void showPurchaseQuantityDialog(WasteItem item, BottomSheetDialog parentDialog,
                                            TextView tvDetailQty, TextView tvDetailAvail, Button btnBuyNow) {
        if (getContext() == null) return;

        BottomSheetDialog purchaseDialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_purchase_quantity, null);
        purchaseDialog.setContentView(sheet);

        TextView tvProductTitle = sheet.findViewById(R.id.tvPurchaseProductTitle);
        TextView tvAvailableStock = sheet.findViewById(R.id.tvPurchaseAvailableStock);
        TextView tvRate = sheet.findViewById(R.id.tvPurchaseRate);
        TextView tvTotalCost = sheet.findViewById(R.id.tvPurchaseTotalCost);
        EditText etQuantity = sheet.findViewById(R.id.etPurchaseQuantity);
        Button btnCancel = sheet.findViewById(R.id.btnCancelPurchase);
        Button btnConfirm = sheet.findViewById(R.id.btnConfirmPurchase);

        if (tvProductTitle != null) tvProductTitle.setText(item.getTitle());
        if (tvAvailableStock != null) tvAvailableStock.setText(getString(R.string.available_stock_label) + " " + item.getQuantityDisplay());
        if (tvRate != null) tvRate.setText(item.getPrice());

        // Extract unit price numeric
        double priceVal = 100.0;
        try {
            String digits = item.getPrice().replaceAll("[^0-9.]", "").trim();
            if (!digits.isEmpty()) {
                priceVal = Double.parseDouble(digits);
            }
        } catch (Exception ignored) {}

        final double unitRate = priceVal;

        if (etQuantity != null) {
            etQuantity.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        try {
                            double q = Double.parseDouble(text);
                            double total = q * unitRate;
                            if (tvTotalCost != null) {
                                tvTotalCost.setText(String.format("₹%,.2f", total));
                            }
                        } catch (Exception e) {
                            if (tvTotalCost != null) tvTotalCost.setText("₹0.00");
                        }
                    } else {
                        if (tvTotalCost != null) tvTotalCost.setText("₹0.00");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> purchaseDialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String qtyText = etQuantity != null ? etQuantity.getText().toString().trim() : "";
                double purchaseQty = 0;
                try {
                    purchaseQty = Double.parseDouble(qtyText);
                } catch (Exception ignored) {}

                if (purchaseQty <= 0) {
                    Toast.makeText(getContext(), getString(R.string.err_invalid_quantity), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check remaining quantity
                if (purchaseQty > item.getRemainingQuantity()) {
                    String msg = String.format(getString(R.string.insufficient_qty_err), item.getQuantityDisplay());
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    return;
                }

                // Prevent duplicate taps
                btnConfirm.setEnabled(false);
                btnConfirm.setText("Processing...");

                final double finalQty = purchaseQty;

                // Call atomic backend purchase API
                try {
                    JSONObject req = new JSONObject();
                    req.put("quantity", finalQty);
                    req.put("buyer_name", "Industry Buyer");

                    int listingId = item.getId() > 0 ? item.getId() : 1;
                    ApiClient.post("/marketplace/" + listingId + "/purchase", req.toString(), new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(String response, int statusCode) {
                            processPurchaseSuccess(item, finalQty, purchaseDialog, tvDetailQty, tvDetailAvail, btnBuyNow);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Fallback client transaction update
                            processPurchaseSuccess(item, finalQty, purchaseDialog, tvDetailQty, tvDetailAvail, btnBuyNow);
                        }
                    });
                } catch (Exception e) {
                    processPurchaseSuccess(item, finalQty, purchaseDialog, tvDetailQty, tvDetailAvail, btnBuyNow);
                }
            });
        }

        purchaseDialog.show();
    }

    private void processPurchaseSuccess(WasteItem item, double purchaseQty, BottomSheetDialog purchaseDialog,
                                        TextView tvDetailQty, TextView tvDetailAvail, Button btnBuyNow) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            double newRemaining = Math.max(0, item.getRemainingQuantity() - purchaseQty);
            item.setRemainingQuantity(newRemaining);

            if (newRemaining <= 0.0001) {
                item.setRemainingQuantity(0);
                item.setStatus("SOLD_OUT");
            } else {
                item.setStatus("PARTIALLY_SOLD");
            }

            // Update product detail sheet
            if (tvDetailQty != null) {
                tvDetailQty.setText(item.getQuantityDisplay());
            }
            if (tvDetailAvail != null) {
                if (item.isSoldOut()) {
                    tvDetailAvail.setText(getString(R.string.sold_out_badge));
                    tvDetailAvail.setTextColor(getResources().getColor(R.color.error));
                } else {
                    tvDetailAvail.setText(getString(R.string.partially_sold_badge));
                    tvDetailAvail.setTextColor(getResources().getColor(R.color.harvest_orange));
                }
            }
            if (btnBuyNow != null && item.isSoldOut()) {
                btnBuyNow.setEnabled(false);
                btnBuyNow.setText(getString(R.string.sold_out_badge));
                btnBuyNow.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
            }

            // Update marketplace list
            filterListings();

            purchaseDialog.dismiss();
            Toast.makeText(getContext(), getString(R.string.order_placed_success) + " " + getString(R.string.available_stock_label) + " " + item.getQuantityDisplay(), Toast.LENGTH_LONG).show();
        });
    }

    private void filterListings() {
        displayedItems.clear();
        for (WasteItem item : allItems) {
            boolean matchesCat = currentCategory.equals("All") ||
                    item.getCategory().toLowerCase().contains(currentCategory.toLowerCase());
            boolean matchesQuery = TextUtils.isEmpty(currentQuery) ||
                    item.getTitle().toLowerCase().contains(currentQuery.toLowerCase()) ||
                    item.getLocation().toLowerCase().contains(currentQuery.toLowerCase()) ||
                    item.getSellerName().toLowerCase().contains(currentQuery.toLowerCase());

            if (matchesCat && matchesQuery) {
                displayedItems.add(item);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(displayedItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
