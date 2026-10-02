import asyncio
import sys
import os

# UTF-8 encoding for bilingual console output
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# Add backend directory to sys.path
sys.path.insert(0, os.path.abspath("backend"))

from httpx import AsyncClient, ASGITransport
from app.main import app

async def run_tests():
    print("=" * 60)
    print("RUNNING PRODUCTION VERIFICATION TESTS FOR KRISHISETU")
    print("=" * 60)

    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        # TEST SCENARIO 1: Create a Waste Listing with 120 Quintal
        print("\n[TEST 1] Farmer creates listing: Rice Straw, 120 Quintal, Rs 100/Quintal")
        listing_payload = {
            "product_name": "Rice Straw (Sharbati)",
            "category": "Crop Residue",
            "description": "Premium dry golden rice straw suitable for cattle fodder and boiler biomass.",
            "original_quantity": 120.0,
            "unit": "Quintal",
            "price": 100.0,
            "price_unit": "Quintal",
            "location": "Indore, Madhya Pradesh",
            "seller_name": "Ramesh Patel",
            "seller_phone": "+91 98765 43210"
        }
        res = await client.post("/api/v1/marketplace", json=listing_payload)
        assert res.status_code == 201, f"Failed to create listing: {res.text}"
        raw = res.json()
        data = raw.get("data", raw)
        listing_id = data.get("id")
        assert data.get("original_quantity") == 120.0, f"Expected 120.0 original_quantity, got {data.get('original_quantity')}"
        assert data.get("remaining_quantity") == 120.0, f"Expected 120.0 remaining_quantity, got {data.get('remaining_quantity')}"
        assert data.get("unit") == "Quintal", f"Expected Quintal, got {data.get('unit')}"
        assert data.get("status") == "ACTIVE", f"Expected ACTIVE, got {data.get('status')}"
        print(f"  -> SUCCESS: Created listing ID {listing_id}")
        print(f"  -> Stored: original_quantity={data.get('original_quantity')}, remaining_quantity={data.get('remaining_quantity')}, unit={data.get('unit')}")

        # Marketplace listing query
        print("\n[TEST 1.1] Verify Marketplace GET returns 120 Quintal (NOT 50 Quintal)")
        res = await client.get("/api/v1/marketplace")
        assert res.status_code == 200
        listings = res.json().get("data", res.json())
        matched = [l for l in listings if l.get("id") == listing_id]
        assert len(matched) == 1
        assert matched[0].get("remaining_quantity") == 120.0
        print(f"  -> SUCCESS: Marketplace list returns {matched[0].get('remaining_quantity')} {matched[0].get('unit')} (NO HARDCODED 50!)")

        # TEST SCENARIO 2: Buyer purchases 20 Quintal
        print("\n[TEST 2] Industry Buyer purchases 20 Quintal from 120 Quintal listing")
        purchase_payload = {
            "quantity": 20.0,
            "buyer_name": "ITC Biomass Processing Ltd.",
            "buyer_phone": "+91 91234 56789"
        }
        res = await client.post(f"/api/v1/marketplace/{listing_id}/purchase", json=purchase_payload)
        assert res.status_code == 200, f"Purchase failed: {res.text}"
        purchase_data = res.json().get("data", res.json())
        order_info = purchase_data.get("order", {})
        listing_info = purchase_data.get("listing", {})
        assert order_info.get("quantity") == 20.0
        assert listing_info.get("remaining_quantity") == 100.0
        assert listing_info.get("status") == "PARTIALLY_SOLD"
        print(f"  -> SUCCESS: Purchase confirmed. Remaining quantity = {listing_info.get('remaining_quantity')} Quintal")
        print(f"  -> Order ID: {order_info.get('order_number')}, Total Cost: Rs {order_info.get('total_price')}")

        # TEST SCENARIO 3: Buyer tries to purchase 150 Quintal (Oversell Protection)
        print("\n[TEST 3] Buyer attempts to purchase 150 Quintal (Available is only 100 Quintal)")
        over_payload = {
            "quantity": 150.0,
            "buyer_name": "Biofuel India Corp",
            "buyer_phone": "+91 99999 88888"
        }
        res = await client.post(f"/api/v1/marketplace/{listing_id}/purchase", json=over_payload)
        assert res.status_code == 400, f"Expected 400 Bad Request, got {res.status_code}"
        err_msg = res.json().get("message", res.json().get("detail", ""))
        print(f"  -> SUCCESS: Transaction rejected as expected with error: '{err_msg}'")
        assert "100.0" in err_msg or "Only" in err_msg or "उपलब्ध" in err_msg

        # TEST SCENARIO 4: Buyer purchases remaining 100 Quintal (Sold Out Transition)
        print("\n[TEST 4] Buyer purchases remaining 100 Quintal")
        rem_payload = {
            "quantity": 100.0,
            "buyer_name": "Biofuel India Corp",
            "buyer_phone": "+91 99999 88888"
        }
        res = await client.post(f"/api/v1/marketplace/{listing_id}/purchase", json=rem_payload)
        assert res.status_code == 200, f"Purchase failed: {res.text}"
        rem_data = res.json().get("data", res.json())
        rem_listing = rem_data.get("listing", {})
        assert rem_listing.get("remaining_quantity") == 0.0
        assert rem_listing.get("status") == "SOLD_OUT"
        print(f"  -> SUCCESS: Remaining quantity = 0.0, Status transitioned to {rem_listing.get('status')}")

        # TEST SCENARIO 5: Subsequent purchase on SOLD_OUT listing
        print("\n[TEST 5] Subsequent purchase attempt on SOLD_OUT listing")
        res = await client.post(f"/api/v1/marketplace/{listing_id}/purchase", json={"quantity": 5.0})
        assert res.status_code == 400
        print(f"  -> SUCCESS: Rejected with error: '{res.json().get('message', res.json().get('detail'))}'")

        # TEST SCENARIO 6: Verify other ecosystem services (Equipment, Labor, Transport)
        print("\n[TEST 6] Verify Equipment, Labor, and Transport endpoints")
        res_eq = await client.get("/api/v1/equipment")
        assert res_eq.status_code == 200
        print(f"  -> Equipment listings available: {len(res_eq.json())} items")

        res_lab = await client.get("/api/v1/labor/jobs")
        assert res_lab.status_code == 200
        print(f"  -> Labor jobs available: {len(res_lab.json())} jobs")

        res_tr = await client.get("/api/v1/transport/vehicles")
        assert res_tr.status_code == 200
        print(f"  -> Transport vehicles available: {len(res_tr.json())} vehicles")

    print("\n" + "=" * 60)
    print("ALL PRODUCTION SCENARIOS PASSED WITH 100% SUCCESS!")
    print("=" * 60)

if __name__ == "__main__":
    asyncio.run(run_tests())
