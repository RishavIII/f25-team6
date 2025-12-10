(function(){
  const uid = requireUserId();
  if (!uid) return;
  let els = { list: null, filters: null, unreadCount: null, btnAll: null, btnUnread: null, btnMarkAll: null, detail: null, detailBody: null, btnBack: null };
  let items = [];
  let filter = 'all';

  function fmtWhen(iso){
    if(!iso) return '';
    const d = new Date(iso);
    const now = new Date();
    const sameDay = d.toDateString() === now.toDateString();
    if (sameDay) return d.toLocaleTimeString([], {hour:'numeric', minute:'2-digit'});
    return d.toLocaleDateString();
  }

  async function load(){
    items = await apiJson(`/tutors/${encodeURIComponent(uid)}/notifications?limit=200`);
    render();
    refreshBadges();
  }

  function render(){
    if (!els.list) return;
    const show = items.filter(n => filter==='all' || !n.read);
    els.list.innerHTML = '';
    const unreadTotal = items.filter(n => !n.read).length;
    if (els.unreadCount) els.unreadCount.textContent = `${unreadTotal} unread`;
    // Sync filter chips visual state
    if (els.btnAll && els.btnUnread){
      const allSel = filter === 'all';
      els.btnAll.classList.toggle('chip-active', allSel);
      els.btnUnread.classList.toggle('chip-active', !allSel);
      els.btnAll.setAttribute('aria-pressed', String(allSel));
      els.btnUnread.setAttribute('aria-pressed', String(!allSel));
    }

    if (show.length === 0){
      const li = document.createElement('li'); li.className='muted'; li.style.padding='12px'; li.textContent = 'No notifications'; els.list.appendChild(li);
      return;
    }
    for (const n of show){
      const li = document.createElement('li'); li.className = 'notif-item' + (!n.read ? ' is-unread' : '');
      const dot = document.createElement('div'); dot.className = 'notif-dot'; dot.setAttribute('aria-hidden','true');
      const body = document.createElement('div'); body.className='notif-body';
      const strong = document.createElement('strong'); strong.textContent = n.title || 'Notification';
      const p = document.createElement('p'); p.textContent = n.body || '';
      const time = document.createElement('span'); time.className = 'time small muted'; time.textContent = fmtWhen(n.createdAt || n.when);
      body.appendChild(strong); if (p.textContent) body.appendChild(p); body.appendChild(time);
      li.appendChild(dot); li.appendChild(body);
      li.addEventListener('click', ()=> openDetail(n));
      els.list.appendChild(li);
    }
  }

  async function markAllRead(){
    await fetch(`/api/tutors/${encodeURIComponent(uid)}/notifications/read-all`, {method:'PUT'});
    await load();
  }

  async function markItemRead(n){
    if (n.read) return;
    await fetch(`/api/tutors/${encodeURIComponent(uid)}/notifications/read-item`, {method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify({type:n.type, itemId:n.id})});
    n.read = true;
    refreshBadges();
  }

  function refreshBadges(){
    if (window.updateTutorUnreadBadge) window.updateTutorUnreadBadge();
    if (window.updateTutorNotificationsBadge) window.updateTutorNotificationsBadge();
  }

  function openDetail(n){
    if (!els.detail || !els.detailBody) return;
    els.list.style.display = 'none';
    els.detail.style.display = '';
    els.detailBody.innerHTML = '';
    const h = document.createElement('h3'); h.textContent = n.title || 'Notification';
    const p = document.createElement('p'); p.textContent = n.body || '';
    const t = document.createElement('div'); t.className='muted small'; t.textContent = fmtWhen(n.createdAt || n.when);
    els.detailBody.appendChild(h); if (p.textContent) els.detailBody.appendChild(p); els.detailBody.appendChild(t);
    markItemRead(n).then(()=> load());
  }

  function backToList(){
    els.detail.style.display = 'none';
    els.list.style.display = '';
  }

  function start(){
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

    if (els.btnAll) els.btnAll.addEventListener('click', ()=>{ filter='all'; render(); });
    if (els.btnUnread) els.btnUnread.addEventListener('click', ()=>{ filter='unread'; render(); });
    if (els.btnMarkAll) els.btnMarkAll.addEventListener('click', markAllRead);
    if (els.btnBack) els.btnBack.addEventListener('click', backToList);

    load();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start); else start();
})();
