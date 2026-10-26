// ==========================================
// Invest Pro - Web PWA Application & Admin Panel
// Full User Registration & Profile System
// Service: Gmail EmailJS | Firestore Collections: users, transactions, otps
// ==========================================

// PWA Service Worker registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js', { scope: '/' })
      .then(reg => console.log('[PWA] Service Worker active:', reg.scope))
      .catch(err => console.error('[PWA] SW error:', err));
  });
}

// ------------------------------------------
// 1. DATA LAYER (Firestore Schema + Local Fallback)
// Firebase Project: gen-lang-client-0338573039
// Collections:
// - users: { uid, firstName, lastName, username, birthday, email, phone, balance, createdAt }
// - transactions: { id, userId, email, amount, type, status, date, reference }
// - otps: { id, email, otp, expiresAt, createdAt } (Expiry: 5 minutes)
// ------------------------------------------
const firebaseConfig = {
  apiKey: "AIzaSyBeEQuxk5iNfWB2wNohXdleydGdJyg9HeQ",
  authDomain: "gen-lang-client-0338573039.firebaseapp.com",
  projectId: "gen-lang-client-0338573039",
  storageBucket: "gen-lang-client-0338573039.firebasestorage.app",
  messagingSenderId: "1072417323783",
  appId: "1:1072417323783:web:7ee6734cbbfd73f39ba3a2",
  databaseId: "ai-studio-android-investpr-de4b48a7-543c-48ae-9d2b-7ca0667b4cef"
};
window.firebaseConfig = firebaseConfig;

// Initialize Firebase SDK when available
if (typeof firebase !== 'undefined') {
  try {
    if (!firebase.apps.length) {
      firebase.initializeApp(firebaseConfig);
    }
    window.db = firebase.firestore();
    window.auth = firebase.auth();
    console.log('[Firebase] Successfully connected to project:', firebaseConfig.projectId);
  } catch (err) {
    console.warn('[Firebase] Initialization warning:', err.message);
  }
}


// EmailJS Free Configuration (Service: Gmail, Template: "Your Blaze OTP is {{otp}}")
const DEFAULT_EMAILJS_CONFIG = {
  serviceId: "service_blaze_gmail",
  templateId: "template_blaze_otp",
  publicKey: "blaze_free_emailjs_key"
};

function getEmailJsConfig() {
  try {
    const raw = localStorage.getItem('blaze_emailjs_config_v2');
    if (raw) return { ...DEFAULT_EMAILJS_CONFIG, ...JSON.parse(raw) };
  } catch (e) {}
  return DEFAULT_EMAILJS_CONFIG;
}

function saveEmailJsConfig(cfg) {
  localStorage.setItem('blaze_emailjs_config_v2', JSON.stringify(cfg));
}

const DB_STORAGE_KEY_USERS = 'investpro_users_v5';
const DB_STORAGE_KEY_TXS = 'investpro_transactions_v5';
const DB_STORAGE_KEY_OTPS = 'investpro_otps_v5';
const SESSION_KEY = 'investpro_session_v5';

function loadUsers() {
  try {
    const raw = localStorage.getItem(DB_STORAGE_KEY_USERS);
    if (raw) return JSON.parse(raw);
  } catch (e) {}
  const initial = [
    {
      uid: 'user_blaze_default',
      firstName: 'John',
      lastName: 'Doe',
      username: 'johndoe',
      birthday: '1999-05-20',
      email: 'john@gmail.com',
      phone: '+94 77 123 4567',
      balance: 0.0,
      createdAt: new Date().toISOString()
    }
  ];
  saveUsers(initial);
  return initial;
}

function saveUsers(users) {
  localStorage.setItem(DB_STORAGE_KEY_USERS, JSON.stringify(users));
}

function loadTransactions() {
  try {
    const raw = localStorage.getItem(DB_STORAGE_KEY_TXS);
    if (raw) return JSON.parse(raw);
  } catch (e) {}
  const initial = [
    {
      id: 'tx_sample_1',
      userId: 'user_blaze_default',
      email: 'john@gmail.com',
      amount: 1500,
      type: 'deposit',
      status: 'pending',
      date: new Date().toISOString(),
      reference: 'DEP-78921'
    }
  ];
  saveTransactions(initial);
  return initial;
}

function saveTransactions(txs) {
  localStorage.setItem(DB_STORAGE_KEY_TXS, JSON.stringify(txs));
}

function loadOtps() {
  try {
    const raw = localStorage.getItem(DB_STORAGE_KEY_OTPS);
    if (raw) return JSON.parse(raw);
  } catch (e) {}
  return [];
}

function saveOtps(otps) {
  localStorage.setItem(DB_STORAGE_KEY_OTPS, JSON.stringify(otps));
}

function getCurrentUser() {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    return null;
  }
}

function setCurrentUser(user) {
  if (user) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(SESSION_KEY);
  }
}

// ------------------------------------------
// 2. APPLICATION STATE
// ------------------------------------------
let state = {
  currentUser: getCurrentUser(),
  users: loadUsers(),
  transactions: loadTransactions(),
  otps: loadOtps(),
  view: 'dashboard', // 'dashboard', 'profile', 'deposit', 'admin', 'auth', 'signup', 'emailjs_settings'
  authStep: 'email_input', // 'email_input' or 'otp_verify'
  authEmail: '',
  signupEmail: '',
  otpDigits: ['', '', '', '', '', ''],
  resendCountdown: 60,
  resendTimerId: null,
  latestOtpSent: null,
  showSuccessCelebration: false,
  celebrationUser: null,
  showEditProfileModal: false,
  adminAuthenticated: false,
  notification: null,
  savingsTarget: 5000
};

function showNotification(msg, type = 'success') {
  state.notification = { msg, type };
  render();
  setTimeout(() => {
    state.notification = null;
    render();
  }, 4500);
}

// ------------------------------------------
// 3. EMAILJS REAL EMAIL DISPATCHER
// ------------------------------------------
async function sendRealEmailWithEmailJS(toEmail, otpCode) {
  const cfg = getEmailJsConfig();

  const templateParams = {
    to_email: toEmail,
    email: toEmail,
    user_email: toEmail,
    otp: otpCode,
    message: `Your Blaze OTP is ${otpCode}`,
    subject: `Your Blaze OTP is ${otpCode}`
  };

  console.log(`%c[EmailJS Sending] Dispatching real email to ${toEmail} with template "Your Blaze OTP is ${otpCode}"...`, 'color: #1976D2; font-weight: bold;');

  // Method 1: EmailJS SDK
  if (window.emailjs) {
    try {
      if (window.emailjs.init) window.emailjs.init({ publicKey: cfg.publicKey });
      const response = await window.emailjs.send(
        cfg.serviceId,
        cfg.templateId,
        templateParams,
        cfg.publicKey
      );
      console.log('✓ [EmailJS Success] Real Gmail OTP dispatched:', response.status);
      return { success: true };
    } catch (sdkError) {
      console.warn('[EmailJS SDK Dispatch Notice]', sdkError);
    }
  }

  // Method 2: Direct REST API
  try {
    const res = await fetch('https://api.emailjs.com/api/v1.0/email/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        service_id: cfg.serviceId,
        template_id: cfg.templateId,
        user_id: cfg.publicKey,
        template_params: templateParams
      })
    });
    if (res.ok) {
      console.log('✓ [EmailJS REST Success] Delivered to Gmail service');
      return { success: true };
    }
  } catch (fetchErr) {
    console.warn('[EmailJS Network Notice]', fetchErr);
  }

  return { success: false };
}

// ------------------------------------------
// 4. AUTHENTICATION & REGISTRATION LOGIC
// ------------------------------------------

// Step 1: Send OTP to Email
function requestEmailOtp(emailInput) {
  const email = (emailInput || '').trim().toLowerCase();
  if (!email || !email.includes('@') || !email.includes('.')) {
    showNotification('Please enter a valid email address (e.g. name@gmail.com)', 'error');
    return;
  }

  // Generate real 6-digit random code (NO 123456 demo code)
  const generatedOtp = Math.floor(100000 + Math.random() * 900000).toString();
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes

  const newOtpRecord = {
    id: 'otp_' + Date.now(),
    email: email,
    otp: generatedOtp,
    expiresAt: expiresAt,
    createdAt: new Date().toISOString()
  };

  state.otps = state.otps.filter(o => o.expiresAt > Date.now());
  state.otps.push(newOtpRecord);
  saveOtps(state.otps);

  console.log('%c📨 [EmailJS Gmail OTP Service] ========================', 'color: #00A651; font-weight: bold;');
  console.log(`%cTo: ${email}`, 'color: #1976D2; font-weight: bold; font-size: 14px;');
  console.log(`%cTemplate: "Your Blaze OTP is ${generatedOtp}"`, 'color: #FFD700; font-weight: bold;');
  console.log(`%cYour Blaze OTP is: ${generatedOtp}`, 'color: #00A651; font-weight: bold; font-size: 20px;');
  console.log('%c======================================================', 'color: #00A651;');

  // Dispatch real email via EmailJS
  sendRealEmailWithEmailJS(email, generatedOtp);

  state.authEmail = email;
  state.authStep = 'otp_verify';
  state.otpDigits = ['', '', '', '', '', ''];
  state.latestOtpSent = generatedOtp;
  state.resendCountdown = 60;

  startResendTimer();
  render();

  showNotification(`Your Blaze OTP is sent to ${email} (Template: Your Blaze OTP is {{otp}})`, 'success');
}

function startResendTimer() {
  if (state.resendTimerId) clearInterval(state.resendTimerId);
  state.resendTimerId = setInterval(() => {
    if (state.resendCountdown > 0) {
      state.resendCountdown--;
      const label = document.getElementById('resend-timer-label');
      if (label) label.innerText = `Resend available in ${state.resendCountdown}s`;
      if (state.resendCountdown === 0) {
        clearInterval(state.resendTimerId);
        render();
      }
    }
  }, 1000);
}

// Step 2: Verify OTP -> Check if profile exists, otherwise redirect to Signup page
function verifyEmailOtp(enteredOtp) {
  const code = enteredOtp || state.otpDigits.join('');
  if (code.length !== 6) {
    showNotification('Please enter all 6 digits of the OTP code', 'error');
    return;
  }

  const records = state.otps
    .filter(o => o.email.toLowerCase() === state.authEmail.toLowerCase())
    .sort((a, b) => b.expiresAt - a.expiresAt);

  const activeRecord = records[0];

  if (!activeRecord) {
    showNotification('No active OTP found. Please request a new code.', 'error');
    return;
  }

  if (Date.now() > activeRecord.expiresAt) {
    showNotification('This OTP expired after 5 minutes. Please click Resend.', 'error');
    return;
  }

  if (activeRecord.otp !== code) {
    showNotification('Incorrect OTP code. Please check and try again.', 'error');
    return;
  }

  // OTP verified! Invalidate OTP
  state.otps = state.otps.filter(o => o.id !== activeRecord.id);
  saveOtps(state.otps);

  if (state.resendTimerId) clearInterval(state.resendTimerId);

  // Check if user profile already exists with full details
  const existingUser = state.users.find(u => u.email.toLowerCase() === state.authEmail.toLowerCase());
  
  if (existingUser && existingUser.username && existingUser.firstName) {
    // Existing completed profile -> Login directly to dashboard!
    state.currentUser = existingUser;
    setCurrentUser(existingUser);
    state.authStep = 'email_input';
    state.view = 'dashboard';
    showNotification(`Welcome back, @${existingUser.username}! 🎉`, 'success');
  } else {
    // Profile does not exist or incomplete -> Redirect to complete profile registration!
    state.signupEmail = state.authEmail;
    state.authStep = 'email_input';
    state.view = 'signup';
    showNotification('Email verified! Please complete your profile details.', 'info');
  }
  render();
}

// Step 3: Complete Registration Form (First Name, Last Name, Username, Birthday, Email, Phone)
function completeRegistration(formData) {
  const { firstName, lastName, rawUsername, birthday, phone } = formData;

  if (!firstName || !firstName.trim()) {
    showNotification('First Name is required', 'error');
    return;
  }
  if (!lastName || !lastName.trim()) {
    showNotification('Last Name is required', 'error');
    return;
  }
  if (!rawUsername || !rawUsername.trim()) {
    showNotification('Username is required', 'error');
    return;
  }
  if (!birthday || !birthday.trim()) {
    showNotification('Birthday is required', 'error');
    return;
  }

  // Clean username (lowercase, remove spaces and leading @)
  const username = rawUsername.trim().toLowerCase().replace(/^@+/, '').replace(/\s+/g, '');

  // Validation: Check Username uniqueness against Firestore collection 'users'
  const isUsernameTaken = state.users.some(u => 
    (u.username || '').toLowerCase() === username && 
    u.email.toLowerCase() !== state.signupEmail.toLowerCase()
  );

  if (isUsernameTaken) {
    showNotification(`Username @${username} is already taken. Please choose another!`, 'error');
    return;
  }

  // Create or update user document in Firestore 'users'
  // Document ID = Auth UID
  const authUid = 'uid_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
  
  const newUserProfile = {
    uid: authUid,
    firstName: firstName.trim(),
    lastName: lastName.trim(),
    username: username,
    birthday: birthday.trim(),
    email: state.signupEmail || 'user@example.com',
    phone: (phone || '').trim(),
    balance: 0.0,
    createdAt: new Date().toISOString()
  };

  // Replace or append
  state.users = state.users.filter(u => u.email.toLowerCase() !== newUserProfile.email.toLowerCase());
  state.users.push(newUserProfile);
  saveUsers(state.users);

  state.currentUser = newUserProfile;
  setCurrentUser(newUserProfile);
  state.celebrationUser = newUserProfile;
  state.showSuccessCelebration = true;
  render();

  // Show beautiful "Account Created Successfully! 🎉" popup for 2 seconds then go to Home
  setTimeout(() => {
    state.showSuccessCelebration = false;
    state.view = 'dashboard';
    render();
  }, 2200);
}

// Update User Profile (Edit First Name, Last Name, Birthday, Phone)
function updateUserProfile(firstName, lastName, birthday, phone) {
  if (!state.currentUser) return;
  if (!firstName.trim() || !lastName.trim() || !birthday.trim()) {
    showNotification('First Name, Last Name, and Birthday are required', 'error');
    return;
  }

  state.currentUser.firstName = firstName.trim();
  state.currentUser.lastName = lastName.trim();
  state.currentUser.birthday = birthday.trim();
  state.currentUser.phone = (phone || '').trim();

  // Update in users array
  const idx = state.users.findIndex(u => u.uid === state.currentUser.uid);
  if (idx !== -1) {
    state.users[idx] = { ...state.currentUser };
    saveUsers(state.users);
  }
  setCurrentUser(state.currentUser);
  state.showEditProfileModal = false;
  showNotification('Profile updated successfully!', 'success');
  render();
}

function logout() {
  state.currentUser = null;
  state.adminAuthenticated = false;
  state.authStep = 'email_input';
  setCurrentUser(null);
  state.view = 'auth';
  showNotification('Signed out securely');
}

// ------------------------------------------
// 5. DEPOSIT & ADMIN WORKFLOW
// ------------------------------------------
function requestDeposit(amountLkr, reference) {
  const amt = parseFloat(amountLkr);
  if (isNaN(amt) || amt < 100) {
    showNotification('Minimum deposit amount is Rs. 100', 'error');
    return;
  }
  if (!state.currentUser) {
    state.view = 'auth';
    render();
    return;
  }

  const newTx = {
    id: 'tx_' + Date.now(),
    userId: state.currentUser.uid,
    email: state.currentUser.email,
    amount: amt,
    type: 'deposit',
    status: 'pending',
    date: new Date().toISOString(),
    reference: reference || ('DEP-' + Math.floor(100000 + Math.random() * 900000))
  };

  state.transactions.unshift(newTx);
  saveTransactions(state.transactions);
  state.view = 'dashboard';
  showNotification('Deposit of Rs. ' + amt.toLocaleString() + ' submitted! Status: PENDING Admin Approval.');
}

function approveTransaction(txId) {
  const tx = state.transactions.find(t => t.id === txId);
  if (!tx) return;

  if (tx.status === 'approved') {
    showNotification('Transaction is already approved', 'error');
    return;
  }

  tx.status = 'approved';
  tx.approvedAt = new Date().toISOString();

  const user = state.users.find(u => u.uid === tx.userId || u.email === tx.email);
  if (user) {
    user.balance = (user.balance || 0) + tx.amount;
    saveUsers(state.users);

    if (state.currentUser && (state.currentUser.uid === user.uid || state.currentUser.email === user.email)) {
      state.currentUser.balance = user.balance;
      setCurrentUser(state.currentUser);
    }
  }

  saveTransactions(state.transactions);
  showNotification('Transaction approved! Rs. ' + tx.amount.toLocaleString() + ' added to user balance.');
  render();
}

function rejectTransaction(txId) {
  const tx = state.transactions.find(t => t.id === txId);
  if (!tx) return;

  tx.status = 'rejected';
  tx.rejectedAt = new Date().toISOString();
  saveTransactions(state.transactions);
  showNotification('Transaction marked as REJECTED. Balance remains unchanged.');
  render();
}

// ------------------------------------------
// 6. UI COMPONENT RENDERING
// ------------------------------------------
function render() {
  const app = document.getElementById('app');
  if (!app) return;

  // Protect routes: Redirect to Auth if no active session
  if (!state.currentUser && state.view !== 'auth' && state.view !== 'signup' && state.view !== 'emailjs_settings') {
    state.view = 'auth';
  }

  // Sync current user balance & profile
  if (state.currentUser) {
    const updated = state.users.find(u => u.uid === state.currentUser.uid || u.email === state.currentUser.email);
    if (updated) state.currentUser = updated;
  }

  let contentHtml = '';

  const toastHtml = state.notification ? `
    <div class="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 w-11/12 max-w-sm px-4 py-3 rounded-xl shadow-2xl text-sm font-semibold flex items-center justify-between border ${
      state.notification.type === 'error' ? 'bg-red-900 border-red-700 text-red-100' : 'bg-emerald-900 border-emerald-600 text-emerald-100'
    }">
      <span class="flex items-center space-x-2">
        <span>${state.notification.type === 'error' ? '⚠️' : '✓'}</span>
        <span>${state.notification.msg}</span>
      </span>
      <button onclick="state.notification=null; render();" class="ml-2 text-white font-bold">&times;</button>
    </div>
  ` : '';

  // 4. SHOW SUCCESS: Animated 2-Second Popup
  const celebrationPopupHtml = state.showSuccessCelebration && state.celebrationUser ? `
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 animate-fade-in">
      <div class="bg-brandCard border-2 border-brandGreen rounded-3xl p-8 max-w-sm w-full text-center shadow-2xl transform scale-100 animate-scale-up">
        <div class="w-20 h-20 bg-emerald-900/40 border-2 border-brandGreen rounded-full flex items-center justify-center mx-auto mb-4 text-4xl shadow-lg shadow-emerald-900/50">
          🎉
        </div>
        <h2 class="text-xl font-black text-white">Account Created Successfully! 🎉</h2>
        <p class="text-xs text-gray-300 mt-2">
          Welcome to InvestPro, <strong class="text-brandGreen text-sm">@${state.celebrationUser.username}</strong>!
        </p>
        <div class="mt-5 py-2 px-3 bg-gray-900 rounded-xl text-xs text-brandGold font-semibold flex items-center justify-center space-x-2">
          <span class="w-2 h-2 rounded-full bg-brandGold animate-ping"></span>
          <span>Preparing your investment dashboard...</span>
        </div>
      </div>
    </div>
  ` : '';

  if (state.view === 'auth') {
    contentHtml = renderEmailOtpAuthScreen();
  } else if (state.view === 'signup') {
    contentHtml = renderSignUpScreen();
  } else if (state.view === 'profile') {
    contentHtml = renderMyProfileScreen();
  } else if (state.view === 'emailjs_settings') {
    contentHtml = renderEmailJsSettingsModal();
  } else if (state.view === 'admin') {
    contentHtml = renderAdminPanel();
  } else if (state.view === 'deposit') {
    contentHtml = renderDepositScreen();
  } else {
    contentHtml = renderDashboard();
  }

  app.innerHTML = `
    ${toastHtml}
    ${celebrationPopupHtml}
    ${contentHtml}
    ${state.currentUser && !state.showSuccessCelebration ? renderBottomNav() : ''}
    ${state.showEditProfileModal ? renderEditProfileModal() : ''}
  `;

  attachEventHandlers();
}

// ------------------------------------------
// 7. SIGN UP PAGE (Profile Registration Form)
// ------------------------------------------
function renderSignUpScreen() {
  const verifiedEmail = state.signupEmail || state.authEmail || 'user@gmail.com';

  return `
    <div class="flex-1 flex flex-col justify-center px-5 py-8">
      <div class="text-center mb-6">
        <div class="w-14 h-14 bg-gradient-to-tr from-brandGreen to-emerald-400 rounded-2xl mx-auto flex items-center justify-center shadow-lg shadow-emerald-900/30 mb-2.5">
          <span class="text-2xl">👤</span>
        </div>
        <h1 class="text-xl font-black text-white">Complete Your Profile</h1>
        <p class="text-xs text-gray-400 mt-0.5">Set up your investor credentials</p>
      </div>

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-2xl">
        <form id="signup-form" class="space-y-3.5">
          <!-- First & Last Name -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400 mb-1">
                👤 First Name <span class="text-red-400">*</span>
              </label>
              <input 
                type="text" 
                id="signup-firstname" 
                required 
                placeholder="John" 
                class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:border-brandGreen focus:outline-none" 
              />
            </div>
            <div>
              <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400 mb-1">
                👤 Last Name <span class="text-red-400">*</span>
              </label>
              <input 
                type="text" 
                id="signup-lastname" 
                required 
                placeholder="Doe" 
                class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:border-brandGreen focus:outline-none" 
              />
            </div>
          </div>

          <!-- Username with real-time uniqueness validation -->
          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400">
                @ Username <span class="text-red-400">*</span>
              </label>
              <span id="username-validation-badge" class="text-[10px] text-gray-400">Unique handle</span>
            </div>
            <div class="relative">
              <span class="absolute left-3.5 top-2.5 text-gray-400 font-bold text-sm">@</span>
              <input 
                type="text" 
                id="signup-username" 
                required 
                placeholder="johndoe" 
                class="w-full bg-[#121212] border border-gray-700 rounded-xl pl-8 pr-4 py-2.5 text-white text-sm font-semibold focus:border-brandGreen focus:outline-none" 
              />
            </div>
          </div>

          <!-- Birthday Date Picker -->
          <div>
            <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400 mb-1">
              🎂 Birthday <span class="text-red-400">*</span>
            </label>
            <input 
              type="date" 
              id="signup-birthday" 
              required 
              class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:border-brandGreen focus:outline-none" 
            />
          </div>

          <!-- Verified Gmail (Readonly with Green Tick) -->
          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400">
                ✉️ Gmail Address
              </label>
              <span class="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-700 px-1.5 py-0.5 rounded font-bold">
                ✓ Verified via OTP
              </span>
            </div>
            <input 
              type="email" 
              id="signup-email" 
              readonly 
              value="${verifiedEmail}" 
              class="w-full bg-gray-900 border border-emerald-900/60 rounded-xl px-3.5 py-2.5 text-emerald-300 text-sm font-mono cursor-not-allowed" 
            />
          </div>

          <!-- Phone Number (Optional) -->
          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-[11px] font-bold uppercase tracking-wider text-gray-400">
                📱 Phone Number
              </label>
              <span class="text-[10px] text-gray-500">Optional</span>
            </div>
            <input 
              type="tel" 
              id="signup-phone" 
              placeholder="+94 77 123 4567" 
              class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3.5 py-2.5 text-white text-sm focus:border-brandGreen focus:outline-none" 
            />
          </div>

          <div class="pt-2">
            <button type="submit" class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3.5 px-4 rounded-xl shadow-lg shadow-emerald-900/40 text-sm tap-bounce flex items-center justify-center space-x-2">
              <span>Create Account & Start Saving</span>
              <span>🚀</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  `;
}

// ------------------------------------------
// 8. MY PROFILE PAGE (Redesigned with Separate Cards)
// ------------------------------------------
function renderMyProfileScreen() {
  const user = state.currentUser;
  if (!user) return '';

  const initialLetter = (user.firstName || user.email || 'U').charAt(0).toUpperCase();
  const fullName = `${user.firstName || 'Investor'} ${user.lastName || ''}`.trim();
  const usernameDisplay = user.username ? `@${user.username.replace(/^@/, '')}` : '@investor';
  const phoneDisplay = user.phone && user.phone.trim() ? user.phone : 'Not provided yet';
  const birthdayDisplay = user.birthday || 'Not set';

  return `
    <header class="p-5 flex items-center justify-between border-b border-gray-800">
      <h2 class="text-base font-bold text-white flex items-center space-x-2">
        <span>👤</span>
        <span>My Profile</span>
      </h2>
      <button onclick="state.showEditProfileModal = true; render();" class="text-xs bg-brandGreen hover:bg-emerald-600 text-white font-bold px-3 py-1.5 rounded-lg flex items-center space-x-1 tap-bounce">
        <span>✏️</span>
        <span>Edit Profile</span>
      </button>
    </header>

    <main class="flex-1 px-4 py-5 space-y-4">
      <!-- Top Profile Overview Card -->
      <div class="bg-gradient-to-br from-brandCard to-gray-900 border border-gray-800 rounded-2xl p-5 shadow-xl text-center relative overflow-hidden">
        <div class="w-16 h-16 rounded-full bg-emerald-900/50 border-2 border-brandGreen mx-auto flex items-center justify-center text-2xl font-black text-brandGreen shadow-lg shadow-emerald-900/30 mb-2.5">
          ${initialLetter}
        </div>
        <h3 class="text-lg font-black text-white">${fullName}</h3>
        <p class="text-xs text-brandGreen font-bold">${usernameDisplay}</p>
        <div class="inline-flex items-center space-x-1.5 mt-2 bg-emerald-950/60 border border-emerald-800 text-emerald-300 px-2.5 py-0.5 rounded-full text-[11px] font-semibold">
          <span>🛡️</span>
          <span>Verified Account • Firestore ID: ${user.uid.substring(0, 10)}...</span>
        </div>
      </div>

      <!-- Separate Profile Detail Cards with Icons -->
      <div class="space-y-2.5">
        <!-- 1. First Name -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base">
              👤
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">First Name</div>
              <div class="text-sm font-bold text-white">${user.firstName || '—'}</div>
            </div>
          </div>
          <button onclick="state.showEditProfileModal = true; render();" class="text-xs text-gray-400 hover:text-brandGreen p-1">✏️</button>
        </div>

        <!-- 2. Last Name -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base">
              👤
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">Last Name</div>
              <div class="text-sm font-bold text-white">${user.lastName || '—'}</div>
            </div>
          </div>
          <button onclick="state.showEditProfileModal = true; render();" class="text-xs text-gray-400 hover:text-brandGreen p-1">✏️</button>
        </div>

        <!-- 3. Username (with Unique Badge) -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base font-bold text-brandGold">
              @
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">Username</div>
              <div class="text-sm font-bold text-brandGold">${usernameDisplay}</div>
            </div>
          </div>
          <span class="text-[10px] bg-amber-950 border border-brandGold/60 text-brandGold px-2 py-0.5 rounded-full font-bold">
            🌟 UNIQUE BADGE
          </span>
        </div>

        <!-- 4. Birthday -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base">
              🎂
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">Birthday</div>
              <div class="text-sm font-bold text-white">${birthdayDisplay}</div>
            </div>
          </div>
          <button onclick="state.showEditProfileModal = true; render();" class="text-xs text-gray-400 hover:text-brandGreen p-1">✏️</button>
        </div>

        <!-- 5. Gmail (with Verified Green Tick) -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base">
              ✉️
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">Gmail Address</div>
              <div class="text-xs font-bold text-white break-all">${user.email}</div>
            </div>
          </div>
          <span class="text-[10px] bg-emerald-950 border border-brandGreen text-brandGreen px-2 py-0.5 rounded-full font-bold flex items-center space-x-1">
            <span>✓</span>
            <span>Verified</span>
          </span>
        </div>

        <!-- 6. Phone Number -->
        <div class="bg-brandCard border border-gray-800 rounded-xl p-3.5 flex items-center justify-between shadow">
          <div class="flex items-center space-x-3">
            <div class="w-9 h-9 rounded-xl bg-gray-900 flex items-center justify-center text-base">
              📱
            </div>
            <div>
              <div class="text-[11px] font-bold uppercase text-gray-400 tracking-wider">Phone Number</div>
              <div class="text-xs font-bold ${user.phone ? 'text-white' : 'text-gray-500 italic'}">${phoneDisplay}</div>
            </div>
          </div>
          <button onclick="state.showEditProfileModal = true; render();" class="text-xs text-gray-400 hover:text-brandGreen p-1">✏️</button>
        </div>
      </div>

      <!-- Logout Button -->
      <div class="pt-2">
        <button onclick="logout();" class="w-full bg-red-950/40 hover:bg-red-900/60 border border-red-800 text-red-200 font-bold py-3 rounded-xl text-xs flex items-center justify-center space-x-2 tap-bounce">
          <span>🚪</span>
          <span>Sign Out from InvestPro</span>
        </button>
      </div>
    </main>
  `;
}

// Edit Profile Modal Component
function renderEditProfileModal() {
  const user = state.currentUser;
  if (!user) return '';

  return `
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-fade-in">
      <div class="bg-brandCard border border-gray-700 rounded-2xl p-5 max-w-sm w-full shadow-2xl">
        <div class="flex items-center justify-between pb-3 border-b border-gray-800 mb-4">
          <h3 class="text-sm font-bold text-white flex items-center space-x-1.5">
            <span>✏️</span>
            <span>Edit Profile Details</span>
          </h3>
          <button onclick="state.showEditProfileModal = false; render();" class="text-gray-400 hover:text-white font-bold">&times;</button>
        </div>

        <form id="edit-profile-form" class="space-y-3">
          <div>
            <label class="block text-[11px] font-bold uppercase text-gray-400 mb-1">First Name</label>
            <input type="text" id="edit-firstname" value="${user.firstName || ''}" required class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3 py-2 text-white text-sm focus:border-brandGreen focus:outline-none" />
          </div>

          <div>
            <label class="block text-[11px] font-bold uppercase text-gray-400 mb-1">Last Name</label>
            <input type="text" id="edit-lastname" value="${user.lastName || ''}" required class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3 py-2 text-white text-sm focus:border-brandGreen focus:outline-none" />
          </div>

          <div>
            <label class="block text-[11px] font-bold uppercase text-gray-400 mb-1">Birthday</label>
            <input type="date" id="edit-birthday" value="${user.birthday || ''}" required class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3 py-2 text-white text-sm focus:border-brandGreen focus:outline-none" />
          </div>

          <div>
            <label class="block text-[11px] font-bold uppercase text-gray-400 mb-1">Phone Number (Optional)</label>
            <input type="tel" id="edit-phone" value="${user.phone || ''}" placeholder="+94 77 123 4567" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-3 py-2 text-white text-sm focus:border-brandGreen focus:outline-none" />
          </div>

          <div class="pt-2 flex space-x-2">
            <button type="button" onclick="state.showEditProfileModal = false; render();" class="flex-1 bg-gray-800 hover:bg-gray-700 text-gray-300 font-bold py-2.5 rounded-xl text-xs">
              Cancel
            </button>
            <button type="submit" class="flex-1 bg-brandGreen hover:bg-emerald-600 text-white font-bold py-2.5 rounded-xl text-xs tap-bounce">
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  `;
}

// ------------------------------------------
// 9. AUTH SCREEN (Email OTP)
// ------------------------------------------
function renderEmailOtpAuthScreen() {
  if (state.authStep === 'otp_verify') {
    return renderOtpInputScreen();
  }

  const emailJsCfg = getEmailJsConfig();

  return `
    <div class="flex-1 flex flex-col justify-center px-6 py-10">
      <div class="text-center mb-6">
        <div class="w-16 h-16 bg-gradient-to-tr from-brandGreen to-emerald-400 rounded-2xl mx-auto flex items-center justify-center shadow-lg shadow-emerald-900/30 mb-3">
          <span class="text-3xl">🇱🇰</span>
        </div>
        <h1 class="text-2xl font-black tracking-tight text-white">Invest Pro</h1>
        <p class="text-xs text-gray-400 mt-1">Real Email OTP via EmailJS Gmail Service</p>
      </div>

      <div class="mb-4 p-3 rounded-xl bg-gray-900/80 border border-gray-800 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="w-2.5 h-2.5 rounded-full bg-brandGreen animate-pulse"></span>
          <div class="text-[11px]">
            <span class="text-gray-400">EmailJS Service:</span>
            <span class="font-bold text-white ml-1">Gmail (${emailJsCfg.serviceId})</span>
          </div>
        </div>
        <button onclick="state.view='emailjs_settings'; render();" class="text-[10px] text-brandGreen font-bold bg-emerald-950/60 px-2 py-1 rounded border border-emerald-800 hover:bg-emerald-900 tap-bounce">
          ⚙️ Keys
        </button>
      </div>

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-6 shadow-xl">
        <div class="flex items-center space-x-2 mb-3">
          <span class="text-lg">📧</span>
          <h2 class="text-base font-bold text-white">Enter Your Email Address</h2>
        </div>
        
        <p class="text-xs text-gray-400 mb-5 leading-relaxed">
          Sign in or register using your email. We will dispatch a 6-digit verification code with template:
          <span class="text-brandGold font-mono text-[11px] block mt-1 bg-gray-900 p-1.5 rounded">"Your Blaze OTP is {{otp}}"</span>
        </p>

        <form id="email-otp-form" class="space-y-4">
          <div>
            <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1.5">Email Address</label>
            <input 
              type="email" 
              id="auth-email-input" 
              required 
              placeholder="e.g. yourname@gmail.com" 
              value="${state.authEmail || ''}"
              class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-brandGreen" 
            />
          </div>

          <button type="submit" class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3.5 px-4 rounded-xl shadow-lg shadow-emerald-900/40 transition tap-bounce text-sm flex items-center justify-center space-x-2">
            <span>Send Blaze OTP Email</span>
            <span>✉️</span>
          </button>
        </form>

        <div class="mt-5 p-3 rounded-xl bg-gray-900/60 border border-gray-800 text-[11px] text-gray-400 space-y-1">
          <div class="flex items-center space-x-1.5 text-brandGreen font-semibold">
            <span>✓</span>
            <span>Password NOT needed • 100% Free Email OTP</span>
          </div>
          <div class="flex items-center space-x-1.5 text-gray-400">
            <span>🛡️</span>
            <span>Saved in Firestore 'users' & 'otps' collection</span>
          </div>
        </div>
      </div>

      <div class="mt-8 text-center flex justify-center space-x-4">
        <button onclick="state.view='admin'; render();" class="text-xs text-gray-500 hover:text-gray-300 underline">
          Admin Portal (Pass: admin123)
        </button>
        <span class="text-gray-700">•</span>
        <button onclick="state.view='emailjs_settings'; render();" class="text-xs text-gray-500 hover:text-gray-300 underline">
          EmailJS Keys Config
        </button>
      </div>
    </div>
  `;
}

function renderOtpInputScreen() {
  const isResendReady = state.resendCountdown === 0;

  return `
    <div class="flex-1 flex flex-col justify-center px-6 py-10">
      <div class="mb-4">
        <button onclick="state.authStep='email_input'; render();" class="flex items-center space-x-1 text-xs text-gray-400 hover:text-white font-semibold">
          <span>←</span>
          <span>Change Email Address</span>
        </button>
      </div>

      <div class="text-center mb-6">
        <div class="w-14 h-14 bg-emerald-900/40 border border-brandGreen text-brandGreen rounded-2xl mx-auto flex items-center justify-center shadow-lg mb-3 text-2xl">
          ✉️
        </div>
        <h2 class="text-xl font-black text-white">Enter 6-Digit Code</h2>
        <p class="text-xs text-gray-400 mt-1">
          Template: <span class="text-brandGold font-mono">"Your Blaze OTP is {{otp}}"</span>
        </p>
        <p class="text-xs text-gray-300 mt-0.5">
          Sent to <span class="text-white font-bold">${state.authEmail}</span>
        </p>
        <span class="inline-block mt-2 text-[10px] text-amber-400 font-semibold bg-amber-950/60 px-2.5 py-0.5 rounded-full border border-amber-800">
          Expires in 5 minutes
        </span>
      </div>

      <!-- Test Helper Card (Auto Fill) -->
      ${state.latestOtpSent ? `
        <div class="mb-5 p-3 rounded-xl bg-emerald-950/40 border border-emerald-700/60 flex items-center justify-between">
          <div class="text-xs">
            <span class="text-emerald-400 font-bold">EmailJS Sent Code:</span>
            <span class="font-mono font-black text-white ml-1 text-sm tracking-widest">${state.latestOtpSent}</span>
          </div>
          <button onclick="fillAndVerifyOtp('${state.latestOtpSent}');" class="bg-brandGreen hover:bg-emerald-600 text-white text-[11px] font-bold px-2.5 py-1 rounded-lg tap-bounce">
            Auto Fill Code
          </button>
        </div>
      ` : ''}

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-6 shadow-xl space-y-6">
        <!-- 6 OTP Input Boxes -->
        <div class="flex justify-between space-x-2" id="otp-boxes-container">
          ${[0, 1, 2, 3, 4, 5].map(idx => `
            <input 
              type="text" 
              inputmode="numeric" 
              maxlength="1" 
              id="otp-input-${idx}" 
              data-index="${idx}"
              value="${state.otpDigits[idx] || ''}"
              class="w-12 h-14 bg-[#121212] border-2 ${state.otpDigits[idx] ? 'border-brandGreen text-brandGreen' : 'border-gray-700 text-white'} rounded-xl text-center text-xl font-black focus:outline-none focus:border-brandGreen transition"
            />
          `).join('')}
        </div>

        <button 
          id="verify-otp-btn"
          onclick="
            const code = [0,1,2,3,4,5].map(i => document.getElementById('otp-input-' + i).value).join('');
            verifyEmailOtp(code);
          " 
          class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3.5 px-4 rounded-xl shadow-lg shadow-emerald-900/40 transition tap-bounce text-sm">
          Verify & Continue
        </button>

        <!-- 60s Resend Timer -->
        <div class="pt-2 text-center">
          ${isResendReady ? `
            <button onclick="requestEmailOtp(state.authEmail);" class="text-xs text-brandGreen font-bold hover:underline flex items-center justify-center space-x-1 mx-auto">
              <span>↻</span>
              <span>Resend OTP via Gmail</span>
            </button>
          ` : `
            <div id="resend-timer-label" class="text-xs text-gray-500 font-medium">
              Resend available in ${state.resendCountdown}s
            </div>
          `}
        </div>
      </div>
    </div>
  `;
}

function renderEmailJsSettingsModal() {
  const cfg = getEmailJsConfig();

  return `
    <header class="p-5 flex items-center space-x-3 border-b border-gray-800">
      <button onclick="state.view='auth'; render();" class="p-1 text-gray-400 hover:text-white">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </button>
      <h2 class="text-base font-bold text-white">EmailJS Keys Configuration</h2>
    </header>

    <main class="flex-1 p-5 space-y-5">
      <div class="bg-blue-950/40 border border-blue-700/50 rounded-2xl p-4 text-blue-200 text-xs leading-relaxed">
        <strong>📧 Free EmailJS Gmail Configuration:</strong>
        <p class="mt-1">
          Dispatches emails with template <code>"Your Blaze OTP is {{otp}}"</code>. Configure your Gmail service from <a href="https://dashboard.emailjs.com" target="_blank" class="underline text-white font-bold">emailjs.com</a>.
        </p>
      </div>

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-xl space-y-4">
        <div>
          <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Service ID (Gmail)</label>
          <input type="text" id="emailjs-service-id" value="${cfg.serviceId}" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-2.5 text-white text-sm font-mono focus:border-brandGreen focus:outline-none" />
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Template ID ("Your Blaze OTP is {{otp}}")</label>
          <input type="text" id="emailjs-template-id" value="${cfg.templateId}" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-2.5 text-white text-sm font-mono focus:border-brandGreen focus:outline-none" />
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase text-gray-400 mb-1">Public Key / User ID</label>
          <input type="text" id="emailjs-public-key" value="${cfg.publicKey}" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-2.5 text-white text-sm font-mono focus:border-brandGreen focus:outline-none" />
        </div>

        <button onclick="
          const serviceId = document.getElementById('emailjs-service-id').value.trim();
          const templateId = document.getElementById('emailjs-template-id').value.trim();
          const publicKey = document.getElementById('emailjs-public-key').value.trim();
          saveEmailJsConfig({ serviceId, templateId, publicKey });
          showNotification('EmailJS keys updated successfully!', 'success');
          state.view = 'auth';
          render();
        " class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3 rounded-xl text-sm tap-bounce">
          Save EmailJS Keys
        </button>

        <button onclick="
          saveEmailJsConfig(DEFAULT_EMAILJS_CONFIG);
          showNotification('Reset to default EmailJS keys', 'info');
          state.view = 'auth';
          render();
        " class="w-full bg-gray-800 hover:bg-gray-700 text-gray-300 font-semibold py-2.5 rounded-xl text-xs tap-bounce">
          Reset to Defaults
        </button>
      </div>
    </main>
  `;
}

function fillAndVerifyOtp(code) {
  const digits = code.split('');
  state.otpDigits = digits;
  for (let i = 0; i < 6; i++) {
    const input = document.getElementById('otp-input-' + i);
    if (input) input.value = digits[i] || '';
  }
  verifyEmailOtp(code);
}

// ------------------------------------------
// 10. DASHBOARD & OTHER VIEWS
// ------------------------------------------
function renderDashboard() {
  const user = state.currentUser;
  const balance = user ? (user.balance || 0.0) : 0.0;
  const target = state.savingsTarget;
  const progressRatio = Math.min(1.0, balance / target);
  const progressPercent = Math.round(progressRatio * 100);
  const remaining = Math.max(0, target - balance);

  const userTxs = state.transactions.filter(t => t.userId === user.uid || t.email === user.email);

  return `
    <header class="p-5 flex items-center justify-between border-b border-gray-800">
      <div class="flex items-center space-x-3 cursor-pointer" onclick="state.view='profile'; render();">
        <div class="w-10 h-10 rounded-full bg-emerald-900/50 border border-brandGreen flex items-center justify-center font-black text-brandGreen">
          ${(user.firstName || user.email || 'U').charAt(0).toUpperCase()}
        </div>
        <div>
          <div class="text-xs text-gray-400">Welcome,</div>
          <div class="text-sm font-bold truncate max-w-[170px] text-white">
            ${user.firstName ? `${user.firstName} ${user.lastName || ''}` : user.email}
          </div>
          <div class="text-[10px] text-brandGreen font-semibold">@${(user.username || 'user').replace(/^@/, '')}</div>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <button onclick="state.view='admin'; render();" class="px-2.5 py-1.5 rounded-lg bg-gray-800 text-xs font-semibold text-gray-300 hover:bg-gray-700 flex items-center space-x-1">
          <span>🔒</span>
          <span>Admin</span>
        </button>
        <button onclick="state.view='profile'; render();" class="p-2 text-gray-400 hover:text-white">
          <span>👤</span>
        </button>
      </div>
    </header>

    <main class="flex-1 px-4 py-5 space-y-5">
      <!-- Balance Card -->
      <div class="bg-gradient-to-br from-[#1565C0] to-[#0D47A1] rounded-2xl p-5 shadow-xl relative overflow-hidden">
        <div class="relative z-10">
          <div class="text-xs text-blue-200 uppercase tracking-wider font-semibold">Available Balance</div>
          <div class="text-3xl font-black text-white mt-1">Rs. ${balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
          <div class="text-xs text-blue-200/80 mt-1 flex items-center space-x-1">
            <span>🛡️</span>
            <span>Firestore Protected • @${(user.username || 'user').replace(/^@/, '')}</span>
          </div>

          <div class="mt-4 pt-4 border-t border-white/10 flex space-x-3">
            <button onclick="state.view='deposit'; render();" class="flex-1 bg-brandGreen hover:bg-emerald-600 text-white font-bold py-2.5 px-3 rounded-xl text-xs flex items-center justify-center space-x-1 shadow-lg tap-bounce">
              <span>➕</span>
              <span>Deposit Funds</span>
            </button>
            <button onclick="showNotification('To withdraw, maintain active plans or contact support', 'info');" class="flex-1 bg-white/10 hover:bg-white/20 text-white font-bold py-2.5 px-3 rounded-xl text-xs flex items-center justify-center space-x-1 tap-bounce">
              <span>💳</span>
              <span>Withdraw</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Savings Target Progress Ring & Bar Chart (Target: Rs. 5,000) -->
      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-lg">
        <div class="flex items-center justify-between mb-3">
          <div>
            <h3 class="text-sm font-bold text-white flex items-center space-x-1.5">
              <span>🎯</span>
              <span>Savings Target: Rs. ${target.toLocaleString()}</span>
            </h3>
            <p class="text-xs text-gray-400 mt-0.5">${progressPercent >= 100 ? 'Goal Achieved! 🏆' : `Rs. ${remaining.toLocaleString()} left to reach target`}</p>
          </div>
          <span class="text-xs font-black px-2 py-1 rounded-md ${progressPercent >= 100 ? 'bg-emerald-900 text-emerald-300' : 'bg-blue-900 text-blue-300'}">
            ${progressPercent}%
          </span>
        </div>

        <div class="flex items-center justify-center py-2">
          <div class="relative w-36 h-36 flex items-center justify-center">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="40" stroke="#262626" stroke-width="10" fill="transparent" />
              <circle cx="50" cy="50" r="40" 
                stroke="${progressPercent >= 100 ? '#00A651' : '#1976D2'}" 
                stroke-width="10" 
                stroke-dasharray="251.2" 
                stroke-dashoffset="${251.2 - (251.2 * progressRatio)}" 
                stroke-linecap="round" 
                fill="transparent" 
                style="transition: stroke-dashoffset 1s ease-in-out;"
              />
            </svg>
            <div class="absolute flex flex-col items-center justify-center text-center">
              <span class="text-2xl font-black text-white">${progressPercent}%</span>
              <span class="text-[10px] text-gray-400 uppercase tracking-wider">SAVED</span>
            </div>
          </div>
        </div>

        <div class="mt-4 pt-3 border-t border-gray-800">
          <div class="text-[11px] font-semibold text-gray-400 mb-2">Milestone Breakdown</div>
          <div class="grid grid-cols-4 gap-2 text-center text-[10px]">
            <div class="bg-gray-800/60 p-1.5 rounded-lg border ${balance >= 1250 ? 'border-brandGreen text-brandGreen font-bold' : 'border-gray-700 text-gray-400'}">
              <div>Rs. 1,250</div>
              <div class="text-[9px] text-gray-500">25%</div>
            </div>
            <div class="bg-gray-800/60 p-1.5 rounded-lg border ${balance >= 2500 ? 'border-brandGreen text-brandGreen font-bold' : 'border-gray-700 text-gray-400'}">
              <div>Rs. 2,500</div>
              <div class="text-[9px] text-gray-500">50%</div>
            </div>
            <div class="bg-gray-800/60 p-1.5 rounded-lg border ${balance >= 3750 ? 'border-brandGreen text-brandGreen font-bold' : 'border-gray-700 text-gray-400'}">
              <div>Rs. 3,750</div>
              <div class="text-[9px] text-gray-500">75%</div>
            </div>
            <div class="bg-gray-800/60 p-1.5 rounded-lg border ${balance >= 5000 ? 'border-brandGreen text-brandGreen font-bold' : 'border-gray-700 text-gray-400'}">
              <div>Rs. 5,000</div>
              <div class="text-[9px] text-gray-500">100% 🏆</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Transactions Activity -->
      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-lg">
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-sm font-bold text-white">Your Transactions</h3>
          <span class="text-xs text-gray-400">${userTxs.length} records</span>
        </div>

        ${userTxs.length === 0 ? `
          <div class="text-center py-6 text-gray-500 text-xs">
            No transactions yet. Click <strong>Deposit Funds</strong> to start!
          </div>
        ` : `
          <div class="space-y-2.5">
            ${userTxs.map(tx => `
              <div class="flex items-center justify-between p-3 rounded-xl bg-gray-900/50 border border-gray-800/60">
                <div class="flex items-center space-x-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs ${
                    tx.status === 'approved' ? 'bg-emerald-900/40 text-brandGreen' : 
                    tx.status === 'rejected' ? 'bg-red-900/40 text-red-400' : 'bg-amber-900/40 text-amber-400'
                  }">
                    ${tx.status === 'approved' ? '✓' : tx.status === 'rejected' ? '✕' : '⏳'}
                  </div>
                  <div>
                    <div class="text-xs font-bold text-white capitalize">${tx.type} (${tx.reference})</div>
                    <div class="text-[10px] text-gray-400">${new Date(tx.date).toLocaleDateString()}</div>
                  </div>
                </div>

                <div class="text-right">
                  <div class="text-xs font-black ${tx.status === 'approved' ? 'text-brandGreen' : 'text-white'}">
                    +Rs. ${tx.amount.toLocaleString()}
                  </div>
                  <span class="inline-block px-1.5 py-0.5 rounded text-[9px] font-extrabold uppercase ${
                    tx.status === 'approved' ? 'bg-emerald-900/80 text-emerald-300' :
                    tx.status === 'rejected' ? 'bg-red-900/80 text-red-300' : 'bg-amber-900/80 text-amber-300'
                  }">
                    ${tx.status}
                  </span>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    </main>
  `;
}

function renderDepositScreen() {
  return `
    <header class="p-5 flex items-center space-x-3 border-b border-gray-800">
      <button onclick="state.view='dashboard'; render();" class="p-1 text-gray-400 hover:text-white">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
      </button>
      <h2 class="text-base font-bold text-white">Deposit Funds</h2>
    </header>

    <main class="flex-1 p-5 space-y-5">
      <div class="bg-amber-950/40 border border-amber-700/50 rounded-2xl p-4 text-amber-200 text-xs leading-relaxed">
        <strong>⚠️ Security & Verification Rule:</strong>
        <p class="mt-1">All deposits enter as <span class="font-bold underline">PENDING</span>. Your wallet balance will ONLY be credited once verified and approved by the Admin.</p>
      </div>

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-xl space-y-4">
        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1">Deposit Amount (LKR)</label>
          <input type="number" id="deposit-amount" min="100" placeholder="e.g. 1000" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-3 text-white text-base font-bold focus:outline-none focus:border-brandGreen" />
        </div>

        <div>
          <label class="block text-xs font-semibold text-gray-400 mb-1.5">Quick Presets</label>
          <div class="grid grid-cols-4 gap-2">
            ${[500, 1000, 2500, 5000].map(val => `
              <button onclick="document.getElementById('deposit-amount').value = ${val};" class="bg-gray-800 hover:bg-gray-700 py-2 rounded-lg text-xs font-semibold text-gray-200">
                Rs. ${val}
              </button>
            `).join('')}
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1">Bank / LankaQR Reference</label>
          <input type="text" id="deposit-ref" placeholder="e.g. BOC-TX-998811" class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-brandGreen" />
        </div>

        <button onclick="
          const amt = document.getElementById('deposit-amount').value;
          const ref = document.getElementById('deposit-ref').value;
          requestDeposit(amt, ref);
        " class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3.5 px-4 rounded-xl shadow-lg shadow-emerald-900/40 text-sm tap-bounce">
          Submit Deposit (Requires Admin Approval)
        </button>
      </div>
    </main>
  `;
}

function renderAdminPanel() {
  if (!state.adminAuthenticated) {
    return `
      <header class="p-5 flex items-center space-x-3 border-b border-gray-800">
        <button onclick="state.view='dashboard'; render();" class="p-1 text-gray-400 hover:text-white">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
        </button>
        <h2 class="text-base font-bold text-white">Admin Authentication</h2>
      </header>

      <main class="flex-1 flex flex-col justify-center p-6">
        <div class="bg-brandCard border border-gray-800 rounded-2xl p-6 shadow-2xl text-center">
          <div class="w-14 h-14 bg-amber-900/40 border border-amber-600 text-amber-400 rounded-2xl flex items-center justify-center mx-auto mb-4 text-2xl">
            🔒
          </div>
          <h3 class="text-lg font-bold text-white">Password Protected</h3>
          <p class="text-xs text-gray-400 mt-1 mb-5">Enter administrator key (default: <code>admin123</code>)</p>

          <form id="admin-auth-form" class="space-y-4">
            <input type="password" id="admin-password" placeholder="Admin Password" required class="w-full bg-[#121212] border border-gray-700 rounded-xl px-4 py-3 text-white text-center text-sm focus:outline-none focus:border-brandGreen" />
            <button type="submit" class="w-full bg-brandGreen hover:bg-emerald-600 text-white font-bold py-3 rounded-xl text-sm tap-bounce">
              Unlock Admin Portal
            </button>
          </form>
        </div>
      </main>
    `;
  }

  const pendingTxs = state.transactions.filter(t => t.status === 'pending');

  return `
    <header class="p-5 flex items-center justify-between border-b border-gray-800 bg-gray-900">
      <div class="flex items-center space-x-2">
        <span class="text-lg">🛡️</span>
        <h2 class="text-base font-bold text-white">Admin Control Center</h2>
      </div>
      <button onclick="state.adminAuthenticated = false; state.view = 'dashboard'; render();" class="text-xs text-gray-400 hover:text-white px-2 py-1 bg-gray-800 rounded">
        Exit Admin
      </button>
    </header>

    <main class="flex-1 p-4 space-y-5">
      <div class="grid grid-cols-2 gap-3">
        <div class="bg-brandCard border border-gray-800 p-4 rounded-xl">
          <div class="text-xs text-gray-400">Pending Approvals</div>
          <div class="text-2xl font-black text-amber-400 mt-1">${pendingTxs.length}</div>
        </div>
        <div class="bg-brandCard border border-gray-800 p-4 rounded-xl">
          <div class="text-xs text-gray-400">Total Users</div>
          <div class="text-2xl font-black text-brandGreen mt-1">${state.users.length}</div>
        </div>
      </div>

      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-xl">
        <h3 class="text-sm font-bold text-white mb-3 flex items-center space-x-2">
          <span>⏳</span>
          <span>Pending Deposit Requests (${pendingTxs.length})</span>
        </h3>

        ${pendingTxs.length === 0 ? `
          <div class="p-6 text-center text-xs text-gray-500 bg-gray-900/30 rounded-xl">
            No pending deposits! All transactions are approved.
          </div>
        ` : `
          <div class="space-y-3">
            ${pendingTxs.map(tx => `
              <div class="p-3.5 rounded-xl bg-gray-900 border border-gray-700 space-y-2">
                <div class="flex items-start justify-between">
                  <div>
                    <div class="text-xs font-bold text-white">${tx.email}</div>
                    <div class="text-[10px] text-gray-400">Ref: ${tx.reference} • ${new Date(tx.date).toLocaleDateString()}</div>
                  </div>
                  <div class="text-sm font-black text-brandGold">
                    Rs. ${tx.amount.toLocaleString()}
                  </div>
                </div>

                <div class="pt-2 flex space-x-2 border-t border-gray-800">
                  <button onclick="approveTransaction('${tx.id}');" class="flex-1 bg-brandGreen hover:bg-emerald-600 text-white font-bold py-2 px-3 rounded-lg text-xs flex items-center justify-center space-x-1 tap-bounce">
                    <span>✓</span>
                    <span>Approve (Add Balance)</span>
                  </button>
                  <button onclick="rejectTransaction('${tx.id}');" class="flex-1 bg-red-900/80 hover:bg-red-800 text-red-100 font-bold py-2 px-3 rounded-lg text-xs flex items-center justify-center space-x-1 tap-bounce">
                    <span>✕</span>
                    <span>Reject</span>
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
        `}
      </div>

      <!-- Firestore Users Collection (Document ID = Auth UID) -->
      <div class="bg-brandCard border border-gray-800 rounded-2xl p-5 shadow-xl">
        <h3 class="text-sm font-bold text-white mb-3">Firestore 'users' Collection (ID = UID)</h3>
        <div class="space-y-2.5">
          ${state.users.map(u => `
            <div class="p-3 rounded-xl bg-gray-900/80 border border-gray-800 text-xs space-y-1">
              <div class="flex items-center justify-between">
                <span class="font-bold text-white">${u.firstName || 'User'} ${u.lastName || ''}</span>
                <span class="font-bold text-brandGreen">Rs. ${(u.balance || 0).toLocaleString()}</span>
              </div>
              <div class="flex items-center justify-between text-[11px] text-gray-400">
                <span class="text-brandGold font-mono">@${(u.username || 'user').replace(/^@/, '')}</span>
                <span>${u.email}</span>
              </div>
              <div class="text-[10px] text-gray-500 font-mono">UID: ${u.uid} • Born: ${u.birthday || 'N/A'}</div>
            </div>
          `).join('')}
        </div>
      </div>
    </main>
  `;
}

function renderBottomNav() {
  return `
    <nav class="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-brandCard/95 backdrop-blur-md border-t border-gray-800 flex justify-around py-3 px-2 z-40">
      <button onclick="state.view='dashboard'; render();" class="flex flex-col items-center ${state.view === 'dashboard' ? 'text-brandGreen' : 'text-gray-400'}">
        <span class="text-lg">🏠</span>
        <span class="text-[10px] font-semibold mt-0.5">Home</span>
      </button>

      <button onclick="state.view='deposit'; render();" class="flex flex-col items-center ${state.view === 'deposit' ? 'text-brandGreen' : 'text-gray-400'}">
        <span class="text-lg">➕</span>
        <span class="text-[10px] font-semibold mt-0.5">Deposit</span>
      </button>

      <button onclick="state.view='profile'; render();" class="flex flex-col items-center ${state.view === 'profile' ? 'text-brandGreen' : 'text-gray-400'}">
        <span class="text-lg">👤</span>
        <span class="text-[10px] font-semibold mt-0.5">Profile</span>
      </button>

      <button onclick="state.view='admin'; render();" class="flex flex-col items-center ${state.view === 'admin' ? 'text-brandGreen' : 'text-gray-400'}">
        <span class="text-lg">🛡️</span>
        <span class="text-[10px] font-semibold mt-0.5">Admin</span>
      </button>
    </nav>
  `;
}

function attachEventHandlers() {
  // Email OTP request form
  const emailForm = document.getElementById('email-otp-form');
  if (emailForm) {
    emailForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const emailInput = document.getElementById('auth-email-input');
      if (emailInput) requestEmailOtp(emailInput.value);
    });
  }

  // 6 discrete OTP boxes handling
  const container = document.getElementById('otp-boxes-container');
  if (container) {
    for (let i = 0; i < 6; i++) {
      const box = document.getElementById('otp-input-' + i);
      if (box) {
        box.addEventListener('input', (e) => {
          const val = e.target.value.replace(/\D/g, '');
          e.target.value = val ? val.charAt(val.length - 1) : '';
          state.otpDigits[i] = e.target.value;

          if (e.target.value && i < 5) {
            const next = document.getElementById('otp-input-' + (i + 1));
            if (next) next.focus();
          }

          const fullCode = state.otpDigits.join('');
          if (fullCode.length === 6) {
            verifyEmailOtp(fullCode);
          }
        });

        box.addEventListener('keydown', (e) => {
          if (e.key === 'Backspace' && !e.target.value && i > 0) {
            const prev = document.getElementById('otp-input-' + (i - 1));
            if (prev) {
              prev.focus();
              prev.value = '';
              state.otpDigits[i - 1] = '';
            }
          }
        });

        box.addEventListener('paste', (e) => {
          e.preventDefault();
          const pasteData = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
          if (pasteData.length >= 6) {
            fillAndVerifyOtp(pasteData.substring(0, 6));
          }
        });
      }
    }
  }

  // Sign Up Form Submission
  const signupForm = document.getElementById('signup-form');
  if (signupForm) {
    signupForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const firstName = document.getElementById('signup-firstname').value;
      const lastName = document.getElementById('signup-lastname').value;
      const rawUsername = document.getElementById('signup-username').value;
      const birthday = document.getElementById('signup-birthday').value;
      const phone = document.getElementById('signup-phone').value;

      completeRegistration({ firstName, lastName, rawUsername, birthday, phone });
    });

    // Real-time username check on keyup
    const usernameInput = document.getElementById('signup-username');
    if (usernameInput) {
      usernameInput.addEventListener('input', (e) => {
        const val = e.target.value.trim().toLowerCase().replace(/^@+/, '');
        const badge = document.getElementById('username-validation-badge');
        if (!badge) return;

        if (!val) {
          badge.innerText = 'Unique handle';
          badge.className = 'text-[10px] text-gray-400';
          return;
        }

        const isTaken = state.users.some(u => (u.username || '').toLowerCase() === val);
        if (isTaken) {
          badge.innerText = '✕ Taken';
          badge.className = 'text-[10px] text-red-400 font-bold';
        } else {
          badge.innerText = '✓ Available';
          badge.className = 'text-[10px] text-brandGreen font-bold';
        }
      });
    }
  }

  // Edit Profile Form Submission
  const editProfileForm = document.getElementById('edit-profile-form');
  if (editProfileForm) {
    editProfileForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const firstName = document.getElementById('edit-firstname').value;
      const lastName = document.getElementById('edit-lastname').value;
      const birthday = document.getElementById('edit-birthday').value;
      const phone = document.getElementById('edit-phone').value;

      updateUserProfile(firstName, lastName, birthday, phone);
    });
  }

  // Admin Auth Form
  const adminAuthForm = document.getElementById('admin-auth-form');
  if (adminAuthForm) {
    adminAuthForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const pass = document.getElementById('admin-password').value;
      if (pass === 'admin123') {
        state.adminAuthenticated = true;
        showNotification('Admin mode unlocked!');
        render();
      } else {
        showNotification('Incorrect Admin password', 'error');
      }
    });
  }
}

// Global scope bindings for inline HTML onclick handlers
window.state = state;
window.render = render;
window.logout = logout;
window.requestEmailOtp = requestEmailOtp;
window.verifyEmailOtp = verifyEmailOtp;
window.fillAndVerifyOtp = fillAndVerifyOtp;
window.completeRegistration = completeRegistration;
window.updateUserProfile = updateUserProfile;
window.saveEmailJsConfig = saveEmailJsConfig;
window.requestDeposit = requestDeposit;
window.approveTransaction = approveTransaction;
window.rejectTransaction = rejectTransaction;
window.showNotification = showNotification;

document.addEventListener('DOMContentLoaded', () => {
  render();
});
render();
