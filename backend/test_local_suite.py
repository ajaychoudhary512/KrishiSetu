import json
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def run_tests():
    print("=== TESTING LOCAL FASTAPI APP INSTANCE ===")
    
    # 1. Health
    r = client.get("/health")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /health -> {r.status_code}")

    # 2. Metrics
    r = client.get("/metrics")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /metrics -> {r.status_code}")

    # 3. Waste Listings GET
    r = client.get("/api/v1/waste")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/waste -> {r.status_code}, count: {len(r.json().get('data', []))}")

    # 4. Waste Listing POST
    r = client.post("/api/v1/waste", json={
        "title": "Fresh Wheat Straw",
        "category": "wheat",
        "source_type": "farmer",
        "quantity": "15 Tons",
        "price": "₹1,400 / Ton",
        "location": "Karnal, Haryana"
    })
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/waste -> {r.status_code}")

    # 5. Equipment GET
    r = client.get("/api/v1/equipment")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/equipment -> {r.status_code}, count: {len(r.json().get('data', []))}")

    # 6. Equipment Book POST
    r = client.post("/api/v1/equipment/book", json={"equipment_id": 1, "days": 3, "start_date": "2026-10-05"})
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/equipment/book -> {r.status_code}")

    # 7. Labor GET
    r = client.get("/api/v1/labor")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/labor -> {r.status_code}, count: {len(r.json().get('data', []))}")

    # 8. Labor Job POST
    r = client.post("/api/v1/labor/job", json={
        "title": "Harvester Helpers Needed",
        "source_type": "farmer",
        "workers_needed": 5,
        "wage": "₹700 / Day",
        "location": "Bhatinda, Punjab",
        "crop_type": "Wheat"
    })
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/labor/job -> {r.status_code}")

    # 9. Labor Apply POST
    r = client.post("/api/v1/labor/apply/101")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/labor/apply/101 -> {r.status_code}")

    # 10. Disease Check POST
    r = client.post("/api/v1/disease-check/scan", data={"crop_hint": "paddy"})
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/disease-check/scan -> {r.status_code}, diagnosis: {r.json().get('diagnosis', {}).get('disease_name')}")

    # 11. Chat GET
    r = client.get("/api/v1/chat/messages")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/chat/messages -> {r.status_code}")

    # 12. Chat Send POST
    r = client.post("/api/v1/chat/send", json={"sender": "Farmer Ramesh", "message": "Ready to dispatch."})
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/chat/send -> {r.status_code}")

    # 13. Wallet Balance GET
    r = client.get("/api/v1/wallet/balance")
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/wallet/balance -> {r.status_code}, balance: {r.json().get('data', {}).get('balance')}")

    # 14. Wallet Escrow Accept POST
    r = client.post("/api/v1/wallet/escrow/accept", json={"amount": 5000.0, "deal_id": "DEAL-55"})
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/wallet/escrow/accept -> {r.status_code}")

    # 15. Wallet Split Payout POST
    r = client.post("/api/v1/wallet/split-payout", json={"total_deal_amount": 10000.0, "deal_id": "DEAL-56"})
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/wallet/split-payout -> {r.status_code}")

    # 16. Razorpay Create Order POST
    r = client.post("/api/v1/wallet/razorpay/create-order", json={"amount": 2500.0})
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/wallet/razorpay/create-order -> {r.status_code}, order_id: {r.json().get('order_id')}")

    # 17. Razorpay Route Split Order POST
    r = client.post("/api/v1/wallet/razorpay/create-route-order", json={"total_deal_amount": 8000.0})
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/wallet/razorpay/create-route-order -> {r.status_code}")

    # 18. Auth Register POST
    import random
    rand_id = random.randint(10000, 99999)
    reg_phone = f"+9198765{rand_id}"
    reg_email = f"farmer_{rand_id}@agrilink.ai"
    r = client.post("/api/v1/auth/register", json={
        "full_name": "Test Kisan",
        "email": reg_email,
        "phone": reg_phone,
        "password": "Password123!",
        "role": "farmer"
    })
    print(f"[{'PASS' if r.status_code == 201 else 'FAIL'}] POST /api/v1/auth/register -> {r.status_code}")

    # 19. Auth Login POST
    r = client.post("/api/v1/auth/login", json={
        "username": reg_phone,
        "password": "Password123!"
    })
    token = None
    if r.status_code == 200:
        token = r.json()["data"]["access_token"]
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/auth/login -> {r.status_code}")

    # 20. Users /me GET
    if token:
        r = client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {token}"})
        print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] GET /api/v1/users/me -> {r.status_code}, name: {r.json().get('data', {}).get('full_name')}")

        # 21. Users /me PUT
        r = client.put("/api/v1/users/me", headers={"Authorization": f"Bearer {token}"}, json={
            "bio": "Certified Organic Farmer",
            "city": "Indore",
            "state": "Madhya Pradesh"
        })
        print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] PUT /api/v1/users/me -> {r.status_code}")

    # 22. Send OTP POST
    r = client.post("/api/v1/auth/send-otp", json={
        "phone": reg_phone,
        "purpose": "login"
    })
    print(f"[{'PASS' if r.status_code == 200 else 'FAIL'}] POST /api/v1/auth/send-otp -> {r.status_code}")

if __name__ == "__main__":
    run_tests()
