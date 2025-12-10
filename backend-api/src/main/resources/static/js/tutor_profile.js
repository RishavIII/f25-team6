(function () {
  const msg = document.getElementById("msg");

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
    msg.textContent = "";

    try {
      const profile = await getTutorProfile(uid);
      const tutorInstruments = await loadTutorInstrumentsForUser(uid);
      populateView(profile, tutorInstruments);
    } catch (err) {
      if (err.status === 404) {
        msg.textContent =
          "No tutor profile found for this account. Redirecting to onboarding...";
        setTimeout(() => {
          window.location.href = "/Tutor/tutor_onboard.html";
        }, 1200);
      } else {
        console.error(err);
        msg.textContent = "Error loading profile.";
      }
    }
  }

  loadProfile();
})();
