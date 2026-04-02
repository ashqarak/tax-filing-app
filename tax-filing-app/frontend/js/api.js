/**
 * TaxEase India — API Client
 * Handles all communication with the Spring Boot backend at localhost:8080
 */

const API_BASE = 'https://taxease-api.onrender.com/api';
const AY = '2025-26'; // Current Assessment Year

// ── Token helpers ──────────────────────────────────────────────
const Auth = {
  setToken(token) { localStorage.setItem('taxease_token', token); },
  getToken() { return localStorage.getItem('taxease_token'); },
  removeToken() { localStorage.removeItem('taxease_token'); localStorage.removeItem('taxease_user'); },
  setUser(user) { localStorage.setItem('taxease_user', JSON.stringify(user)); },
  getUser() { try { return JSON.parse(localStorage.getItem('taxease_user')); } catch { return null; } },
  isLoggedIn() { return !!Auth.getToken(); },

  logout() {
    Auth.removeToken();
    window.location.href = 'login.html';
  },

  requireLogin() {
    if (!Auth.isLoggedIn()) {
      window.location.href = 'login.html';
      return false;
    }
    return true;
  }
};

// ── Core fetch wrapper ──────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const token = Auth.getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...(options.headers || {})
  };

  const resp = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });

  if (resp.status === 401) {
    Auth.logout();
    throw new Error('Session expired. Please log in again.');
  }

  if (!resp.ok) {
    let errMsg = `Error ${resp.status}`;
    try {
      const body = await resp.json();
      errMsg = body.message || body.error || JSON.stringify(body);
    } catch (_) {
      errMsg = await resp.text().catch(() => errMsg);
    }
    throw new Error(errMsg);
  }

  // Handle empty responses
  const contentType = resp.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return resp.json();
  }
  return null;
}

// ── Auth API ────────────────────────────────────────────────────
const AuthAPI = {
  async register({ name, email, password }) {
    const data = await apiFetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password })
    });
    Auth.setToken(data.token);
    return data;
  },

  async login({ email, password }) {
    const data = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
    Auth.setToken(data.token);
    return data;
  },

  async ping() {
    return apiFetch('/auth/ping');
  }
};

// ── Client Profile API ──────────────────────────────────────────
const ClientAPI = {
  async getMe() {
    return apiFetch('/client/me');
  },
  async getProfile() {
    return apiFetch('/client/profile');
  },
  async updateProfile(profile) {
    return apiFetch('/client/profile', {
      method: 'PUT',
      body: JSON.stringify(profile)
    });
  }
};

// ── Income API ──────────────────────────────────────────────────
const IncomeAPI = {
  async get(ay = AY) {
    return apiFetch(`/income/${ay}`);
  },
  async save(income, ay = AY) {
    return apiFetch(`/income/${ay}`, {
      method: 'POST',
      body: JSON.stringify(income)
    });
  }
};

// ── Deduction API ───────────────────────────────────────────────
const DeductionAPI = {
  async get(ay = AY) {
    return apiFetch(`/deductions/${ay}`);
  },
  async save(deductions, ay = AY) {
    return apiFetch(`/deductions/${ay}`, {
      method: 'POST',
      body: JSON.stringify(deductions)
    });
  }
};

// ── Tax API ─────────────────────────────────────────────────────
const TaxAPI = {
  async calculate(ay = AY) {
    return apiFetch(`/tax/calculate/${ay}`);
  },
  async submit(ay = AY) {
    return apiFetch(`/tax/submit/${ay}`, { method: 'POST' });
  },
  async getReturn(ay = AY) {
    return apiFetch(`/tax/return/${ay}`);
  },
  async getAllReturns() {
    return apiFetch('/tax/returns');
  }
};

// ── Helpers ─────────────────────────────────────────────────────
function formatINR(amount) {
  if (amount === null || amount === undefined) return '₹0';
  const num = Math.abs(amount);
  let formatted;
  if (num >= 10000000) {
    formatted = (num / 10000000).toFixed(2) + ' Cr';
  } else if (num >= 100000) {
    formatted = (num / 100000).toFixed(2) + ' L';
  } else {
    formatted = num.toLocaleString('en-IN');
  }
  return (amount < 0 ? '−₹' : '₹') + formatted;
}

function formatDate(dateStr) {
  if (!dateStr) return '—';
  try {
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  } catch { return dateStr; }
}

// Toast notifications
function showToast(message, type = 'info', duration = 4000) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(30px)';
    toast.style.transition = '0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// Set button to loading state
function setLoading(btn, loading, text = null) {
  if (loading) {
    btn.dataset.originalText = btn.innerHTML;
    btn.innerHTML = `<span class="spinner"></span> ${text || 'Loading…'}`;
    btn.disabled = true;
  } else {
    btn.innerHTML = btn.dataset.originalText || btn.innerHTML;
    btn.disabled = false;
  }
}
