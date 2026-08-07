# 🧪 AgriLink AI — Complete Backend API Testing Manual

This manual contains **everything you need** to test all backend endpoints using **Postman**, **Browser (Swagger UI)**, **cURL**, or **PowerShell**.

---

## 🟢 Server Info & Base URLs

* **Base Server URL**: `http://127.0.0.1:8080`
* **API Base Path**: `http://127.0.0.1:8080/api/v1`
* **Interactive Swagger UI**: [http://127.0.0.1:8080/docs](http://127.0.0.1:8080/docs)
* **ReDoc Documentation**: [http://127.0.0.1:8080/redoc](http://127.0.0.1:8080/redoc)
* **Health Check**: [http://127.0.0.1:8080/health](http://127.0.0.1:8080/health)
* **Postman Collection File**: `C:\Agrilink\AgriLink_AI_Postman_Collection.json`

---

## 🛠️ How to Start/Restart the Server

Double-click or run [start_backend.bat](file:///c:/Agrilink/start_backend.bat) in your project root:
```cmd
cd C:\Agrilink
.\start_backend.bat
```

---

## 🚀 All 14 API Endpoints & Request Payloads

### 1. Health Check
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/health`
* **PowerShell Command**:
  ```powershell
  Invoke-RestMethod -Uri 'http://127.0.0.1:8080/health'
  ```

---

### 2. User Registration
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/auth/register`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "full_name": "Ramesh Patel",
    "email": "ramesh@agrilink.ai",
    "phone": "+919876543210",
    "password": "Kisan@12345"
  }
  ```

---

### 3. User Login
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/auth/login`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "username": "ramesh@agrilink.ai",
    "password": "Kisan@12345"
  }
  ```

---

### 4. Waste Marketplace — Get Listings
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/api/v1/waste?category=all`
* **PowerShell Command**:
  ```powershell
  Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/waste?category=all'
  ```

---

### 5. Waste Marketplace — Post Listing
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/waste`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "id": 5,
    "title": "Organic Mustard Straw Bales",
    "category": "mustard",
    "quantity": "12 Tons",
    "price": "₹1,600 / Ton",
    "location": "Ujjain, MP",
    "farmer_name": "Kailash Choudhary",
    "image_url": "assets/agri_waste_banner.png"
  }
  ```

---

### 6. Equipment Rental — Get Machinery
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/api/v1/equipment`
* **PowerShell Command**:
  ```powershell
  Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/equipment'
  ```

---

### 7. Equipment Rental — Book Rental Machine
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/equipment/book`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "equipment_id": 1,
    "days": 3,
    "start_date": "2026-08-10"
  }
  ```

---

### 8. Labour Hiring — Get Open Jobs
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/api/v1/labor`
* **PowerShell Command**:
  ```powershell
  Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/labor'
  ```

---

### 9. Labour Hiring — Post Requirement
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/labor/job`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "title": "Cotton Picking Workers Needed",
    "workers_needed": 8,
    "wage": "₹550 / Day",
    "location": "Rajkot, Gujarat",
    "crop_type": "Cotton"
  }
  ```

---

### 10. AI Crop Disease Detection Scan
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/disease-check/scan`
* **Body Type**: `form-data`
* **Form Field**: `crop_hint` = `paddy`

---

### 11. Chat & Escrow — Get History
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/api/v1/chat/messages`

---

### 12. Chat & Escrow — Send Message
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/chat/send`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "sender": "Farmer Gurpreet",
    "message": "I can deliver the paddy straw by 10 AM tomorrow."
  }
  ```

---

### 13. Wallet — Get Balance & Transactions
* **Method**: `GET`
* **URL**: `http://127.0.0.1:8080/api/v1/wallet/balance`

---

### 14. Wallet — Accept & Lock Escrow
* **Method**: `POST`
* **URL**: `http://127.0.0.1:8080/api/v1/wallet/escrow/accept`
* **Header**: `Content-Type: application/json`
* **Body**:
  ```json
  {
    "amount": 56350,
    "deal_id": "DEAL-9081"
  }
  ```
