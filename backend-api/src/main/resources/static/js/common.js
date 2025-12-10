const API_BASE = "/api";

function requireUserId() {
  try {
    const uid = localStorage.getItem("userId");
    if (!uid) {
      window.location.href = "/Tutor/tutor_login.html";
      return null;
    }
    return uid;
  } catch (e) {
    window.location.href = "/Tutor/tutor_login.html";
    return null;
  }
}

async function apiJson(path, options = {}) {
  const res = await fetch(API_BASE + path, options);
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    const err = new Error(
      `Request failed: ${res.status} ${res.statusText} ${text}`
    );
    err.status = res.status;
    throw err;
  }
  return res.json();
}

function getTutorProfile(userId) {
  return apiJson(`/tutor-profiles/${encodeURIComponent(userId)}`);
}

function createTutorProfile(userId, payload) {
  return apiJson(`/tutor-profiles/${encodeURIComponent(userId)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

function getTutorInstruments() {
  return apiJson("/tutor-instruments");
}

function createTutorInstrument(req) {
  return apiJson("/tutor-instruments", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
}

function getInstruments() {
  return apiJson("/instruments");
}

function formatMoneyFromCents(cents) {
  if (cents == null) return "";
  const dollars = cents / 100;
  return "$" + dollars.toFixed(2);
}

// Global: update unread messages badge in navbar for tutors
window.updateTutorUnreadBadge = async function updateTutorUnreadBadge(){
  try{
    const role = localStorage.getItem('userRole');
    const uid = localStorage.getItem('userId');
    if (role !== 'TUTOR' || !uid) return;
    const anchor = document.querySelector('nav.nav-links a.nav-link[href*="tutor-message.html"]');
    if (!anchor) return;
    let badge = anchor.querySelector('.nav-badge');
    if (!badge){
      badge = document.createElement('span');
      badge.className = 'nav-badge';
      anchor.appendChild(badge);
    }
    const convos = await apiJson(`/tutors/${encodeURIComponent(uid)}/conversations`).catch(()=>[]);
    const total = Array.isArray(convos) ? convos.reduce((s,c)=> s + (c.unreadCount||0), 0) : 0;
    if (total > 0){ badge.textContent = total > 99 ? '99+' : String(total); badge.style.display = 'inline-flex'; }
    else { badge.textContent=''; badge.style.display='none'; }
  }catch(_){ /* ignore */ }
}

// Try to update badge on load
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', ()=>{ window.updateTutorUnreadBadge && window.updateTutorUnreadBadge(); });
} else {
  window.updateTutorUnreadBadge && window.updateTutorUnreadBadge();
}

// Global: update notifications unread badge in navbar for tutors
window.updateTutorNotificationsBadge = async function updateTutorNotificationsBadge(){
  try{
    const role = localStorage.getItem('userRole');
    const uid = localStorage.getItem('userId');
    if (role !== 'TUTOR' || !uid) return;
    const anchor = document.querySelector('nav.nav-links a.nav-link[href*="tutor-notifications.html"]');
    if (!anchor) return;
    let badge = anchor.querySelector('.nav-badge');
    if (!badge){ badge = document.createElement('span'); badge.className = 'nav-badge'; anchor.appendChild(badge); }
    const count = await apiJson(`/tutors/${encodeURIComponent(uid)}/notifications/unread-count`).catch(()=>0);
    const total = Number(count) || 0;
    if (total > 0){ badge.textContent = total > 99 ? '99+' : String(total); badge.style.display = 'inline-flex'; }
    else { badge.textContent=''; badge.style.display='none'; }
  }catch(_){ }
}

// Update notifications badge on load as well
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', ()=>{ window.updateTutorNotificationsBadge && window.updateTutorNotificationsBadge(); });
} else {
  window.updateTutorNotificationsBadge && window.updateTutorNotificationsBadge();
}
