(function(){
  const uid = requireUserId();
  if (!uid) return;

  let state = { convoId: null, otherName: null };
  const els = {
    list: null,
    chat: null,
    text: null,
    send: null,
    header: null,
    headerName: null,
    headerAvatar: null,
  };

  function initials(name){
    if (!name) return 'U';
    return name.split(' ').map(s=>s[0]).join('').slice(0,2).toUpperCase();
  }
  function fmtTime(iso){
    if(!iso) return '';
    const d = new Date(iso);
    return d.toLocaleTimeString([], {hour:'numeric', minute:'2-digit'});
  }

  async function loadConversations(){
    const convos = await apiJson(`/tutors/${encodeURIComponent(uid)}/conversations`);
    renderConversations(convos);
    // Do not auto-open; keep chat empty until user clicks
    if (window.updateTutorUnreadBadge) window.updateTutorUnreadBadge();
  }

  function renderConversations(convos){
    els.list.innerHTML = '';
    if (!convos || convos.length === 0){
      const li = document.createElement('li');
      li.className = 'muted';
      li.style.padding = '12px';
      li.textContent = 'No conversations yet';
      els.list.appendChild(li);
      return;
    }
    for (const c of convos){
      const li = document.createElement('li'); li.className='convo'; li.dataset.id=c.conversationId;
      const av = document.createElement('div'); av.className='avatar'; av.textContent = initials(c.otherName);
      const meta = document.createElement('div'); meta.className='convo-meta';
      const strong = document.createElement('strong'); strong.textContent = c.otherName || 'Student';
      const span = document.createElement('span'); span.className='muted';
      span.textContent = c.lastMessageAt ? `${fmtTime(c.lastMessageAt)} · ${c.lastMessage || ''}` : '';
      meta.appendChild(strong); meta.appendChild(span);
      const unread = document.createElement('div'); unread.className='convo-unread';
      const count = c.unreadCount || 0;
      unread.textContent = count > 0 ? String(count) : '';
      li.appendChild(av); li.appendChild(meta); li.appendChild(unread);
      li.addEventListener('click', ()=> selectConversation(c.conversationId, c.otherName));
      els.list.appendChild(li);
    }
  }

  async function selectConversation(id, otherName){
    state.convoId = id; state.otherName = otherName || null;
    // Show header/chat/composer now that a thread is active
    if (els.header) els.header.style.display = '';
    if (els.chat) els.chat.style.display = '';
    if (els.send && els.text) { document.getElementById('chatComposer').style.display = ''; }
    if (els.headerName) els.headerName.textContent = state.otherName || '';
    if (els.headerAvatar) els.headerAvatar.textContent = initials(state.otherName);
    // Mark as read to clear unread counter
    try { await fetch(`/api/tutors/${encodeURIComponent(uid)}/conversations/${encodeURIComponent(id)}/read`, {method:'PUT'}); } catch(_){ }
    const msgs = await apiJson(`/tutors/${encodeURIComponent(uid)}/conversations/${encodeURIComponent(id)}/messages`);
    renderMessages(msgs);
    // Refresh list to update unread counters and ordering
    await loadConversations();
    if (window.updateTutorUnreadBadge) window.updateTutorUnreadBadge();
  }

  function renderMessages(msgs){
    els.chat.innerHTML = '';
    for (const m of msgs){
      const mine = String(m.senderUserId) === String(uid);
      const wrap = document.createElement('div'); wrap.className = 'message ' + (mine ? 'sent' : 'received');
      const bubble = document.createElement('div'); bubble.className='bubble'; bubble.textContent = m.body || '';
      const time = document.createElement('div'); time.className='time muted small'; time.textContent = fmtTime(m.createdAt);
      wrap.appendChild(bubble); wrap.appendChild(time);
      els.chat.appendChild(wrap);
    }
    els.chat.scrollTop = els.chat.scrollHeight;
  }

  async function sendMessage(){
    const text = (els.text.value||'').trim();
    if(!text || !state.convoId) return;
    els.send.disabled = true;
    try {
      await apiJson(`/tutors/${encodeURIComponent(uid)}/conversations/${encodeURIComponent(state.convoId)}/messages`, {
        method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({body:text})
      });
      els.text.value='';
      await selectConversation(state.convoId);
      await loadConversations();
      if (window.updateTutorUnreadBadge) window.updateTutorUnreadBadge();
    } finally { els.send.disabled = false; }
  }

  function start(){
    els.list = document.getElementById('convoList');
    els.chat = document.getElementById('chatLog');
    els.text = document.getElementById('composerText');
    els.send = document.getElementById('composerSend');
    els.header = document.getElementById('chatHeader');
    els.headerName = document.getElementById('chatName');
    els.headerAvatar = document.getElementById('chatAvatar');
    if (!els.list || !els.chat) return;

    if (els.send) els.send.addEventListener('click', sendMessage);
    if (els.text) els.text.addEventListener('keydown', (e)=>{ if(e.key==='Enter' && !e.shiftKey){ e.preventDefault(); sendMessage(); }});

    loadConversations();
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start); else start();
})();
