(function () {
  const uid = requireUserId();
  if (!uid) return;

  const els = {
    lessonsList: document.getElementById('upcoming-lessons-list'),
    lessonsEmpty: document.getElementById('upcoming-lessons-empty'),
    reviewsList: document.getElementById('reviews-list'),
    reviewsEmpty: document.getElementById('reviews-empty'),
    reviewsSeeAll: document.getElementById('reviews-see-all'),
    earnings: document.getElementById('metric-earnings'),
    ctr: document.getElementById('metric-ctr'),
    rating: document.getElementById('metric-rating'),
    ratingNote: document.getElementById('metric-rating-note'),
    students: document.getElementById('stat-students'),
    lessonsMonth: document.getElementById('stat-lessons-month'),
    avgLesson: document.getElementById('stat-avg-lesson')
  };

  function tzFormatDate(date) {
    const d = new Date(date);
    const month = d.toLocaleString(undefined, { month: 'short' });
    const day = d.getDate();
    const time = d.toLocaleString(undefined, { hour: 'numeric', minute: '2-digit' });
    return `${month} ${day} · ${time}`;
  }
  function formatMinutes(min) {
    if (min >= 60 && min % 60 === 0) return (min / 60) + 'h';
    if (min >= 60) return Math.floor(min / 60) + 'h ' + (min % 60) + 'm';
    return min + 'm';
  }
  function formatPercent(x) {
    if (isNaN(x)) return '—';
    return (x * 100).toFixed(1) + '%';
  }
  function setText(el, text) { if (el) el.textContent = text; }

  async function load() {
    try {
      const [upcoming, requests, reviews, metrics] = await Promise.all([
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/upcoming-lessons?limit=7`),
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/incoming-requests`),
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/reviews?limit=5`),
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/metrics`)
      ]);

      renderUpcoming(upcoming || []);
      renderRequests(requests || []);

      // Metrics mapping
      setText(els.earnings, formatMoneyFromCents(metrics.monthlyEarningsCents || 0));
      setText(els.ctr, formatPercent(metrics.clickThroughRate ?? NaN));
      renderRating(metrics.overallRatingAvg || 0);
      setText(els.ratingNote, `Based on ${metrics.overallRatingCount || 0} review${(metrics.overallRatingCount || 0) === 1 ? '' : 's'}`);
      setText(els.students, String(metrics.numberOfStudents || 0));
      setText(els.lessonsMonth, String(metrics.lessonsThisMonth || 0));
      const avgMin = Math.round(metrics.averageLessonMinutes || 0);
      setText(els.avgLesson, avgMin ? formatMinutes(avgMin) : '—');

      renderReviews(reviews || []);
    } catch (e) {
      console.error('Failed to load dashboard', e);
    }
  }

  function renderRequests(list) {
    const elList = document.getElementById('incoming-requests-list');
    const elEmpty = document.getElementById('incoming-requests-empty');
    if (!elList) return;
    elList.innerHTML = '';

    if (!list || list.length === 0) {
      if (elEmpty) elEmpty.style.display = 'block';
      return;
    }
    if (elEmpty) elEmpty.style.display = 'none';

    for (const r of list) {
      const li = document.createElement('li');
      const info = document.createElement('div'); info.className = 'lesson-info';

      const strong = document.createElement('strong');
      // IncomingRequestDto uses flattened field names
      strong.textContent = r.studentName || 'Student';

      const meta = document.createElement('span'); meta.className = 'lesson-meta';
      const instr = r.instrumentName || 'Lesson';
      const when = r.createdAt ? tzFormatDate(r.createdAt) : '';
      const details = r.details || '';
      meta.textContent = details ? `${instr} — ${details}` : instr;

      info.appendChild(strong); info.appendChild(meta);

      const actions = document.createElement('div'); actions.className = 'lesson-actions';

      const btnAccept = document.createElement('button');
      btnAccept.className = 'action'; btnAccept.type = 'button'; btnAccept.textContent = 'Accept';
      btnAccept.style.color = 'green';
      btnAccept.onclick = async () => {
        if (!confirm('Accept this request?')) return;
        try {
          await apiJson(`/booking-requests/${r.id}/accept`, { method: 'POST' });
          li.remove();
          if (elList.children.length === 0 && elEmpty) elEmpty.style.display = 'block';
        } catch (e) { alert('Failed to accept: ' + e.message); }
      };

      const btnDecline = document.createElement('button');
      btnDecline.className = 'action'; btnDecline.type = 'button'; btnDecline.textContent = 'Decline';
      btnDecline.style.color = 'red';
      btnDecline.onclick = async () => {
        if (!confirm('Decline this request?')) return;
        try {
          await apiJson(`/booking-requests/${r.id}/decline`, { method: 'POST' });
          li.remove();
          if (elList.children.length === 0 && elEmpty) elEmpty.style.display = 'block';
        } catch (e) { alert('Failed to decline: ' + e.message); }
      };

      actions.appendChild(btnAccept);
      actions.appendChild(btnDecline);

      li.appendChild(info); li.appendChild(actions);
      elList.appendChild(li);
    }
  }

  function renderUpcoming(list) {
    if (!els.lessonsList) return;
    els.lessonsList.innerHTML = '';
    if (!list || list.length === 0) {
      if (els.lessonsEmpty) els.lessonsEmpty.style.display = '';
      return;
    }
    if (els.lessonsEmpty) els.lessonsEmpty.style.display = 'none';

    for (const l of list) {
      const li = document.createElement('li');
      const info = document.createElement('div'); info.className = 'lesson-info';
      const strong = document.createElement('strong');
      strong.textContent = l.studentName || 'Student';
      const meta = document.createElement('span'); meta.className = 'lesson-meta';
      const instr = l.instrumentName || 'Lesson';
      const when = tzFormatDate(l.startUtc);
      const dur = l.durationMin || 0;
      const price = l.priceCents ? formatMoneyFromCents(l.priceCents) : '';
      meta.textContent = `${instr} — ${when} · ${formatMinutes(dur)}${price ? ' · ' + price : ''}`;
      info.appendChild(strong); info.appendChild(meta);

      const actions = document.createElement('div'); actions.className = 'lesson-actions';

      // Mark Completed button
      const btnComplete = document.createElement('button');
      btnComplete.className = 'action'; btnComplete.type = 'button';
      btnComplete.textContent = 'Mark Completed';
      btnComplete.style.color = 'green';
      btnComplete.onclick = async () => {
        if (!confirm(`Mark this lesson as completed and process payment${l.priceCents ? ' (' + formatMoneyFromCents(l.priceCents) + ')' : ''}?`)) return;
        try {
          await apiJson(`/lessons/${l.id}/complete`, { method: 'POST' });
          alert('Lesson marked as completed! Payment processed.');
          load(); // Reload the entire dashboard to update metrics
        } catch (e) { alert('Failed to complete lesson: ' + e.message); }
      };
      actions.appendChild(btnComplete);

      // Details button - opens modal
      const btnDetails = document.createElement('button');
      btnDetails.className = 'action'; btnDetails.type = 'button';
      btnDetails.textContent = 'Details';
      btnDetails.onclick = () => window.showLessonModal(l);
      actions.appendChild(btnDetails);

      // Message button - navigates to messaging page
      const btnMessage = document.createElement('button');
      btnMessage.className = 'action'; btnMessage.type = 'button';
      btnMessage.textContent = 'Message';
      btnMessage.onclick = async () => {
        if (!l.studentId) { alert('Cannot message: student info not available'); return; }
        // Find or create conversation with this student
        try {
          const convos = await apiJson(`/tutors/${encodeURIComponent(uid)}/conversations`);
          const convo = convos.find(c => c.otherUserId === l.studentId);
          if (convo) {
            window.location.href = `tutor-message.html?conversationId=${convo.conversationId}`;
          } else {
            // Navigate to message page - it will start a new conversation
            window.location.href = `tutor-message.html?studentId=${l.studentId}`;
          }
        } catch (e) {
          console.error(e);
          window.location.href = 'tutor-message.html';
        }
      };
      actions.appendChild(btnMessage);

      li.appendChild(info); li.appendChild(actions);
      els.lessonsList.appendChild(li);
    }
  }

  // Modal functions
  window.showLessonModal = function (lesson) {
    const modal = document.getElementById('lessonModal');
    const content = document.getElementById('modalContent');
    if (!modal || !content) return;

    const when = tzFormatDate(lesson.startUtc);
    const dur = lesson.durationMin || 0;
    const price = lesson.priceCents ? formatMoneyFromCents(lesson.priceCents) : 'N/A';

    content.innerHTML = `
      <div class="modal-row">
        <span class="modal-label">Student</span>
        <span class="modal-value">${lesson.studentName || 'Unknown'}</span>
      </div>
      <div class="modal-row">
        <span class="modal-label">Instrument</span>
        <span class="modal-value">${lesson.instrumentName || 'Unknown'}</span>
      </div>
      <div class="modal-row">
        <span class="modal-label">Date & Time</span>
        <span class="modal-value">${when}</span>
      </div>
      <div class="modal-row">
        <span class="modal-label">Duration</span>
        <span class="modal-value">${formatMinutes(dur)}</span>
      </div>
      <div class="modal-row">
        <span class="modal-label">Price</span>
        <span class="modal-value">${price}</span>
      </div>
    `;
    modal.classList.add('active');
  };

  window.closeLessonModal = function () {
    const modal = document.getElementById('lessonModal');
    if (modal) modal.classList.remove('active');
  };

  // Close modal when clicking outside the modal content
  document.addEventListener('click', function (e) {
    const modal = document.getElementById('lessonModal');
    if (modal && e.target === modal) {
      window.closeLessonModal();
    }
  });

  function renderRating(avg) {
    if (!els.rating) return;
    const rounded = (Math.round((avg || 0) * 10) / 10).toFixed(1);
    const stars = Math.round(avg || 0);
    const starStr = Array.from({ length: 5 }, (_, i) => i < stars ? '★' : '☆').join(' ');
    els.rating.textContent = '';
    els.rating.setAttribute('aria-label', `${rounded} out of 5 stars`);
    const spanStars = document.createElement('span'); spanStars.textContent = starStr + ' ';
    const spanVal = document.createElement('span'); spanVal.className = 'rating-value'; spanVal.textContent = rounded;
    els.rating.appendChild(spanStars); els.rating.appendChild(spanVal);
  }

  function renderReviews(items) {
    if (!els.reviewsList || !els.reviewsEmpty) return;
    els.reviewsList.innerHTML = '';
    if (!items || items.length === 0) {
      els.reviewsEmpty.style.display = '';
      els.reviewsEmpty.textContent = 'No reviews yet.';
      if (els.reviewsSeeAll) els.reviewsSeeAll.style.display = 'none';
      return;
    }
    els.reviewsEmpty.style.display = 'none';
    for (const r of items) {
      const li = document.createElement('li');
      const avatar = document.createElement('div'); avatar.className = 'avatar';
      const initials = (r.studentName || 'U').split(' ').map(s => s[0]).join('').slice(0, 2).toUpperCase();
      avatar.textContent = initials;
      const body = document.createElement('div'); body.className = 'review-body';
      const strong = document.createElement('strong'); strong.textContent = r.studentName || 'Student';
      const stars = document.createElement('div'); stars.className = 'stars';
      stars.setAttribute('aria-hidden', 'true');
      const starStr = Array.from({ length: 5 }, (_, i) => i < (r.rating || 0) ? '★' : '☆').join('');
      stars.textContent = starStr;
      const p = document.createElement('p'); p.textContent = r.text || '';
      body.appendChild(strong); body.appendChild(stars); body.appendChild(p);
      li.appendChild(avatar); li.appendChild(body);
      els.reviewsList.appendChild(li);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', load);
  } else {
    load();
  }
})();
