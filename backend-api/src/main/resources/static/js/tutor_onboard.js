(function () {
  let form, msg, fileInput, instrumentRowsContainer, addInstrumentBtn;
  let uploadedPhotoUrl = null;
  let instruments = [];
  const levels = ["BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"];
  (function prefillTimezone() {
    try {
      const tzInput = document.getElementById("timezone");
      if (tzInput && !tzInput.value) {
        tzInput.value = Intl.DateTimeFormat().resolvedOptions().timeZone;
      }
    } catch (e) { }
  })();

  function makeLevelSelect(defaultValue, dataRole) {
    const sel = document.createElement("select");
    sel.style.padding = "10px";
    sel.style.borderRadius = "8px";
    sel.style.border = "1px solid rgba(15, 23, 42, 0.12)";
    sel.style.background = "#fff";
    sel.style.fontSize = "0.95rem";
    sel.dataset.role = dataRole;

    levels.forEach((lvl) => {
      const opt = document.createElement("option");
      opt.value = lvl;
      opt.textContent =
        lvl.charAt(0) + lvl.slice(1).toLowerCase().replace("_", " ");
      sel.appendChild(opt);
    });

    if (defaultValue && levels.includes(defaultValue)) {
      sel.value = defaultValue;
    }
    return sel;
  }

  function createInstrumentRow() {
    if (!instrumentRowsContainer) return;
    if (!instruments || instruments.length === 0) {
      if (msg) msg.textContent =
        "Instruments are stilll loading. Please try again in a moment.";
      return;
    }

    const row = document.createElement("div");
    row.className = "instrument-row";
    row.style.display = "flex";
    row.style.flexWrap = "wrap";
    row.style.gap = "12px";
    row.style.alignItems = "center";
    row.style.marginBottom = "12px";

    const instWrapper = document.createElement("div");
    instWrapper.style.flex = "1 1 180px";

    const instLabel = document.createElement("div");
    instLabel.textContent = "Instrument";
    instLabel.style.fontSize = "0.85rem";
    instLabel.style.color = "#6b7280";
    instLabel.style.marginBottom = "4px";

    const instSelect = document.createElement("select");
    instSelect.style.width = "100%";
    instSelect.style.padding = "10px";
    instSelect.style.borderRadius = "8px";
    instSelect.style.border = "1px solid rgba(15, 23, 42, 0.12)";
    instSelect.style.background = "#fff";
    instSelect.style.fontSize = "0.95rem";
    instSelect.dataset.role = "instrument";

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Select instrument";
    placeholder.disabled = true;
    placeholder.selected = true;
    instSelect.appendChild(placeholder);

    instruments.forEach((inst) => {
      const opt = document.createElement("option");
      opt.value = inst.id;
      opt.textContent = inst.name;
      instSelect.appendChild(opt);
    });

    instWrapper.appendChild(instLabel);
    instWrapper.appendChild(instSelect);

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
    const minSelect = makeLevelSelect("BEGINNER", "minLevel");
    minSelect.style.width = "100%";
    minWrapper.appendChild(minSelect);

    const maxWrapper = document.createElement("div");
    maxWrapper.style.flex = "1";
    const maxLabel = document.createElement("div");
    maxLabel.textContent = "Max level";
    maxLabel.style.fontSize = "0.85rem";
    maxLabel.style.color = "#6b7280";
    maxWrapper.appendChild(maxLabel);
    const maxSelect = makeLevelSelect("EXPERT", "maxLevel");
    maxSelect.style.width = "100%";
    maxWrapper.appendChild(maxSelect);

    levelsWrapper.appendChild(minWrapper);
    levelsWrapper.appendChild(maxWrapper);

    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.textContent = "Remove";
    removeBtn.style.border = "none";
    removeBtn.style.background = "transparent";
    removeBtn.style.color = "#b91c1c";
    removeBtn.style.cursor = "pointer";
    removeBtn.style.fontSize = "0.9rem";
    removeBtn.addEventListener("click", () => row.remove());

    row.appendChild(instWrapper);
    row.appendChild(levelsWrapper);
    row.appendChild(removeBtn);

    instrumentRowsContainer.appendChild(row);
  }

  async function loadInstruments() {
    try {
      instruments = await getInstruments();
      if (instruments.length > 0) createInstrumentRow();
    } catch (err) {
      console.error(err);
      if (msg) msg.textContent = "Failed to load instruments.";
    }
  }

  function handlePhotoChange() {
    const f = fileInput.files && fileInput.files[0];
    if (!f) return;

    // Local preview only
    const reader = new FileReader();
    reader.onload = function (e) {
      document.getElementById("photoPreview").src = e.target.result;
    };
    reader.readAsDataURL(f);
  }

  function collectInstrumentSelections() {
    const selections = [];
    document.querySelectorAll(".instrument-row").forEach((row) => {
      const instSelect = row.querySelector("select[data-role='instrument']");
      const minSelect = row.querySelector("select[data-role='minLevel']");
      const maxSelect = row.querySelector("select[data-role='maxLevel']");
      if (!instSelect) return;
      const instId = instSelect.value;
      if (!instId) return;
      const minLevel = (minSelect && minSelect.value) || "BEGINNER";
      const maxLevel = (maxSelect && maxSelect.value) || "EXPERT";
      selections.push({
        instrumentId: parseInt(instId, 10),
        minLevel,
        maxLevel,
      });
    });
    return selections;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (msg) msg.textContent = "Creating profile...";

    const userId = requireUserId();
    if (!userId) return;

    const bio = document.getElementById("bio").value || null;
    const hourly = document.getElementById("hourlyRate").value;
    const hourlyRateCents =
      hourly && !isNaN(hourly)
        ? Math.round(parseFloat(hourly) * 100)
        : null;
    const onlineEnabled = document.getElementById("onlineEnabled").checked;
    const inPersonEnabled =
      document.getElementById("inPersonEnabled").checked;
    const city = document.getElementById("city").value || null;
    const state = document.getElementById("state").value || null;
    const timezone = document.getElementById("timezone").value || null;
    const cancellationNote =
      document.getElementById("cancellationNote").value || null;

    const zipcode = document.getElementById("zipcode").value || null;

    const latRaw = document.getElementById("latitude").value;
    const lonRaw = document.getElementById("longitude").value;

    // Initial payload (no photo URL yet)
    const payload = {
      photoUrl: null,
      bio,
      hourlyRateCents,
      onlineEnabled,
      inPersonEnabled,
      city,
      state,
      zipcode,
      timezone,
      cancellationNote,
      latitude: latRaw ? parseFloat(latRaw) : null,
      longitude: lonRaw ? parseFloat(lonRaw) : null,
    };

    try {
      // 1. Create Profile
      await createTutorProfile(userId, payload);

      // 2. Upload Photo (if selected)
      if (fileInput && fileInput.files && fileInput.files[0]) {
        if (msg) msg.textContent = "Uploading photo...";
        const fd = new FormData();
        fd.append("file", fileInput.files[0]);
        const upRes = await fetch(
          `/api/tutor-profiles/${encodeURIComponent(userId)}/photo`,
          { method: "POST", body: fd }
        );
        if (!upRes.ok) {
          console.warn("Photo upload failed after profile creation");
          // Continue anyway, not blocking
        }
      }

      // 3. Create Instruments
      if (msg) msg.textContent = "Saving instruments...";
      const instrumentSelections = collectInstrumentSelections();
      for (const sel of instrumentSelections) {
        await createTutorInstrument({
          tutorUserId: Number(userId),
          instrumentId: sel.instrumentId,
          minLevel: sel.minLevel,
          maxLevel: sel.maxLevel,
        });
      }

      window.location.href = "/Tutor/tutor_profile.html";
    } catch (err) {
      if (err.status === 409) {
        if (msg) msg.textContent =
          "A profile already exists for this account. Redirecting...";
        setTimeout(
          () => (window.location.href = "/Tutor/tutor_profile.html"),
          1000
        );
      } else if (err.status === 404) {
        if (msg) msg.textContent = "User not found. Please log in again.";
      } else if (err.status === 400) {
        if (msg) msg.textContent = "Bad request: " + err.message;
      } else {
        console.error(err);
        if (msg) msg.textContent = "Error creating profile.";
      }
    }
  }

  function start() {
    form = document.getElementById("onboard-form");
    msg = document.getElementById("msg");
    fileInput = document.getElementById("inputPhoto");
    instrumentRowsContainer = document.getElementById("instrumentRows");
    addInstrumentBtn = document.getElementById("addInstrumentRow");

    if (!form) return; // page mismatch

    const zipEl = document.getElementById('zipcode');
    if (zipEl) {
      // Use input event for live auto-fill as user types
      zipEl.addEventListener('input', async (e) => {
        const zip = e.target.value.trim();
        if (zip && zip.length === 5 && /^\d{5}$/.test(zip)) {
          try {
            const res = await fetch(`https://api.zippopotam.us/us/${zip}`);
            if (res.ok) {
              const data = await res.json();
              const place = data.places && data.places[0];
              if (place) {
                document.getElementById('city').value = place['place name'];
                document.getElementById('state').value = place['state abbreviation'];
                document.getElementById('latitude').value = place.latitude;
                document.getElementById('longitude').value = place.longitude;
              }
            }
          } catch (err) { console.error('Geo Error', err); }
        }
      });
    }

    if (fileInput) fileInput.addEventListener("change", handlePhotoChange);
    if (addInstrumentBtn) addInstrumentBtn.addEventListener("click", createInstrumentRow);
    form.addEventListener("submit", handleSubmit);

    loadInstruments();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }
})();
