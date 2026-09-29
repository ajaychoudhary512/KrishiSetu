import sys
import json
import httpx
import asyncio

REMOTE_BASE = "https://krishisetu-backend-d73j.onrender.com"

async def test_remote():
    print(f"=== TESTING REMOTE BACKEND ({REMOTE_BASE}) ===")
    results = []
    
    async with httpx.AsyncClient(base_url=REMOTE_BASE, timeout=30.0) as client:
        # 1. Health
        try:
            r = await client.get("/health")
            results.append(("/health", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/health", "GET", "ERROR", str(e)))

        # 2. Waste listings
        try:
            r = await client.get("/api/v1/waste")
            results.append(("/api/v1/waste", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/waste", "GET", "ERROR", str(e)))

        # 3. Create waste listing
        try:
            payload = {
                "title": "Automated Test Paddy Straw",
                "category": "paddy",
                "source_type": "farmer",
                "quantity": "5 Tons",
                "price": "₹1,700 / Ton",
                "location": "Indore, MP",
                "farmer_name": "Test Farmer"
            }
            r = await client.post("/api/v1/waste", json=payload)
            results.append(("/api/v1/waste", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/waste", "POST", "ERROR", str(e)))

        # 4. Equipment list
        try:
            r = await client.get("/api/v1/equipment")
            results.append(("/api/v1/equipment", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/equipment", "GET", "ERROR", str(e)))

        # 5. Equipment book
        try:
            r = await client.post("/api/v1/equipment/book", json={"equipment_id": 1, "days": 2, "start_date": "2026-10-01"})
            results.append(("/api/v1/equipment/book", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/equipment/book", "POST", "ERROR", str(e)))

        # 6. Labor jobs
        try:
            r = await client.get("/api/v1/labor")
            results.append(("/api/v1/labor", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/labor", "GET", "ERROR", str(e)))

        # 7. Labor post job
        try:
            job_payload = {
                "title": "Weeding Workers Needed",
                "source_type": "farmer",
                "workers_needed": 4,
                "wage": "₹600 / Day",
                "location": "Bhopal, MP",
                "crop_type": "Soybean"
            }
            r = await client.post("/api/v1/labor/job", json=job_payload)
            results.append(("/api/v1/labor/job", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/labor/job", "POST", "ERROR", str(e)))

        # 8. Labor apply
        try:
            r = await client.post("/api/v1/labor/apply/101")
            results.append(("/api/v1/labor/apply/101", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/labor/apply/101", "POST", "ERROR", str(e)))

        # 9. Disease check scan
        try:
            r = await client.post("/api/v1/disease-check/scan", data={"crop_hint": "paddy"})
            results.append(("/api/v1/disease-check/scan", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/disease-check/scan", "POST", "ERROR", str(e)))

        # 10. Chat history
        try:
            r = await client.get("/api/v1/chat/messages")
            results.append(("/api/v1/chat/messages", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/chat/messages", "GET", "ERROR", str(e)))

        # 11. Chat send
        try:
            r = await client.post("/api/v1/chat/send", json={"sender": "Test Farmer", "message": "Can I pick up tomorrow?"})
            results.append(("/api/v1/chat/send", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/chat/send", "POST", "ERROR", str(e)))

        # 12. Wallet balance
        try:
            r = await client.get("/api/v1/wallet/balance")
            results.append(("/api/v1/wallet/balance", "GET", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/wallet/balance", "GET", "ERROR", str(e)))

        # 13. Wallet escrow accept
        try:
            r = await client.post("/api/v1/wallet/escrow/accept", json={"amount": 15000.0, "deal_id": "DEAL-TEST-01"})
            results.append(("/api/v1/wallet/escrow/accept", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/wallet/escrow/accept", "POST", "ERROR", str(e)))

        # 14. Wallet split payout
        try:
            r = await client.post("/api/v1/wallet/split-payout", json={"total_deal_amount": 20000.0, "deal_id": "DEAL-TEST-02"})
            results.append(("/api/v1/wallet/split-payout", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/wallet/split-payout", "POST", "ERROR", str(e)))

        # 15. Wallet Razorpay create order
        try:
            r = await client.post("/api/v1/wallet/razorpay/create-order", json={"amount": 1000.0})
            results.append(("/api/v1/wallet/razorpay/create-order", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/wallet/razorpay/create-order", "POST", "ERROR", str(e)))

        # 16. Wallet Razorpay create route order
        try:
            r = await client.post("/api/v1/wallet/razorpay/create-route-order", json={"total_deal_amount": 5000.0})
            results.append(("/api/v1/wallet/razorpay/create-route-order", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/wallet/razorpay/create-route-order", "POST", "ERROR", str(e)))

        # 17. Auth register test user
        test_email = f"test_{int(asyncio.get_event_loop().time())}@example.com"
        test_phone = f"+9198{int(asyncio.get_event_loop().time()) % 100000000:08d}"
        try:
            reg_payload = {
                "full_name": "API Test User",
                "email": test_email,
                "phone": test_phone,
                "password": "Password123!",
                "role": "farmer"
            }
            r = await client.post("/api/v1/auth/register", json=reg_payload)
            results.append(("/api/v1/auth/register", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/auth/register", "POST", "ERROR", str(e)))

        # 18. Auth login
        try:
            login_payload = {
                "username": test_email,
                "password": "Password123!"
            }
            r = await client.post("/api/v1/auth/login", json=login_payload)
            results.append(("/api/v1/auth/login", "POST", r.status_code, r.text[:120]))
        except Exception as e:
            results.append(("/api/v1/auth/login", "POST", "ERROR", str(e)))

    for path, method, status, resp in results:
        status_sym = "[PASS]" if (isinstance(status, int) and 200 <= status < 300) else f"[{status}]"
        # Sanitize any unicode characters from response preview
        clean_resp = resp.encode('ascii', errors='replace').decode('ascii')
        print(f"{status_sym:8} | {method:5} | {path:35} | {clean_resp}")

if __name__ == "__main__":
    asyncio.run(test_remote())
