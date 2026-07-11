const API = '/api';

function getToken() { return localStorage.getItem('token'); }
function getUser() { return JSON.parse(localStorage.getItem('user') || '{}'); }
function isLoggedIn() { return !!getToken(); }

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getToken()}`
  };
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/login.html';
}

function requireAuth() {
  if (!isLoggedIn()) window.location.href = '/login.html';
}

async function apiPost(endpoint, data, auth = false) {
  const headers = auth ? authHeaders() : { 'Content-Type': 'application/json' };
  const res = await fetch(API + endpoint, {
    method: 'POST',
    headers,
    body: JSON.stringify(data)
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || 'Request failed');
  }
  return res.json();
}

async function apiGet(endpoint) {
  const res = await fetch(API + endpoint, { headers: authHeaders() });
  if (res.status === 401) {
    logout();
    return;
  }
  if (!res.ok) throw new Error('Request failed');
  return res.json();
}

async function apiPatch(endpoint, data) {
  const res = await fetch(API + endpoint, {
    method: 'PATCH',
    headers: authHeaders(),
    body: JSON.stringify(data)
  });
  if (!res.ok) {
    let message = 'Request failed';
    try {
      const body = await res.json();
      if (body && body.error) message = body.error;
    } catch (_) { /* no JSON body */ }
    throw new Error(message);
  }
  return res.json();
}

async function apiDelete(endpoint) {
  const res = await fetch(API + endpoint, { method: 'DELETE', headers: authHeaders() });
  if (!res.ok) {
    let message = 'Request failed';
    try {
      const body = await res.json();
      if (body && body.error) message = body.error;
    } catch (_) { /* no JSON body, e.g. plain 404/405 */ }
    throw new Error(message);
  }
}

function showAlert(id, message, type = 'error') {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = message;
  el.className = `alert alert-${type}`;
  el.style.display = 'block';
  setTimeout(() => el.style.display = 'none', 4000);
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[char]);
}

function statusBadge(status) {
  if (!status) return '';
  return `<span class="badge badge-${status.toLowerCase()}">${escapeHtml(status.replace('_', ' '))}</span>`;
}

function priorityBadge(priority) {
  if (!priority) return '';
  return `<span class="badge badge-${priority.toLowerCase()}">${escapeHtml(priority)}</span>`;
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('en-ZA', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  });
}

function renderNavbar() {
  const user = getUser();
  const isAdmin = user.role === 'ADMIN' || user.role === 'TECHNICIAN';
  const nameEl = document.getElementById('navbar-username');
  if (nameEl) nameEl.textContent = user.username || '';
  const adminLink = document.getElementById('admin-link');
  if (isAdmin && adminLink) adminLink.style.display = 'inline';
}