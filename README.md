# 🌾 KrishiSetu (AgriLink AI) — Smart Agriculture Marketplace

**KrishiSetu** is an all-in-one, AI-powered agricultural marketplace platform connecting farmers, buyers, equipment owners, and agricultural laborers. It features an agricultural waste marketplace, equipment rental & sales, labor hiring, AI computer vision crop disease detection, real-time negotiation chat, and secure wallet payments.

---

## 📁 Repository Structure

```text
KrishiSetu/
├── 🐍 backend/                 # FastAPI Python REST API Server
│   ├── app/                    # Application source code (Controllers, Models, Services)
│   ├── AgriLink_AI_Postman_Collection.json # Postman API Test Collection
│   ├── start_backend.bat       # One-click Windows server launcher
│   └── requirements.txt        # Python dependencies
├── 🌐 frontend/                # Web Application UI (HTML5, Vanilla CSS, JS)
│   ├── index.html              # Main interactive 20-screen Web Application
│   ├── styles.css              # Custom styling & modern design tokens
│   └── app.js                  # Frontend API integration & state management
├── 📱 app/                     # Android Studio Application (Java)
│   └── src/main/java/com/agrilink/app/ # Android Activities, Fragments, Adapters
├── .gitignore                  # Production Git exclusion rules
└── README.md                   # Master Documentation
```

---

## 🚀 Getting Started

### 1. Run the Backend API Server

Navigate to the `backend` directory and start the server:

```cmd
cd backend
python -m uvicorn app.main:app --host 0.0.0.0 --port 8080 --reload
```

*Or on Windows, double-click `backend/start_backend.bat`.*

* **Swagger UI (Interactive API Docs)**: [http://127.0.0.1:8080/docs](http://127.0.0.1:8080/docs)
* **ReDoc Documentation**: [http://127.0.0.1:8080/redoc](http://127.0.0.1:8080/redoc)
* **Health Check**: [http://127.0.0.1:8080/health](http://127.0.0.1:8080/health)

---

### 2. Run the Web Frontend

Open `frontend/index.html` in any web browser, or serve it using Live Server in VS Code. All features and 20 screens will dynamically communicate with the live FastAPI backend.

---

### 3. Run the Android Mobile App

1. Launch **Android Studio**.
2. Select **Open** and select the project directory.
3. Build & Run on an Android Emulator or connected device.

---

## 🧪 API Endpoints Overview

| Category | Method | Endpoint | Description |
|---|---|---|---|
| **Health** | `GET` | `/health` | Server status check |
| **Auth** | `POST` | `/api/v1/auth/register` | User registration |
| **Auth** | `POST` | `/api/v1/auth/login` | User authentication & JWT token |
| **Waste** | `GET` | `/api/v1/waste` | Browse agri-waste listings |
| **Waste** | `POST` | `/api/v1/waste` | Post agri-waste listing |
| **Equipment** | `GET` | `/api/v1/equipment` | Browse equipment rentals |
| **Equipment** | `POST` | `/api/v1/equipment/book` | Book machinery rental |
| **Labor** | `GET` | `/api/v1/labor` | List labor requirements |
| **Labor** | `POST` | `/api/v1/labor/job` | Post labor job offer |
| **AI Check** | `POST` | `/api/v1/disease-check/scan` | AI Crop disease diagnosis |
| **Chat** | `GET` | `/api/v1/chat/messages` | Fetch deal chat history |
| **Chat** | `POST` | `/api/v1/chat/send` | Send negotiation message |
| **Wallet** | `GET` | `/api/v1/wallet/balance` | Get wallet balance |
| **Wallet** | `POST` | `/api/v1/wallet/escrow/accept` | Lock funds in escrow |

> **Postman Collection**: Import `backend/AgriLink_AI_Postman_Collection.json` into Postman for instant API testing.

---

## 🔒 Security & Privacy

- Sensitive files (`.env`, `agrilink.db`, virtual environments) are excluded via `.gitignore`.
- Production backend code has been cleaned of internal comments and docstrings.
- Secure JWT authentication for protected API endpoints.
