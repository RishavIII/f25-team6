(function () {
  const uid = requireUserId();
  if (!uid) return;

  // State
  let currentYear, currentMonth; // month is 1-12
  let selectedDate = null;
  let eventsCache = [];

  // DOM elements
  const els = {
    title: null,
    grid: null,
    dayList: null,
    dayDetailTitle: null,
    btnPrev: null,
    btnToday: null,
    btnNext: null
  };

  // Format helpers
  function titleFor(y, m) {
    const d = new Date(y, m - 1, 1);
    return d.toLocaleString(undefined, { month: 'long', year: 'numeric' });
  }

  function fmtTime(iso) {
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
  }

  function formatDate(date) {
    const options = { weekday: 'long', month: 'long', day: 'numeric' };
    return date.toLocaleDateString(undefined, options);
  }

  function isSameDay(d1, d2) {
    return d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate();
  }

  // Get events for a specific day
  function getEventsForDay(date) {
    return eventsCache.filter(e => isSameDay(new Date(e.startUtc), date));
  }

  // Render the calendar grid
  function renderGrid(y, m, events) {
    eventsCache = events;
    if (!els.grid) return;
    els.grid.innerHTML = '';

    const firstDay = new Date(y, m - 1, 1);
    const lastDay = new Date(y, m, 0);
    const startDayOfWeek = firstDay.getDay(); // 0 = Sunday
    const totalDays = lastDay.getDate();
    const prevMonthLastDay = new Date(y, m - 1, 0).getDate();

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Previous month days
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const dayNum = prevMonthLastDay - i;
      const dayDiv = createDayCell(dayNum, true, new Date(y, m - 2, dayNum), events);
      els.grid.appendChild(dayDiv);
    }

    // Current month days
    for (let day = 1; day <= totalDays; day++) {
      const date = new Date(y, m - 1, day);
      const isToday = isSameDay(date, today);
      const dayDiv = createDayCell(day, false, date, events, isToday);
      els.grid.appendChild(dayDiv);
    }

    // Next month days to fill the grid
    const totalCells = els.grid.children.length;
    const remainingCells = (7 - (totalCells % 7)) % 7;
    for (let day = 1; day <= remainingCells; day++) {
      const dayDiv = createDayCell(day, true, new Date(y, m, day), events);
      els.grid.appendChild(dayDiv);
    }

    // Select today by default if in current month, otherwise first day
    if (selectedDate === null || selectedDate.getMonth() !== m - 1 || selectedDate.getFullYear() !== y) {
      if (today.getMonth() === m - 1 && today.getFullYear() === y) {
        selectedDate = today;
      } else {
        selectedDate = new Date(y, m - 1, 1);
      }
    }
    updateSelection();
    renderDayDetail(selectedDate);
  }

  function createDayCell(dayNum, isMuted, date, events, isToday = false) {
    const dayDiv = document.createElement('div');
    dayDiv.className = 'cal-day';
    if (isMuted) dayDiv.classList.add('is-muted');
    if (isToday) dayDiv.classList.add('today');

    const numSpan = document.createElement('span');
    numSpan.className = 'num';
    numSpan.textContent = dayNum;
    dayDiv.appendChild(numSpan);

    // Get events for this day
    const dayEvents = getEventsForDay(date);

    // Check if any events are unviewed
    const viewed = window.getViewedLessonIds ? window.getViewedLessonIds() : [];
    const unviewedEvents = dayEvents.filter(ev => !viewed.includes(ev.id));

    if (dayEvents.length > 0) {
      // Show badge only if there are unviewed events
      if (unviewedEvents.length > 0) {
        const badge = document.createElement('span');
        badge.className = 'badge';
        badge.textContent = unviewedEvents.length;
        dayDiv.appendChild(badge);
      }

      // Show event dots
      const dotsDiv = document.createElement('div');
      dotsDiv.className = 'event-dots';
      dayEvents.slice(0, 5).forEach(ev => {
        const dot = document.createElement('span');
        dot.className = 'event-dot';
        if (ev.status === 'COMPLETED') dot.classList.add('completed');
        if (ev.status === 'CANCELED') dot.classList.add('canceled');
        dotsDiv.appendChild(dot);
      });
      dayDiv.appendChild(dotsDiv);
    }

    // Store date for click handling
    dayDiv.dataset.date = date.toISOString();
    dayDiv.addEventListener('click', () => {
      selectedDate = new Date(dayDiv.dataset.date);
      updateSelection();
      renderDayDetail(selectedDate);

      // Mark all lessons on this day as viewed
      const clickedDayEvents = getEventsForDay(selectedDate);
      if (clickedDayEvents.length > 0 && window.markLessonsAsViewed) {
        window.markLessonsAsViewed(clickedDayEvents.map(ev => ev.id));
        // Also hide the badge on this cell
        const badge = dayDiv.querySelector('.badge');
        if (badge) badge.remove();
      }
    });

    return dayDiv;
  }

  function updateSelection() {
    if (!els.grid) return;
    const cells = els.grid.querySelectorAll('.cal-day');
    cells.forEach(cell => {
      const cellDate = new Date(cell.dataset.date);
      if (selectedDate && isSameDay(cellDate, selectedDate)) {
        cell.classList.add('selected');
      } else {
        cell.classList.remove('selected');
      }
    });
  }

  function renderDayDetail(date) {
    if (!els.dayList || !els.dayDetailTitle) return;

    els.dayDetailTitle.textContent = `Sessions — ${formatDate(date)}`;
    els.dayList.innerHTML = '';

    const dayEvents = getEventsForDay(date);
    if (dayEvents.length === 0) {
      const li = document.createElement('li');
      li.className = 'no-events';
      li.textContent = 'No sessions scheduled for this day.';
      els.dayList.appendChild(li);
      return;
    }

    // Sort by start time
    dayEvents.sort((a, b) => new Date(a.startUtc) - new Date(b.startUtc));

    for (const ev of dayEvents) {
      const li = document.createElement('li');

      const infoDiv = document.createElement('div');
      infoDiv.className = 'lesson-info';

      const timeSpan = document.createElement('span');
      timeSpan.className = 'lesson-time';
      timeSpan.textContent = `${fmtTime(ev.startUtc)} – ${fmtTime(ev.endUtc)}`;

      const metaSpan = document.createElement('span');
      metaSpan.className = 'lesson-meta';
      const modeName = (ev.mode || '').toLowerCase().replace('_', ' ');
      metaSpan.textContent = `Lesson with ${ev.studentName || 'Student'}${modeName ? ' (' + modeName + ')' : ''}`;

      infoDiv.appendChild(timeSpan);
      infoDiv.appendChild(metaSpan);

      const statusSpan = document.createElement('span');
      statusSpan.className = 'lesson-status';
      const status = (ev.status || 'SCHEDULED').toLowerCase();
      statusSpan.classList.add(status);
      statusSpan.textContent = status.charAt(0).toUpperCase() + status.slice(1);

      li.appendChild(infoDiv);
      li.appendChild(statusSpan);
      els.dayList.appendChild(li);
    }
  }

  async function loadMonth(y, m) {
    currentYear = y;
    currentMonth = m;
    if (els.title) els.title.textContent = titleFor(y, m);

    try {
      const events = await apiJson(`/tutors/${encodeURIComponent(uid)}/calendar?year=${y}&month=${m}`);
      renderGrid(y, m, events || []);
    } catch (e) {
      console.error('Failed to load calendar', e);
      renderGrid(y, m, []);
    }
  }

  function goToPrevMonth() {
    let y = currentYear, m = currentMonth - 1;
    if (m < 1) { m = 12; y--; }
    loadMonth(y, m);
  }

  function goToNextMonth() {
    let y = currentYear, m = currentMonth + 1;
    if (m > 12) { m = 1; y++; }
    loadMonth(y, m);
  }

  function goToToday() {
    const now = new Date();
    selectedDate = now;
    loadMonth(now.getFullYear(), now.getMonth() + 1);
  }

  function init() {
    els.title = document.getElementById('calMonthTitle');
    els.grid = document.getElementById('calGrid');
    els.dayList = document.getElementById('dayList');
    els.dayDetailTitle = document.getElementById('dayDetailTitle');
    els.btnPrev = document.getElementById('btnPrev');
    els.btnToday = document.getElementById('btnToday');
    els.btnNext = document.getElementById('btnNext');

    if (!els.grid) return;

    // Set up navigation
    if (els.btnPrev) els.btnPrev.addEventListener('click', goToPrevMonth);
    if (els.btnToday) els.btnToday.addEventListener('click', goToToday);
    if (els.btnNext) els.btnNext.addEventListener('click', goToNextMonth);

    // Load current month
    const now = new Date();
    selectedDate = now;
    loadMonth(now.getFullYear(), now.getMonth() + 1);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
