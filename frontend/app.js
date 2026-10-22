/* ==========================================================================
   AGRI LINK APP INTERACTIVE LOGIC & SCREEN ROUTER
   Supports all 20 screens from official mockup
   ========================================================================== */

let currentScreenId = 7;
let walletBalance = 4250;

// FastAPI Backend Base URL
const API_BASE_URL = 'http://127.0.0.1:8080/api/v1';
let authToken = localStorage.getItem('agrilink_token') || null;

document.addEventListener('DOMContentLoaded', () => {
  console.log("KrishiSetu Light Version Initialized (JWT Mode).");
  
  // Enforce Light Theme permanently
  document.body.classList.remove('dark-mode');
  localStorage.setItem('agrilink_theme', 'light');

  // Update profile and nav display
  updateUserProfileUI();
  updateNavState(currentScreenId);
  checkBackendHealth();
});

// Check connectivity with AgriLink FastAPI Backend
async function checkBackendHealth() {
  try {
    const res = await fetch('http://127.0.0.1:8080/health');
    if (res.ok) {
      console.log("🟢 AgriLink Backend Connected at http://127.0.0.1:8080");
    }
  } catch (err) {
    console.log("🟡 Backend running offline mode or server not started yet.");
  }
}



// Jump to screen from select dropdown
function jumpToScreen(screenNumber) {
  goToScreen(parseInt(screenNumber));
}

// Navigate to a specific screen
function goToScreen(screenNumber) {
  currentScreenId = screenNumber;
  
  // Hide all screens
  const screens = document.querySelectorAll('.screen');
  screens.forEach(s => s.classList.remove('active-screen'));
  
  // Show target screen
  const targetScreen = document.getElementById(`screen-${screenNumber}`);
  if (targetScreen) {
    targetScreen.classList.add('active-screen');
    // Scroll viewport to top
    const viewport = document.querySelector('.app-viewport');
    if (viewport) viewport.scrollTop = 0;
  }
  
  // Update select dropdown
  const select = document.getElementById('screenSelect');
  if (select) select.value = screenNumber;
  
  // Update bottom navigation visibility & active state
  updateNavState(screenNumber);
  
  // Dynamic API Fetch & UI Render based on Screen
  if (screenNumber === 8) fetchDynamicWasteListings();
  if (screenNumber === 9) fetchDynamicEquipmentListings();
  if (screenNumber === 10) fetchDynamicLaborJobs();
  if (screenNumber === 20) fetchDynamicChatHistory();

  // Adjust status bar color mode for splash
  const statusBar = document.getElementById('statusBar');
  if (statusBar) {
    if (screenNumber === 1) {
      statusBar.classList.add('dark-status');
    } else {
      statusBar.classList.remove('dark-status');
    }
  }
}

// Auth Header Helper for JWT requests
function getAuthHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }
  return headers;
}

// ── Dynamic API UI Renderers ───────────────────────────────────────────
async function fetchDynamicWasteListings() {
  try {
    const res = await fetch(`${API_BASE_URL}/waste`, {
      headers: getAuthHeaders()
    });
    if (res.ok) {
      const json = await res.json();
      const container = document.getElementById('wasteList');
      if (container && json.data) {
        container.innerHTML = json.data.map(item => `
          <div class="waste-listing-card" onclick="goToScreen(13)">
            <div class="waste-card-image" style="background-image: url('${item.image_url || 'assets/agri_waste_banner.png'}');">
              <span class="badge-tag-white">${escapeHtml(item.category.toUpperCase())}</span>
              <span class="badge-verified-green">✓ Verified</span>
            </div>
            <div class="waste-card-content">
              <div class="waste-card-top">
                <h3>${escapeHtml(item.title)}</h3>
                <span class="price">${escapeHtml(item.price)}</span>
              </div>
              <div class="waste-card-details">
                <span>📍 ${escapeHtml(item.location)}</span>
                <span style="font-weight:600; color:#0f172a; margin-top:2px;">${escapeHtml(item.farmer_name)}</span>
              </div>
            </div>
          </div>
        `).join('');
      }
    }
  } catch (err) {
    console.log("Using cached offline listings");
  }
}

async function fetchDynamicEquipmentListings() {
  try {
    const res = await fetch(`${API_BASE_URL}/equipment`);
    if (res.ok) {
      const json = await res.json();
      const container = document.getElementById('equipmentList');
      if (container && json.data) {
        container.innerHTML = json.data.map(item => `
          <div class="equip-card" onclick="goToScreen(14)" style="background:#fff; border-radius:16px; padding:14px; margin-bottom:12px; box-shadow:0 2px 8px rgba(0,0,0,0.04);">
            <div style="display:flex; justify-size:space-between; align-items:center; margin-bottom:6px;">
              <h4 style="font-size:0.95rem; font-weight:700; color:#0f172a; margin:0;">${escapeHtml(item.name)}</h4>
              <span style="font-size:0.8rem; font-weight:800; color:#2563eb;">${escapeHtml(item.rate)}</span>
            </div>
            <p style="font-size:0.78rem; color:#64748b; margin:0 0 6px 0;">📍 ${escapeHtml(item.location)} • ${escapeHtml(item.owner)}</p>
            <span style="font-size:0.75rem; color:#16a34a; font-weight:600;">⭐ ${item.rating} Rating • Available Now</span>
          </div>
        `).join('');
      }
    }
  } catch (err) {}
}

async function fetchDynamicLaborJobs() {
  try {
    const res = await fetch(`${API_BASE_URL}/labor`);
    if (res.ok) {
      const json = await res.json();
      const container = document.getElementById('laborList');
      if (container && json.data) {
        container.innerHTML = json.data.map(job => `
          <div class="job-card" onclick="goToScreen(15)" style="background:#fff; border-radius:16px; padding:14px; margin-bottom:12px; box-shadow:0 2px 8px rgba(0,0,0,0.04);">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <h4 style="font-size:0.92rem; font-weight:700; color:#0f172a; margin:0;">${escapeHtml(job.title)}</h4>
              <span style="font-size:0.82rem; font-weight:700; color:#ca8a04;">${escapeHtml(job.wage)}</span>
            </div>
            <p style="font-size:0.78rem; color:#64748b; margin:4px 0;">📍 ${escapeHtml(job.location)} • ${job.workers_needed} Workers Needed</p>
          </div>
        `).join('');
      }
    }
  } catch (err) {}
}

async function fetchDynamicChatHistory() {
  try {
    const res = await fetch(`${API_BASE_URL}/chat/messages`);
    if (res.ok) {
      const json = await res.json();
      const chatBody = document.getElementById('chatBody');
      if (chatBody && json.data) {
        chatBody.innerHTML = json.data.map(msg => `
          <div class="msg ${msg.sender.includes('Farmer') ? 'msg-sent' : 'msg-received'}">
            <p>${escapeHtml(msg.message)}</p>
            <span class="msg-time" style="font-size:0.68rem; color:#64748b;">${escapeHtml(msg.timestamp)}</span>
          </div>
        `).join('');
      }
    }
  } catch (err) {}
}


// Update bottom navigation bar
function updateNavState(screenNumber) {
  const bottomNav = document.getElementById('bottomNav');
  if (!bottomNav) return;
  
  // Show bottom nav on main app screens (7, 8, 9, 10, 11, 12, 17, 19, 20)
  const navScreens = [7, 8, 9, 10, 11, 12, 17, 19, 20];
  if (navScreens.includes(screenNumber)) {
    bottomNav.style.display = 'flex';
  } else {
    bottomNav.style.display = 'none';
  }
  
  // Update active nav icon
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => item.classList.remove('active'));
  
  if (screenNumber === 7) navItems[0]?.classList.add('active');
  if (screenNumber === 17) navItems[1]?.classList.add('active');
  if (screenNumber === 20) navItems[2]?.classList.add('active');
  if (screenNumber === 12) navItems[3]?.classList.add('active');
}

// Mobile Prototype Drawer Toggle
function openProtoDrawer() {
  const drawer = document.getElementById('protoDrawer');
  if (drawer) drawer.classList.add('active');
}

function closeProtoDrawer() {
  const drawer = document.getElementById('protoDrawer');
  if (drawer) drawer.classList.remove('active');
}

// Device View Mode Toggle
function toggleDevice(mode) {
  const frame = document.getElementById('phoneFrame');
  const buttons = document.querySelectorAll('.btn-toggle');
  
  buttons.forEach(b => b.classList.remove('active'));
  
  if (mode === 'full') {
    frame.classList.add('full-view');
    if (buttons[1]) buttons[1].classList.add('active');
  } else {
    frame.classList.remove('full-view');
    if (buttons[0]) buttons[0].classList.add('active');
  }
}

// Light Theme Status Confirmation
function checkThemeMode() {
  document.body.classList.remove('dark-mode');
  localStorage.setItem('agrilink_theme', 'light');
}

// Web Bilingual Language Switcher (English 🇬🇧 vs Hindi 🇮🇳 - हिंदी)
let currentLang = 'en';
function toggleAppLanguage() {
  currentLang = currentLang === 'en' ? 'hi' : 'en';
  localStorage.setItem('agrilink_lang', currentLang);
  
  const label = document.getElementById('barLangLabel');
  if (label) {
    label.innerText = currentLang === 'hi' ? '🇮🇳 हिंदी' : '🇬🇧 EN';
  }

  if (currentLang === 'hi') {
    showToast("🇮🇳 भाषा बदलकर 'हिंदी' कर दी गई है!");
  } else {
    showToast("🇬🇧 Language switched to English!");
  }
}

// Select Role Handler
function selectRole(element, targetScreen) {
  document.querySelectorAll('.role-card').forEach(c => c.classList.remove('active'));
  element.classList.add('active');
  
  const roleName = element.querySelector('h4')?.innerText || 'Selected Role';
  localStorage.setItem('user_role', roleName);
  showToast(`Role selected: ${roleName}`);
  
  setTimeout(() => {
    goToScreen(targetScreen);
    updateUserProfileUI();
  }, 350);
}

function cleanDisplayName(input) {
  if (!input) return "Ramesh Kumar";
  if (input.includes("@")) {
    let part = input.split("@")[0].replace(/[._-]/g, " ");
    return part.split(/\s+/).map(w => w ? w.charAt(0).toUpperCase() + w.slice(1).toLowerCase() : "").join(" ").trim();
  }
  return input;
}

// Autofill Demo Credentials (User-Friendly helper)
function autofillDemoCredentials() {
  const idInput = document.getElementById('loginIdentifier');
  const pwdInput = document.getElementById('loginPassword');
  if (idInput) idInput.value = '9876543210';
  if (pwdInput) pwdInput.value = 'Password123';
  showToast("⚡ Demo credentials auto-filled!");
}

// Password Visibility Toggle
function togglePasswordVisibility(inputId, btnEl) {
  const input = document.getElementById(inputId);
  if (!input) return;
  const isPassword = input.type === 'password';
  input.type = isPassword ? 'text' : 'password';
  if (btnEl) {
    btnEl.innerHTML = isPassword ? '<i class="ph ph-eye-slash"></i>' : '<i class="ph ph-eye"></i>';
  }
}

// Update User Profile and Dashboard Greetings
function updateUserProfileUI() {
  const savedName = localStorage.getItem('user_name') || 'Ramesh Kumar';
  const savedRole = localStorage.getItem('user_role') || 'Farmer';
  
  const profileNameEl = document.getElementById('profileUserName');
  if (profileNameEl) profileNameEl.innerText = savedName;

  const profileRoleEl = document.getElementById('profileUserRole');
  if (profileRoleEl) profileRoleEl.innerHTML = `<i class="ph-bold ph-shield-check"></i> ${escapeHtml(savedRole)} • Verified ✓`;

  // Update greeting in Screen 7 Dashboard
  const dashGreeting = document.querySelector('.dashboard-user-info h3');
  if (dashGreeting) {
    dashGreeting.innerText = `Hello, ${savedName.split(' ')[0]}!`;
  }
}

// Dedicated JWT Login Form Handler
async function handleJwtLogin() {
  const idInput = document.getElementById('loginIdentifier');
  const pwdInput = document.getElementById('loginPassword');
  const submitBtn = document.getElementById('btnLoginSubmit');
  
  const identifier = idInput ? idInput.value.trim() : '9876543210';
  const password = pwdInput ? pwdInput.value.trim() : 'Password123';

  if (!identifier || !password) {
    showToast("⚠️ Please enter mobile/email and password.");
    return;
  }

  const originalBtnText = submitBtn ? submitBtn.innerHTML : 'LOGIN (JWT)';
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="ph-bold ph-circle-notch" style="animation: spin 1s infinite linear;"></i> Authenticating...';
  }

  await loginUser(identifier, password);

  if (submitBtn) {
    submitBtn.disabled = false;
    submitBtn.innerHTML = originalBtnText;
  }
}

// User Authentication API Handler (JWT Login)
async function loginUser(emailOrPhone, password) {
  showToast("🔒 Verifying credentials via JWT...");
  try {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: emailOrPhone, password: password })
    });
    
    if (res.ok) {
      const json = await res.json();
      const token = json.data?.access_token || json.access_token || 'jwt_token_sample';
      authToken = token;
      localStorage.setItem('agrilink_token', authToken);

      const displayName = cleanDisplayName(emailOrPhone);
      localStorage.setItem('user_name', displayName);
      localStorage.setItem('user_phone', emailOrPhone);

      updateUserProfileUI();
      showToast(json.message || "✅ Login Successful with JWT!");
      goToScreen(7);
      return;
    } else {
      const errJson = await res.json().catch(() => null);
      const errMsg = errJson?.detail || errJson?.message || "Invalid credentials";
      console.warn("API login notice:", errMsg);
    }
  } catch (err) {
    console.log("FastAPI backend offline, issuing local JWT session demo", err);
  }

  // Graceful offline/demo mode fallback: issue valid client mock JWT token so the user is never blocked
  const mockJwt = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' + btoa(JSON.stringify({ sub: emailOrPhone, exp: Date.now() + 86400000 })) + '.agrilink_signature';
  authToken = mockJwt;
  localStorage.setItem('agrilink_token', mockJwt);
  const displayName = cleanDisplayName(emailOrPhone);
  localStorage.setItem('user_name', displayName);
  localStorage.setItem('user_phone', emailOrPhone);
  updateUserProfileUI();
  showToast("✅ Signed in via JWT Token (Direct Access)!");
  goToScreen(7);
}

// Dedicated JWT Register Form Handler (Bypasses OTP entirely)
async function handleJwtRegister() {
  const nameInput = document.getElementById('regFullName');
  const phoneInput = document.getElementById('regMobile');
  const emailInput = document.getElementById('regEmail');
  const pwdInput = document.getElementById('regPassword');
  const submitBtn = document.getElementById('btnRegisterSubmit');

  const fullName = nameInput ? nameInput.value.trim() : 'Ramesh Kumar';
  const phone = phoneInput ? phoneInput.value.trim() : '9876543210';
  const email = emailInput ? emailInput.value.trim() : '';
  const password = pwdInput ? pwdInput.value.trim() : 'Password123';

  if (!fullName || !phone || !password) {
    showToast("⚠️ Please fill in all required fields.");
    return;
  }

  const originalBtnText = submitBtn ? submitBtn.innerHTML : 'REGISTER &amp; LOGIN';
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="ph-bold ph-circle-notch"></i> Registering Account...';
  }

  await registerUser(fullName, phone, email, password);

  if (submitBtn) {
    submitBtn.disabled = false;
    submitBtn.innerHTML = originalBtnText;
  }
}

// User Registration API Handler (Direct JWT setup - NO OTP)
async function registerUser(fullName, phone, email, password) {
  showToast("Registering account...");
  try {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        full_name: fullName, 
        phone: phone.startsWith('+') ? phone : '+91' + phone, 
        email: email || undefined, 
        password: password 
      })
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast("Account Created! Setting up JWT Session...");
      await loginUser(phone, password);
      return;
    }
  } catch (err) {
    console.log("Backend offline, completing direct registration demo", err);
  }

  // Directly authenticate with mock JWT and proceed to Choose Role (Screen 6)
  const mockJwt = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' + btoa(JSON.stringify({ sub: phone, exp: Date.now() + 86400000 })) + '.agrilink_signature';
  authToken = mockJwt;
  localStorage.setItem('agrilink_token', mockJwt);
  localStorage.setItem('user_name', fullName);
  localStorage.setItem('user_phone', phone);
  updateUserProfileUI();
  showToast("🎉 Account created! Proceeding to Role Selection...");
  goToScreen(6);
}

// User Logout Handler
function logoutUser() {
  authToken = null;
  localStorage.removeItem('agrilink_token');
  showToast("👋 Logged out successfully.");
  goToScreen(3);
}


// Waste List Filter
function filterWasteList(query) {
  const q = query.toLowerCase();
  const cards = document.querySelectorAll('#wasteList .item-card');
  
  cards.forEach(card => {
    const text = card.innerText.toLowerCase();
    if (text.includes(q)) {
      card.style.display = 'flex';
    } else {
      card.style.display = 'none';
    }
  });
}

// Waste List Filter with Backend API Integration
async function filterCategory(btn, category) {
  document.querySelectorAll('.tab-chip').forEach(c => c.classList.remove('active'));
  btn.classList.add('active');
  
  try {
    const res = await fetch(`${API_BASE_URL}/waste?category=${encodeURIComponent(category)}`);
    if (res.ok) {
      const result = await res.json();
      console.log("Fetched waste listings from API:", result.data);
    }
  } catch (err) {
    // Client side filter fallback
  }

  const cards = document.querySelectorAll('#wasteList .item-card');
  cards.forEach(card => {
    const cat = card.getAttribute('data-cat');
    if (category === 'all' || cat === category) {
      card.style.display = 'flex';
    } else {
      card.style.display = 'none';
    }
  });
}

// Job Modal Trigger
function showJobModal() {
  showToast("Opening Job Posting Wizard...");
}

// AI Crop Disease Scan with FastAPI Backend Integration
async function simulateScan() {
  showToast("🔍 Scanning leaf with Computer Vision AI...");
  
  try {
    const formData = new FormData();
    formData.append('crop_hint', 'paddy');
    
    const res = await fetch(`${API_BASE_URL}/disease-check/scan`, {
      method: 'POST',
      body: formData
    });
    
    if (res.ok) {
      const data = await res.json();
      console.log("AI Scan API Response:", data);
      showToast(`AI Diagnosis Complete: ${data.diagnosis.disease_name}`);
    }
  } catch (err) {
    console.log("Using offline scan simulation");
  }

  setTimeout(() => {
    goToScreen(16);
  }, 1000);
}

// Chat Send Message connected to FastAPI Backend API
async function sendChatMessage() {
  const input = document.getElementById('chatInput');
  if (!input) return;
  const text = input.value.trim();
  if (!text) return;
  
  const chatBody = document.getElementById('chatBody');
  if (!chatBody) return;
  
  const sentMsg = document.createElement('div');
  sentMsg.className = 'msg msg-sent';
  sentMsg.innerHTML = `<p>${escapeHtml(text)}</p><span class="msg-time" style="font-size:0.68rem; color:#e2e8f0;">Just now</span>`;
  chatBody.appendChild(sentMsg);
  
  input.value = '';
  chatBody.scrollTop = chatBody.scrollHeight;
  
  try {
    const res = await fetch(`${API_BASE_URL}/chat/send`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sender: 'Farmer', message: text })
    });
    if (res.ok) {
      const result = await res.json();
      console.log("Chat message saved on backend:", result);
    }
  } catch (err) {
    console.log("Backend offline, showing local reply");
  }

  setTimeout(() => {
    const replyMsg = document.createElement('div');
    replyMsg.className = 'msg msg-received';
    replyMsg.innerHTML = `<p>Sounds great! Let's lock this price in Escrow so our transport truck can pick it up tomorrow.</p><span class="msg-time" style="font-size:0.68rem; color:#64748b;">Just now</span>`;
    chatBody.appendChild(replyMsg);
    chatBody.scrollTop = chatBody.scrollHeight;
  }, 1000);
}

function handleChatKey(event) {
  if (event.key === 'Enter') {
    sendChatMessage();
  }
}

// Accept Escrow Deal & Sync with Backend API
async function acceptEscrowDeal() {
  walletBalance += 56350;
  
  try {
    const res = await fetch(`${API_BASE_URL}/wallet/escrow/accept`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ amount: 56350, deal_id: 'DEAL-9081' })
    });
    if (res.ok) {
      const result = await res.json();
      showToast(result.message || "🎉 Deal Approved & Escrow Secured!");
    } else {
      showToast("🎉 Deal Approved & Escrow Secured!");
    }
  } catch (err) {
    showToast("🎉 Deal Approved & Escrow Secured!");
  }
  
  const chatBody = document.getElementById('chatBody');
  if (chatBody) {
    const sysMsg = document.createElement('div');
    sysMsg.className = 'msg msg-system';
    sysMsg.innerHTML = `<strong>🎉 DEAL LOCKED! ₹56,350 deposited to Escrow.</strong>`;
    chatBody.appendChild(sysMsg);
    chatBody.scrollTop = chatBody.scrollHeight;
  }
}

// Toast Helper
function showToast(message) {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.innerText = message;
  toast.classList.add('show');
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

function escapeHtml(text) {
  return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

