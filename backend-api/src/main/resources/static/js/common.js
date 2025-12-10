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
