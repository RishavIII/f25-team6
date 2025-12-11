(function () {
  let msg;
  let edit = {};

  function populateView(profile, tutorInstruments) {
    const photo =
      profile.photoUrl && profile.photoUrl.trim() !== ""
        ? profile.photoUrl
        : "/assets/empty-pfp.svg";
    document.getElementById("viewPhoto").src = photo;

    const name = profile.user && profile.user.name ? profile.user.name : "";
    document.getElementById("viewName").textContent = name;

    document.getElementById("viewBio").textContent = profile.bio || "";
    document.getElementById("viewHourlyRate").textContent =
      formatMoneyFromCents(profile.hourlyRateCents) || "";

    const lessonTypes = [];
    if (profile.onlineEnabled) lessonTypes.push("Online");
    if (profile.inPersonEnabled) lessonTypes.push("In-person");
    document.getElementById("viewLessonTypes").textContent =
      lessonTypes.length > 0 ? lessonTypes.join(", ") : "";

    const instrEl = document.getElementById("viewInstruments");
    instrEl.textContent = "";
    instrEl.innerHTML = "";

    if (tutorInstruments && tutorInstruments.length > 0) {
      const wrapper = document.createElement("div");
      wrapper.className = "pill-list";
      tutorInstruments.forEach((ti) => {
        const instName =
          ti.instrument && ti.instrument.name ? ti.instrument.name : "";
        const minL = ti.minLevel || "BEGINNER";
        const maxL = ti.maxLevel || "EXPERT";
        const pill = document.createElement("span");
        pill.className = "pill";
        pill.textContent = instName
          ? `${instName} (${minL} - ${maxL})`
          : `${minL} – ${maxL}`;
        wrapper.appendChild(pill);
      });
      instrEl.appendChild(wrapper);
    }

    const locParts = [];
    if (profile.city) locParts.push(profile.city);
    if (profile.state) locParts.push(profile.state);
    document.getElementById("viewLocation").textContent = locParts.join(", ");

    let latLonText = "";
    if (profile.latitude != null || profile.longitude != null) {
      const lat =
        profile.latitude != null ? profile.latitude.toFixed(6) : "—";
      const lon =
        profile.longitude != null ? profile.longitude.toFixed(6) : "—";
      latLonText = `${lat}, ${lon}`;
    }
    document.getElementById("viewLatLon").textContent = latLonText;

    document.getElementById("viewTimezone").textContent =
      profile.timezone || "";
    document.getElementById("viewCancel").textContent =
      profile.cancellationNote || "";
  }

  async function loadTutorInstrumentsForUser(userId) {
    try {
      const all = await getTutorInstruments();
      return all.filter(
        (ti) =>
          ti.tutor &&
          (ti.tutor.userId === Number(userId) || ti.tutor.userId === userId)
      );
    } catch (e) {
      console.error(e);
      return [];
    }
  }

  async function loadProfile() {
    const uid = requireUserId();
    if (!uid) return;
    if (!msg) msg = document.getElementById("msg");
    if (msg) msg.textContent = "";

    try {
      const profile = await getTutorProfile(uid);
      const tutorInstruments = await loadTutorInstrumentsForUser(uid);
      populateView(profile, tutorInstruments);
      populateEdit(profile);
    } catch (err) {
      if (err.status === 404) {
        if (msg) msg.textContent =
          "No tutor profile found for this account. Redirecting to onboarding...";
        setTimeout(() => {
          window.location.href = "/Tutor/tutor_onboard.html";
        }, 1200);
      } else {
        console.error(err);
        if (msg) msg.textContent = "Error loading profile.";
      }
    }
  }

  function populateEdit(p){
    edit.photoPreview = document.getElementById('editPhotoPreview');
    edit.photoInput = document.getElementById('editPhoto');
    edit.bio = document.getElementById('editBio');
    edit.hourly = document.getElementById('editHourly');
    edit.online = document.getElementById('editOnline');
    edit.inPerson = document.getElementById('editInPerson');
    edit.city = document.getElementById('editCity');
    edit.state = document.getElementById('editState');
    edit.zip = document.getElementById('editZip');
    edit.lat = document.getElementById('editLat');
    edit.lon = document.getElementById('editLon');
    edit.tz = document.getElementById('editTimezone');
    edit.cancel = document.getElementById('editCancel');

    if (!edit.photoPreview) return;
    edit.photoPreview.src = (p.photoUrl && p.photoUrl.trim()!=='') ? p.photoUrl : '/assets/empty-pfp.svg';
    edit.bio.value = p.bio || '';
    edit.hourly.value = p.hourlyRateCents != null ? (p.hourlyRateCents/100).toFixed(2) : '';
    edit.online.checked = !!p.onlineEnabled;
    edit.inPerson.checked = !!p.inPersonEnabled;
    edit.city.value = p.city || '';
    edit.state.value = p.state || '';
    edit.tz.value = p.timezone || '';
    edit.cancel.value = p.cancellationNote || '';
    edit.lat.value = p.latitude != null ? p.latitude : '';
    edit.lon.value = p.longitude != null ? p.longitude : '';
  }

  async function uploadEditPhoto(){
    const f = edit.photoInput && edit.photoInput.files && edit.photoInput.files[0];
    if (!f) return null;
    const uid = requireUserId(); if (!uid) return null;
    const fd = new FormData(); fd.append('file', f);
    const res = await fetch(`/api/tutor-profiles/${encodeURIComponent(uid)}/photo`, {method:'POST', body: fd});
    if (!res.ok){ const t = await res.text(); throw new Error(`Upload failed: ${res.status} ${t}`); }
    const data = await res.json();
    if (edit.photoPreview) edit.photoPreview.src = data.photoUrl;
    const previewOnView = document.getElementById('viewPhoto'); if (previewOnView) previewOnView.src = data.photoUrl;
    return data.photoUrl;
  }

  async function geocodeZip(zip){
    const res = await fetch(`https://api.zippopotam.us/us/${encodeURIComponent(zip)}`);
    if (!res.ok) throw new Error('Invalid zipcode');
    const data = await res.json();
    const place = data.places && data.places[0];
    return { city: place['place name'], state: place['state abbreviation'], lat: parseFloat(place.latitude), lon: parseFloat(place.longitude) };
  }

  function bindEdit(){
    const btnOpen = document.getElementById('editOpenBtn');
    const card = document.getElementById('editCard');
    const form = document.getElementById('editForm');
    const btnCancel = document.getElementById('editCancelBtn');
    if (btnOpen && card){
      btnOpen.addEventListener('click', ()=>{ card.style.display=''; });
    }
    if (btnCancel && card){ btnCancel.addEventListener('click', ()=>{ card.style.display='none'; }); }
    if (edit.photoInput){ edit.photoInput.addEventListener('change', async ()=>{ try{ await uploadEditPhoto(); }catch(e){ if (msg) msg.textContent = e.message; } }); }
    if (edit.zip){
      edit.zip.addEventListener('blur', async ()=>{
        const z = (edit.zip.value||'').trim(); if (!z) return;
        try{
          const g = await geocodeZip(z);
          edit.city.value = g.city; edit.state.value = g.state; edit.lat.value = g.lat; edit.lon.value = g.lon;
        }catch(e){ if (msg) msg.textContent = 'Invalid zipcode'; }
      });
    }
    if (form){
      form.addEventListener('submit', async (e)=>{
        e.preventDefault(); if (msg) msg.textContent='';
        const uid = requireUserId(); if (!uid) return;
        try{
          // ensure photo upload is applied if any file selected
          if (edit.photoInput && edit.photoInput.files && edit.photoInput.files[0]){
            await uploadEditPhoto();
          }
          const payload = {
            bio: edit.bio.value || null,
            hourlyRateCents: edit.hourly.value ? Math.round(parseFloat(edit.hourly.value)*100) : null,
            onlineEnabled: !!edit.online.checked,
            inPersonEnabled: !!edit.inPerson.checked,
            city: edit.city.value || null,
            state: edit.state.value || null,
            timezone: edit.tz.value || null,
            cancellationNote: edit.cancel.value || null,
            latitude: edit.lat.value ? parseFloat(edit.lat.value) : null,
            longitude: edit.lon.value ? parseFloat(edit.lon.value) : null
          };
          const res = await fetch(`/api/tutor-profiles/${encodeURIComponent(uid)}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)});
          if (!res.ok){ const t = await res.text(); throw new Error(`Save failed: ${res.status} ${t}`); }
          const updated = await res.json();
          populateView(updated, []);
          populateEdit(updated);
          const card = document.getElementById('editCard'); if (card) card.style.display='none';
        }catch(e){ console.error(e); if (msg) msg.textContent = e.message || 'Save failed'; }
      });
    }
  }

  function start(){
    msg = document.getElementById("msg");
    loadProfile();
    bindEdit();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }
})();
