(function () {
  let msg;
  let edit = {};
  let allInstruments = []; // All available instruments
  let currentTutorInstruments = []; // Current tutor's instruments
  const levels = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];

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

    document.getElementById("viewZipcode").textContent = profile.zipcode || "";

    document.getElementById("viewTimezone").textContent =
      profile.timezone || "";
    document.getElementById("viewCancel").textContent =
      profile.cancellationNote || "";
  }

  async function loadTutorInstrumentsForUser(userId) {
    try {
      const all = await getTutorInstruments();
      return all.filter(
        (ti) => (ti.tutorId === Number(userId) || ti.tutorId === userId)
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
      // Load all instruments for the dropdown
      allInstruments = await getInstruments();

      const profile = await getTutorProfile(uid);
      currentTutorInstruments = await loadTutorInstrumentsForUser(uid);
      populateView(profile, currentTutorInstruments);
      populateEdit(profile, currentTutorInstruments);
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

  function makeLevelSelect(defaultValue, dataRole) {
    const sel = document.createElement("select");
    sel.dataset.role = dataRole;

    levels.forEach((lvl) => {
      const opt = document.createElement("option");
      opt.value = lvl;
      opt.textContent = lvl.charAt(0) + lvl.slice(1).toLowerCase();
      sel.appendChild(opt);
    });

    if (defaultValue && levels.includes(defaultValue)) {
      sel.value = defaultValue;
    }
    return sel;
  }

  function createInstrumentRow(existingTi = null) {
    const container = document.getElementById('editInstrumentRows');
    if (!container) return;
    if (!allInstruments || allInstruments.length === 0) {
      if (msg) msg.textContent = "Instruments are loading...";
      return;
    }

    const row = document.createElement("div");
    row.className = "instrument-row";
    if (existingTi && existingTi.id) {
      row.dataset.existingId = existingTi.id;
    }

    // Instrument wrapper
    const instWrapper = document.createElement("div");
    instWrapper.style.flex = "1 1 180px";

    const instLabel = document.createElement("div");
    instLabel.textContent = "Instrument";
    instLabel.style.fontSize = "0.85rem";
    instLabel.style.color = "#6b7280";
    instLabel.style.marginBottom = "4px";

    const instSelect = document.createElement("select");
    instSelect.style.width = "100%";
    instSelect.dataset.role = "instrument";

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Select instrument";
    placeholder.disabled = true;
    if (!existingTi) placeholder.selected = true;
    instSelect.appendChild(placeholder);

    allInstruments.forEach((inst) => {
      const opt = document.createElement("option");
      opt.value = inst.id;
      opt.textContent = inst.name;
      if (existingTi && existingTi.instrument && existingTi.instrument.id === inst.id) {
        opt.selected = true;
      }
      instSelect.appendChild(opt);
    });

    instWrapper.appendChild(instLabel);
    instWrapper.appendChild(instSelect);

    // Levels wrapper
    const levelsWrapper = document.createElement("div");
    levelsWrapper.style.display = "flex";
    levelsWrapper.style.flex = "1 1 260px";
    levelsWrapper.style.gap = "8px";
    levelsWrapper.style.alignItems = "center";

    const minWrapper = document.createElement("div");
    minWrapper.style.flex = "1";
    const minLabel = document.createElement("div");
    minLabel.textContent = "Min level";
    minLabel.style.fontSize = "0.85rem";
    minLabel.style.color = "#6b7280";
    minWrapper.appendChild(minLabel);
    const minSelect = makeLevelSelect(existingTi ? existingTi.minLevel : "BEGINNER", "minLevel");
    minSelect.style.width = "100%";
    minWrapper.appendChild(minSelect);

    const maxWrapper = document.createElement("div");
    maxWrapper.style.flex = "1";
    const maxLabel = document.createElement("div");
    maxLabel.textContent = "Max level";
    maxLabel.style.fontSize = "0.85rem";
    maxLabel.style.color = "#6b7280";
    maxWrapper.appendChild(maxLabel);
    const maxSelect = makeLevelSelect(existingTi ? existingTi.maxLevel : "EXPERT", "maxLevel");
    maxSelect.style.width = "100%";
    maxWrapper.appendChild(maxSelect);

    levelsWrapper.appendChild(minWrapper);
    levelsWrapper.appendChild(maxWrapper);

    // Remove button
    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.textContent = "Remove";
    removeBtn.className = "remove-btn";
    removeBtn.addEventListener("click", () => row.remove());

    row.appendChild(instWrapper);
    row.appendChild(levelsWrapper);
    row.appendChild(removeBtn);

    container.appendChild(row);
  }

  function populateInstrumentRows(tutorInstruments) {
    const container = document.getElementById('editInstrumentRows');
    if (!container) return;
    container.innerHTML = '';

    if (tutorInstruments && tutorInstruments.length > 0) {
      tutorInstruments.forEach(ti => createInstrumentRow(ti));
    }
  }

  function collectInstrumentSelections() {
    const selections = [];
    document.querySelectorAll("#editInstrumentRows .instrument-row").forEach((row) => {
      const instSelect = row.querySelector("select[data-role='instrument']");
      const minSelect = row.querySelector("select[data-role='minLevel']");
      const maxSelect = row.querySelector("select[data-role='maxLevel']");
      if (!instSelect) return;
      const instId = instSelect.value;
      if (!instId) return;
      const existingId = row.dataset.existingId ? parseInt(row.dataset.existingId, 10) : null;
      selections.push({
        existingId,
        instrumentId: parseInt(instId, 10),
        minLevel: (minSelect && minSelect.value) || "BEGINNER",
        maxLevel: (maxSelect && maxSelect.value) || "EXPERT",
      });
    });
    return selections;
  }

  function populateEdit(p, tutorInstruments) {
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

    // Populate text fields
    if (edit.bio) edit.bio.value = p.bio || '';
    if (edit.hourly) edit.hourly.value = p.hourlyRateCents != null ? (p.hourlyRateCents / 100).toFixed(2) : '';
    if (edit.online) edit.online.checked = !!p.onlineEnabled;
    if (edit.inPerson) edit.inPerson.checked = !!p.inPersonEnabled;
    if (edit.city) edit.city.value = p.city || '';
    if (edit.state) edit.state.value = p.state || '';
    if (edit.tz) edit.tz.value = p.timezone || '';
    if (edit.cancel) edit.cancel.value = p.cancellationNote || '';
    if (edit.zip) edit.zip.value = p.zipcode || '';
    if (edit.lat) edit.lat.value = p.latitude != null ? p.latitude : '';
    if (edit.lon) edit.lon.value = p.longitude != null ? p.longitude : '';

    // Populate instrument rows
    populateInstrumentRows(tutorInstruments);

    // Re-attach zipcode autofill event listener
    attachZipcodeAutofill();
  }

  function attachZipcodeAutofill() {
    if (edit.zip) {
      // Remove any existing listener to avoid duplicates
      edit.zip.removeEventListener('input', handleZipcodeInput);
      // Attach the event listener
      edit.zip.addEventListener('input', handleZipcodeInput);
    }
  }

  async function handleZipcodeInput() {
    const z = (edit.zip.value || '').trim();
    if (z && z.length === 5 && /^\d{5}$/.test(z)) {
      try {
        const g = await geocodeZip(z);
        edit.city.value = g.city;
        edit.state.value = g.state;
        edit.lat.value = g.lat;
        edit.lon.value = g.lon;
      } catch (e) { /* ignore invalid zip */ }
    }
  }

  async function uploadEditPhoto() {
    const f = edit.photoInput && edit.photoInput.files && edit.photoInput.files[0];
    if (!f) return null;
    const uid = requireUserId(); if (!uid) return null;
    const fd = new FormData(); fd.append('file', f);
    const res = await fetch(`/api/tutor-profiles/${encodeURIComponent(uid)}/photo`, { method: 'POST', body: fd });
    if (!res.ok) { const t = await res.text(); throw new Error(`Upload failed: ${res.status} ${t}`); }
    const data = await res.json();
    const previewOnView = document.getElementById('viewPhoto');
    if (previewOnView) previewOnView.src = data.photoUrl + '?t=' + new Date().getTime();
    return data.photoUrl;
  }

  async function geocodeZip(zip) {
    const res = await fetch(`https://api.zippopotam.us/us/${encodeURIComponent(zip)}`);
    if (!res.ok) throw new Error('Invalid zipcode');
    const data = await res.json();
    const place = data.places && data.places[0];
    return { city: place['place name'], state: place['state abbreviation'], lat: parseFloat(place.latitude), lon: parseFloat(place.longitude) };
  }

  async function saveInstruments(uid, selections) {
    // Get current tutor instruments
    const current = await loadTutorInstrumentsForUser(uid);
    const currentIds = current.map(ti => ti.id);
    const selectedExistingIds = selections.filter(s => s.existingId).map(s => s.existingId);

    // Delete instruments that were removed
    for (const ti of current) {
      if (!selectedExistingIds.includes(ti.id)) {
        await fetch(`/api/tutor-instruments/${ti.id}`, { method: 'DELETE' });
      }
    }

    // Create new instruments
    for (const sel of selections) {
      if (!sel.existingId) {
        await createTutorInstrument({
          tutorUserId: Number(uid),
          instrumentId: sel.instrumentId,
          minLevel: sel.minLevel,
          maxLevel: sel.maxLevel,
        });
      }
    }
  }

  function bindEdit() {
    const btnOpen = document.getElementById('editOpenBtn');
    const form = document.getElementById('profileForm');
    const btnCancel = document.getElementById('editCancelBtn');
    const addInstrumentBtn = document.getElementById('addInstrumentBtn');

    if (btnOpen && form) {
      btnOpen.addEventListener('click', () => { form.classList.add('editing'); });
    }
    if (btnCancel && form) {
      btnCancel.addEventListener('click', async () => {
        form.classList.remove('editing');
        // Re-populate edit fields with current data to reset changes
        const uid = requireUserId();
        if (uid) {
          try {
            const profile = await getTutorProfile(uid);
            currentTutorInstruments = await loadTutorInstrumentsForUser(uid);
            populateEdit(profile, currentTutorInstruments);
          } catch (e) { console.error(e); }
        }
      });
    }
    if (addInstrumentBtn) {
      addInstrumentBtn.addEventListener('click', () => createInstrumentRow());
    }
    const photoInput = document.getElementById('editPhoto');
    if (photoInput) {
      photoInput.addEventListener('change', () => {
        // Show local preview immediately (don't upload yet)
        const f = photoInput.files && photoInput.files[0];
        if (f) {
          const reader = new FileReader();
          reader.onload = function (e) {
            const viewPhoto = document.getElementById('viewPhoto');
            if (viewPhoto) viewPhoto.src = e.target.result;
          };
          reader.readAsDataURL(f);
        }
      });
    }
    if (form) {
      form.addEventListener('submit', async (e) => {
        e.preventDefault(); if (msg) msg.textContent = '';
        const uid = requireUserId(); if (!uid) return;
        try {
          // ensure photo upload is applied if any file selected
          if (edit.photoInput && edit.photoInput.files && edit.photoInput.files[0]) {
            await uploadEditPhoto();
          }

          // Save profile
          const payload = {
            bio: edit.bio.value || null,
            hourlyRateCents: edit.hourly.value ? Math.round(parseFloat(edit.hourly.value) * 100) : null,
            onlineEnabled: !!edit.online.checked,
            inPersonEnabled: !!edit.inPerson.checked,
            city: edit.city.value || null,
            state: edit.state.value || null,
            zipcode: edit.zip.value || null,
            timezone: edit.tz.value || null,
            cancellationNote: edit.cancel.value || null,
            latitude: edit.lat.value ? parseFloat(edit.lat.value) : null,
            longitude: edit.lon.value ? parseFloat(edit.lon.value) : null
          };
          const res = await fetch(`/api/tutor-profiles/${encodeURIComponent(uid)}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
          if (!res.ok) { const t = await res.text(); throw new Error(`Save failed: ${res.status} ${t}`); }
          const updated = await res.json();

          // Save instruments
          const instrumentSelections = collectInstrumentSelections();
          await saveInstruments(uid, instrumentSelections);

          // Reload and display
          currentTutorInstruments = await loadTutorInstrumentsForUser(uid);
          populateView(updated, currentTutorInstruments);
          populateEdit(updated, currentTutorInstruments);
          form.classList.remove('editing');
        } catch (e) { console.error(e); if (msg) msg.textContent = e.message || 'Save failed'; }
      });
    }
  }

  function start() {
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
