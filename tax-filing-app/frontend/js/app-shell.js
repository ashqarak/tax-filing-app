/**
 * TaxEase India — Shared App Shell
 * Sidebar, auth guard, and navigation
 */

// Auth guard — call at top of every app page
function requireAuth() {
  if (!Auth.requireLogin()) return false;
  return true;
}

// Inject the sidebar HTML into #app-sidebar element
function renderSidebar(activePage) {
  const user = Auth.getUser();
  const name  = user?.name  || 'User';
  const email = user?.email || '';
  const initials = name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

  const navItems = [
    { id: 'dashboard',  href: 'dashboard.html',  icon: '🏠', label: 'Dashboard' },
    { id: 'profile',    href: 'profile.html',     icon: '👤', label: 'My Profile' },
    { id: 'income',     href: 'income.html',      icon: '💰', label: 'Income Details' },
    { id: 'deductions', href: 'deductions.html',  icon: '🧾', label: 'Deductions' },
    { id: 'tax-result', href: 'tax-result.html',  icon: '🧮', label: 'Tax Calculator' },
    { id: 'history',    href: 'history.html',     icon: '📋', label: 'Filing History' },
  ];

  const navHTML = navItems.map(item => `
    <li>
      <a href="${item.href}" class="${activePage === item.id ? 'active' : ''}">
        <span class="nav-icon">${item.icon}</span>
        ${item.label}
      </a>
    </li>
  `).join('');

  const sidebar = document.getElementById('app-sidebar');
  if (!sidebar) return;

  sidebar.innerHTML = `
    <div class="sidebar-logo" onclick="window.location.href='index.html'">
      <span class="brand-icon">🇮🇳</span>
      <span class="brand-name">TaxEase<span class="brand-accent">India</span></span>
    </div>

    <p class="sidebar-section-label">Main Menu</p>
    <ul class="sidebar-nav">${navHTML}</ul>

    <div class="sidebar-bottom">
      <div class="sidebar-user">
        <div class="sidebar-avatar">${initials}</div>
        <div class="sidebar-user-info">
          <div class="sidebar-user-name">${name}</div>
          <div class="sidebar-user-email">${email}</div>
        </div>
      </div>
      <ul class="sidebar-nav">
        <li>
          <button onclick="Auth.logout()" style="color:var(--accent-red);">
            <span class="nav-icon">🚪</span> Sign Out
          </button>
        </li>
      </ul>
    </div>
  `;
}

// Load user info and populate sidebar
async function initAppShell(activePage) {
  if (!requireAuth()) return false;

  // Try to refresh user info from server
  try {
    const user = await ClientAPI.getMe();
    Auth.setUser(user);
  } catch (_) { /* use cached */ }

  renderSidebar(activePage);
  return true;
}
