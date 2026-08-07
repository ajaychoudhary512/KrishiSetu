# 🌾 AgriLink AI — Fullstack Smart Agriculture Marketplace

AgriLink AI is an all-in-one smart agriculture platform connecting farmers, buyers, equipment owners, and laborers. It features waste marketplace, equipment rental, labor hiring, computer vision AI crop disease diagnosis, real-time chat, and escrow payments.

---

## 📁 Project Structure (VS Code Workspace)

```text
c:\Agrilink\
├── index.html            # Main 20-Screen App Frontend UI
├── styles.css            # Modern UI Styling & Design Tokens
├── app.js                # Interactive UI Logic & API Integration
├── start_backend.bat     # 🚀 One-Click Launcher for Backend Server
├── README.md             # Project Guide & Documentation
└── backend/              # 🐍 FastAPI Backend Server
    ├── app/
    │   ├── api/v1/endpoints/  # REST APIs (Waste, Equipment, Labor, AI Disease, Chat, Wallet)
    │   ├── auth/              # JWT Auth & Security
    │   ├── database/          # Database connection & Alembic sessions
    │   ├── models/            # SQLAlchemy DB Models
    │   ├── schemas/           # Pydantic Input/Output Validation Schemas
    │   └── main.py            # FastAPI Application Entry Point
    ├── .env                   # Server Environment Configuration
    └── requirements.txt       # Python Dependencies
```

---

## 🚀 How to Run the App in VS Code

### 1. Start the FastAPI Backend Server
Run the launcher script directly from VS Code Terminal or Windows Explorer:
```cmd
start_backend.bat
```
* Or manually run in terminal:
  ```cmd
  cd backend
  python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
  ```
* **API Documentation (Swagger UI)**: Open [http://localhost:8000/docs](http://localhost:8000/docs)
* **ReDoc UI**: Open [http://localhost:8000/redoc](http://localhost:8000/redoc)
* **Health Check**: Open [http://localhost:8000/health](http://localhost:8000/health)

### 2. Launch the Web Frontend
* Open `index.html` in your browser or click **Live Server** in VS Code.
* All 20 screens will interact dynamically with your live FastAPI backend!
