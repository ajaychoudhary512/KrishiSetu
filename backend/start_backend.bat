@echo off
echo ===================================================
echo 🚀 Starting AgriLink AI FastAPI Backend Server
echo Base URL: http://127.0.0.1:8080/api/v1
echo Swagger Docs: http://127.0.0.1:8080/docs
echo Health Check: http://127.0.0.1:8080/health
echo ===================================================
cd /d "%~dp0backend"

set PYTHON_CMD=python
if exist "C:\Users\AJAY CHOUDHARY\AppData\Local\Python\pythoncore-3.14-64\python.exe" (
    set "PYTHON_CMD=C:\Users\AJAY CHOUDHARY\AppData\Local\Python\pythoncore-3.14-64\python.exe"
)

"%PYTHON_CMD%" -m uvicorn app.main:app --host 0.0.0.0 --port 8080 --reload
pause
