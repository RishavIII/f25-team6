(function(){
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

  function tzFormatDate(date){
    const d = new Date(date);
    const month = d.toLocaleString(undefined, { month: 'short' });
    const day = d.getDate();
    const time = d.toLocaleString(undefined, { hour: 'numeric', minute: '2-digit' });
    return `${month} ${day} · ${time}`;
  }
  function formatMinutes(min){
    if (min >= 60 && min % 60 === 0) return (min/60) + 'h';
    if (min >= 60) return Math.floor(min/60) + 'h ' + (min%60) + 'm';
    return min + 'm';
  }
  function formatPercent(x){
    if (isNaN(x)) return '—';
    return (x*100).toFixed(1) + '%';
  }
  function setText(el, text){ if (el) el.textContent = text; }

  async function load(){
    try {
      const [upcoming, reviews, metrics] = await Promise.all([
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/upcoming-lessons?limit=7`),
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/reviews?limit=5`),
        apiJson(`/tutors/${encodeURIComponent(uid)}/dashboard/metrics`)
      ]);

      renderUpcoming(upcoming || []);

      // Metrics mapping
      setText(els.earnings, formatMoneyFromCents(metrics.monthlyEarningsCents || 0));
      setText(els.ctr, formatPercent(metrics.clickThroughRate ?? NaN));
      renderRating(metrics.overallRatingAvg || 0);
      setText(els.ratingNote, `Based on ${metrics.overallRatingCount || 0} review${(metrics.overallRatingCount||0)===1?'':'s'}`);
      setText(els.students, String(metrics.numberOfStudents || 0));
      setText(els.lessonsMonth, String(metrics.lessonsThisMonth || 0));
      const avgMin = Math.round(metrics.averageLessonMinutes || 0);
      setText(els.avgLesson, avgMin ? formatMinutes(avgMin) : '—');

      renderReviews(reviews || []);
    } catch (e) {
      console.error('Failed to load dashboard', e);
    }
  }

  function renderUpcoming(list){
    if (!els.lessonsList) return;
    els.lessonsList.innerHTML = '';
    if (!list || list.length === 0){
      if (els.lessonsEmpty) els.lessonsEmpty.style.display = '';
      return;
    }
    if (els.lessonsEmpty) els.lessonsEmpty.style.display = 'none';

    for (const l of list){
      const li = document.createElement('li');
      const info = document.createElement('div'); info.className = 'lesson-info';
      const strong = document.createElement('strong');
      strong.textContent = l.studentName || 'Student';
      const meta = document.createElement('span'); meta.className = 'lesson-meta';
      const instr = l.instrumentName || 'Lesson';
      const when = tzFormatDate(l.startUtc);
      const dur = l.durationMin || 0;
      meta.textContent = `${instr} — ${when} · ${formatMinutes(dur)}`;
      info.appendChild(strong); info.appendChild(meta);

      const actions = document.createElement('div'); actions.className = 'lesson-actions';
      for (const label of ['Details','Reschedule','Message']){
        const btn = document.createElement('button');
        btn.className = 'action'; btn.type = 'button'; btn.textContent = label;
        // Handlers to be implemented in next step
        actions.appendChild(btn);
      }

      li.appendChild(info); li.appendChild(actions);
      els.lessonsList.appendChild(li);
    }
  }

  function renderRating(avg){
    if (!els.rating) return;
    const rounded = (Math.round((avg || 0) * 10) / 10).toFixed(1);
    const stars = Math.round(avg || 0);
    const starStr = Array.from({length:5}, (_,i)=> i < stars ? '★' : '☆').join(' ');
    els.rating.textContent = '';
    els.rating.setAttribute('aria-label', `${rounded} out of 5 stars`);
    const spanStars = document.createElement('span'); spanStars.textContent = starStr + ' ';
    const spanVal = document.createElement('span'); spanVal.className = 'rating-value'; spanVal.textContent = rounded;
    els.rating.appendChild(spanStars); els.rating.appendChild(spanVal);
  }

  function renderReviews(items){
    if (!els.reviewsList || !els.reviewsEmpty) return;
    els.reviewsList.innerHTML = '';
    if (!items || items.length === 0){
      els.reviewsEmpty.style.display = '';
      els.reviewsEmpty.textContent = 'No reviews yet.';
      if (els.reviewsSeeAll) els.reviewsSeeAll.style.display = 'none';
      return;
    }
    els.reviewsEmpty.style.display = 'none';
    for (const r of items){
      const li = document.createElement('li');
      const avatar = document.createElement('div'); avatar.className = 'avatar';
      const initials = (r.studentName || 'U').split(' ').map(s=>s[0]).join('').slice(0,2).toUpperCase();
      avatar.textContent = initials;
      const body = document.createElement('div'); body.className = 'review-body';
      const strong = document.createElement('strong'); strong.textContent = r.studentName || 'Student';
      const stars = document.createElement('div'); stars.className='stars';
      stars.setAttribute('aria-hidden','true');
      const starStr = Array.from({length:5}, (_,i)=> i < (r.rating||0) ? '★' : '☆').join('');
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
