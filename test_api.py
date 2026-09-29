import urllib.request
import urllib.error
import json
import uuid

BASE_URL = "https://krishisetu-backend-d73j.onrender.com"
API_URL = f"{BASE_URL}/api/v1"

def make_request(method, url, data=None, headers=None):
    if headers is None:
        headers = {}
    if data is not None and not isinstance(data, bytes):
        data = json.dumps(data).encode("utf-8")
        headers["Content-Type"] = "application/json"
        
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            status = response.status
            body = response.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8")
    
    return status, body

def print_result(name, status, body):
    print(f"--- {name} ---")
    print(f"Status: {status}")
    try:
        print("Response:", json.dumps(json.loads(body), indent=2))
    except:
        print("Response:", body)
    print("\n")

def test_apis():
    # 1. Health
    status, body = make_request("GET", f"{BASE_URL}/health")
    print_result("Health Check", status, body)

    # 2. Register
    uid = uuid.uuid4().hex[:6]
    test_user_email = f"test_{uid}@example.com"
    test_phone = f"+9198{uuid.uuid4().int % 100000000:08d}"
    register_payload = {
        "full_name": "Test User",
        "email": test_user_email,
        "phone": test_phone,
        "password": "Password123!"
    }
    status, body = make_request("POST", f"{API_URL}/auth/register", data=register_payload)
    print_result("Register User", status, body)

    # 3. Login
    login_payload = {
        "username": test_phone,
        "password": "Password123!"
    }
    status, body = make_request("POST", f"{API_URL}/auth/login", data=login_payload)
    print_result("Login User (JSON)", status, body)
    
    # Check if we need form data for login
    if status == 422:
        print("Login failed with JSON, trying Form Data (OAuth2)")
        import urllib.parse
        form_data = urllib.parse.urlencode({
            "username": test_user_email,
            "password": "Password123"
        }).encode("utf-8")
        headers = {"Content-Type": "application/x-www-form-urlencoded"}
        status, body = make_request("POST", f"{API_URL}/auth/login", data=form_data, headers=headers)
        print_result("Login User (Form Data)", status, body)
    
    # 4. Try another endpoint like /waste
    status, body = make_request("GET", f"{API_URL}/waste?category=all")
    print_result("Get Waste Listings", status, body)

if __name__ == "__main__":
    test_apis()
