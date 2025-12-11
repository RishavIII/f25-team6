(function () {
  const uid = requireUserId();
  if (!uid) return;
  let els = { list: null, filters: null, unreadCount: null, btnAll: null, btnUnread: null, btnMarkAll: null, detail: null, detailBody: null, btnBack: null };
  let items = [];
  let filter = 'all';

  function fmtWhen(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    const now = new Date();
    const sameDay = d.toDateString() === now.toDateString();
    if (sameDay) return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
    return d.toLocaleDateString();
  }

  async function load() {
    items = await apiJson(`/tutors/${encodeURIComponent(uid)}/notifications?limit=200`);
    render();
    refreshBadges();
  }

  function render() {
    if (!els.list) return;
    const show = items.filter(n => filter === 'all' || !n.read);
    els.list.innerHTML = '';
    const unreadTotal = items.filter(n => !n.read).length;
    if (els.unreadCount) els.unreadCount.textContent = `${unreadTotal} unread`;
    // Sync filter chips visual state
    if (els.btnAll && els.btnUnread) {
      const allSel = filter === 'all';
      els.btnAll.classList.toggle('chip-active', allSel);
      els.btnUnread.classList.toggle('chip-active', !allSel);
      els.btnAll.setAttribute('aria-pressed', String(allSel));
      els.btnUnread.setAttribute('aria-pressed', String(!allSel));
    }

    if (show.length === 0) {
      const li = document.createElement('li'); li.className = 'muted'; li.style.padding = '12px'; li.textContent = 'No notifications'; els.list.appendChild(li);
      return;
    }
    for (const n of show) {
      const li = document.createElement('li'); li.className = 'notif-item' + (!n.read ? ' is-unread' : '');
      const dot = document.createElement('div'); dot.className = 'notif-dot'; dot.setAttribute('aria-hidden', 'true');
      const body = document.createElement('div'); body.className = 'notif-body';
      const strong = document.createElement('strong'); strong.textContent = n.title || 'Notification';
      const p = document.createElement('p');
      // If booking notification, show instrument and requested time
      if (n.type === 'booking') {
        p.textContent = n.body || '';
        const whenEl = document.createElement('div'); whenEl.className = 'muted small'; whenEl.textContent = fmtWhen(n.when || n.createdAt);
        body.appendChild(strong); if (p.textContent) body.appendChild(p); body.appendChild(whenEl);
      } else {
        p.textContent = n.body || '';
        const time = document.createElement('span'); time.className = 'time small muted'; time.textContent = fmtWhen(n.createdAt || n.when);
        body.appendChild(strong); if (p.textContent) body.appendChild(p); body.appendChild(time);
      }
      li.appendChild(dot); li.appendChild(body);
      li.addEventListener('click', () => openDetail(n));
      els.list.appendChild(li);
    }
  }

  async function markAllRead() {
    await fetch(`/api/tutors/${encodeURIComponent(uid)}/notifications/read-all`, { method: 'PUT' });
    await load();
  }

  async function markItemRead(n) {
    if (n.read) return;
    await fetch(`/api/tutors/${encodeURIComponent(uid)}/notifications/read-item`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ type: n.type, itemId: n.id }) });
    n.read = true;
    refreshBadges();
  }

  function refreshBadges() {
    if (window.updateTutorUnreadBadge) window.updateTutorUnreadBadge();
    if (window.updateTutorNotificationsBadge) window.updateTutorNotificationsBadge();
  }

  function openDetail(n) {
    if (!els.detail || !els.detailBody) return;
    els.list.style.display = 'none';
    els.detail.style.display = '';
    els.detailBody.innerHTML = '';
    const h = document.createElement('h3'); h.textContent = n.title || 'Notification';
    const p = document.createElement('p'); p.textContent = n.body || '';
    const t = document.createElement('div'); t.className = 'muted small'; t.textContent = fmtWhen(n.createdAt || n.when);
    els.detailBody.appendChild(h); if (p.textContent) els.detailBody.appendChild(p); els.detailBody.appendChild(t);

    // Contextual actions
    const actions = document.createElement('div');
    actions.className = 'actions-row';
    actions.style.marginTop = '16px';

    if (n.type === 'booking') {
      const btnAccept = document.createElement('a');
      btnAccept.className = 'btn small';
      btnAccept.style.color = 'green';
      btnAccept.style.border = '1px solid green';
      btnAccept.textContent = 'Accept Request';
      btnAccept.href = '#';
      btnAccept.onclick = async (e) => {
        e.preventDefault();
        if (!confirm('Accept this request?')) return;
        try {
          await apiJson(`/booking-requests/${n.id}/accept`, { method: 'POST' });
          alert('Request accepted!');
          load(); backToList();
        } catch (err) { alert('Error: ' + err.message); }
      };

      const btnDecline = document.createElement('a');
      btnDecline.className = 'btn small';
      btnDecline.style.color = 'red';
      btnDecline.style.border = '1px solid red';
      btnDecline.textContent = 'Decline Request';
      btnDecline.href = '#';
      btnDecline.onclick = async (e) => {
        e.preventDefault();
        if (!confirm('Decline this request?')) return;
        try {
          await apiJson(`/booking-requests/${n.id}/decline`, { method: 'POST' });
          alert('Request declined.');
          load(); backToList();
        } catch (err) { alert('Error: ' + err.message); }
      };
      actions.appendChild(btnAccept);
      actions.appendChild(btnDecline);
      // Details button - show booking details in a modal-like overlay
      const btnDetails = document.createElement('a');
      btnDetails.className = 'btn small';
      btnDetails.textContent = 'Details';
      btnDetails.href = '#';
      btnDetails.onclick = (e) => {
        e.preventDefault();
        // Show inline detail modal if available
        if (window.showBookingModal) window.showBookingModal(n);
        else {
          // Fallback: expand details inline
          const extra = document.createElement('div');
          extra.className = 'muted small';
          extra.style.marginTop = '8px';
          extra.textContent = `Instrument: ${n.body || ''} · When: ${fmtWhen(n.when)}`;
          els.detailBody.appendChild(extra);
        }
      };
      actions.appendChild(btnDetails);

      // Message button - go to messaging page with studentId if available
      const btnMessage = document.createElement('a');
      btnMessage.className = 'btn small';
      btnMessage.textContent = 'Message';
      btnMessage.href = '#';
      btnMessage.onclick = (e) => {
        e.preventDefault();
        if (n.conversationId) {
          window.location.href = `tutor-message.html?conversationId=${n.conversationId}`;
        } else if (n.studentId) {
          window.location.href = `tutor-message.html?studentId=${n.studentId}`;
        } else {
          window.location.href = 'tutor-message.html';
        }
      };
      actions.appendChild(btnMessage);
    } else if (n.type === 'message') {
      const btnReply = document.createElement('a');
      btnReply.className = 'btn primary small';
      btnReply.textContent = 'View Conversation';
      btnReply.href = '/Tutor/tutor-message.html'; // Ideally link to specific convo if we had convo ID
      actions.appendChild(btnReply);
    }

    if (actions.children.length > 0) els.detailBody.appendChild(actions);

    markItemRead(n).then(() => load());
  }

  function backToList() {
    els.detail.style.display = 'none';
    els.list.style.display = '';
  }

  function start() {
    els.list = document.getElementById('notifList');
    els.filters = document.getElementById('notifFilters');
    els.unreadCount = document.getElementById('unreadCount');
    els.btnAll = document.getElementById('filterAll');
    els.btnUnread = document.getElementById('filterUnread');
    els.btnMarkAll = document.getElementById('btnMarkAllRead');
    els.detail = document.getElementById('notifDetail');
    els.detailBody = document.getElementById('notifDetailBody');
    els.btnBack = document.getElementById('btnBackList');
    if (!els.list) return;

    if (els.btnAll) els.btnAll.addEventListener('click', () => { filter = 'all'; render(); });
    if (els.btnUnread) els.btnUnread.addEventListener('click', () => { filter = 'unread'; render(); });
    if (els.btnMarkAll) els.btnMarkAll.addEventListener('click', markAllRead);
    if (els.btnBack) els.btnBack.addEventListener('click', backToList);

    // Booking modal close handler
    const bookingCloseBtn = document.getElementById('bookingModalClose');
    if (bookingCloseBtn) bookingCloseBtn.addEventListener('click', () => closeBookingModal());

    load();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start); else start();

  // Booking modal helpers
  window.showBookingModal = function (n) {
    const modal = document.getElementById('bookingModal');
    const content = document.getElementById('bookingModalContent');
    if (!modal || !content) return;
    content.innerHTML = '';
    const h = document.createElement('div');
    h.className = 'modal-row';
    const lbl = document.createElement('span'); lbl.className = 'modal-label'; lbl.textContent = 'Student';
    const val = document.createElement('span'); val.className = 'modal-value'; val.textContent = n.title ? n.title.replace('Lesson Request','') : (n.body || '');
    h.appendChild(lbl); h.appendChild(val); content.appendChild(h);
    const instrRow = document.createElement('div'); instrRow.className = 'modal-row';
    const il = document.createElement('span'); il.className = 'modal-label'; il.textContent = 'When';
    const iv = document.createElement('span'); iv.className = 'modal-value'; iv.textContent = fmtWhen(n.when);
    instrRow.appendChild(il); instrRow.appendChild(iv); content.appendChild(instrRow);
    if (n.body) {
      const notes = document.createElement('div'); notes.style.marginTop = '8px'; notes.textContent = n.body; content.appendChild(notes);
    }
    modal.style.display = 'flex'; modal.classList.add('active');
  };

  function closeBookingModal() {
    const modal = document.getElementById('bookingModal');
    if (modal) { modal.style.display = 'none'; modal.classList.remove('active'); }
  }
})();
