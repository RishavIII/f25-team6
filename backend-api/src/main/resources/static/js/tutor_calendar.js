(function(){
  const uid = requireUserId();
  if (!uid) return;
  let els = { title: null, dayList: null };

  function titleFor(y,m){
    const d = new Date(y, m-1, 1);
    return d.toLocaleString(undefined, { month:'long', year:'numeric' });
  }
  function fmtTime(iso){
    const d = new Date(iso);
    return d.toLocaleTimeString([], {hour:'numeric', minute:'2-digit'});
  }

  async function load(y,m){
    const events = await apiJson(`/tutors/${encodeURIComponent(uid)}/calendar?year=${y}&month=${m}`);
    render(y,m,events);
  }

  function render(y,m,events){
    if (els.title) els.title.textContent = titleFor(y,m);
    els.dayList.innerHTML = '';
    const today = new Date();
    const dayEvents = events.filter(e => new Date(e.startUtc).toDateString() === today.toDateString());
    if (dayEvents.length === 0){
      const li = document.createElement('li'); li.textContent = 'No sessions today.'; els.dayList.appendChild(li); return;
    }
    for (const ev of dayEvents){
      const start = fmtTime(ev.startUtc); const end = fmtTime(ev.endUtc);
      const li = document.createElement('li');
      const strong = document.createElement('strong'); strong.textContent = `${start}–${end}`;
      const text = document.createTextNode(` • Lesson with ${ev.studentName || 'Student'} (${(ev.mode||'').toLowerCase().replace('_',' ')})`);
      li.appendChild(strong); li.appendChild(text); els.dayList.appendChild(li);
    }
  }

  function start(){
    els.title = document.getElementById('calMonthTitle');
    els.dayList = document.getElementById('dayList');
    if (!els.dayList) return;
    const now = new Date();
    load(now.getFullYear(), now.getMonth()+1);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start); else start();
})();
