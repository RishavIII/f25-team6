(function () {
    const tableBody = document.getElementById('userTableBody');
    const msgEl = document.getElementById('msg');

    function showMsg(text, isError = false) {
        msgEl.textContent = text;
        msgEl.style.color = isError ? '#b91c1c' : '#047857';
        setTimeout(() => msgEl.textContent = '', 4000);
    }

    const msgFrom = document.getElementById('msgFrom');
    const msgTo = document.getElementById('msgTo');
    const msgBody = document.getElementById('msgBody');
    const bookStudent = document.getElementById('bookStudent');
    const bookTutor = document.getElementById('bookTutor');
    const bookInstrument = document.getElementById('bookInstrument');
    const bookDate = document.getElementById('bookDate');
    const bookTime = document.getElementById('bookTime');
    const pendingBookingBody = document.getElementById('pendingBookingBody');
    const reviewStudent = document.getElementById('reviewStudent');
    const reviewTutor = document.getElementById('reviewTutor');
    const reviewLesson = document.getElementById('reviewLesson');
    const reviewRating = document.getElementById('reviewRating');
    const reviewText = document.getElementById('reviewText');

    async function loadUsers() {
        try {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center">Loading...</td></tr>';
            const users = await apiJson('/admin/users');
            renderTable(users);
            populateSelectors(users);
        } catch (e) {
            console.error(e);
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:red">Error loading users</td></tr>';
        }
    }

    async function loadInstruments() {
        try {
            const instruments = await apiJson('/instruments');
            bookInstrument.innerHTML = '';
            instruments.sort((a, b) => a.name.localeCompare(b.name));
            instruments.forEach(inst => {
                const opt = document.createElement('option');
                opt.value = inst.id;
                opt.textContent = inst.name;
                bookInstrument.appendChild(opt);
            });
        } catch (e) {
            console.error('Failed to load instruments', e);
            // Fallback to empty
            bookInstrument.innerHTML = '<option value="1">Unknown</option>';
        }
    }

    function setDefaultBookingDate() {
        // Default to tomorrow at 2pm
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(14, 0, 0, 0);
        // Format for date input: YYYY-MM-DD
        const year = tomorrow.getFullYear();
        const month = String(tomorrow.getMonth() + 1).padStart(2, '0');
        const day = String(tomorrow.getDate()).padStart(2, '0');
        bookDate.value = `${year}-${month}-${day}`;
        // Format for time input: HH:mm
        bookTime.value = '14:00';
    }

    function populateSelectors(users) {
        // Clear options
        msgFrom.innerHTML = ''; msgTo.innerHTML = '';
        bookStudent.innerHTML = ''; bookTutor.innerHTML = '';
        reviewStudent.innerHTML = ''; reviewTutor.innerHTML = '';

        users.sort((a, b) => a.name.localeCompare(b.name));

        users.forEach(u => {
            const opt = document.createElement('option');
            opt.value = u.id;
            opt.textContent = `${u.name} (${u.role})`;

            msgFrom.appendChild(opt.cloneNode(true));
            msgTo.appendChild(opt.cloneNode(true));

            if (u.role === 'STUDENT') {
                bookStudent.appendChild(opt.cloneNode(true));
                reviewStudent.appendChild(opt.cloneNode(true));
            }
            if (u.role === 'TUTOR') {
                bookTutor.appendChild(opt.cloneNode(true));
                reviewTutor.appendChild(opt.cloneNode(true));
            }
        });
    }

    function renderTable(users) {
        tableBody.innerHTML = '';
        if (!users || users.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center">No users found</td></tr>';
            return;
        }

        const sorted = [...users].sort((a, b) => a.id - b.id);

        sorted.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${u.id}</td>
                <td>${u.name || '-'}</td>
                <td>${u.email || '-'}</td>
                <td><span class="role-badge role-${u.role}">${u.role}</span></td>
                <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
                <td style="display:flex; gap:8px;">
                    <button class="btn btn-sm login-as-btn" data-id="${u.id}" style="background:#e0f2fe; color:#0369a1;">Login as</button>
                    <button class="btn btn-danger btn-sm delete-btn" data-id="${u.id}">Delete</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        // Bind buttons
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.dataset.id;
                if (confirm(`Delete user ID ${id}?`)) await deleteUser(id);
            });
        });

        document.querySelectorAll('.login-as-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.dataset.id;
                impersonateUser(id);
            });
        });
    }

    async function impersonateUser(id) {
        document.cookie = `duet_user_id=${id}; path=/; max-age=86400`;
        // Also update localStorage so frontend apps (which use requireUserId in common.js) pick it up immediately
        localStorage.setItem('userId', id);
        // We might want to set role too, but userId is the critical one for loading profiles.
        // To do this right, we should probably grab the user from the list to get their role.
        const row = document.querySelector(`.login-as-btn[data-id="${id}"]`).closest('tr');
        if (row) {
            const roleBadge = row.querySelector('.role-badge');
            if (roleBadge) {
                const role = roleBadge.textContent.trim();
                localStorage.setItem('userRole', role);
                if (role === 'STUDENT') {
                    window.location.href = '/Student/student-home.html';
                    return;
                }
            }
        }
        window.location.href = '/Tutor/tutor_home.html';
    }

    async function sendMessages() {
        const fromIds = Array.from(msgFrom.selectedOptions).map(o => parseInt(o.value));
        const toIds = Array.from(msgTo.selectedOptions).map(o => parseInt(o.value));
        const body = msgBody.value.trim();

        if (fromIds.length === 0 || toIds.length === 0 || !body) {
            alert("Please select at least one sender, one receiver, and message text.");
            return;
        }

        try {
            const res = await fetch('/api/admin/messages', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fromUserIds: fromIds, toUserIds: toIds, body })
            });
            if (!res.ok) throw new Error("Failed to send");
            showMsg("Messages queued successfully");
            msgBody.value = '';
        } catch (e) {
            console.error(e);
            showMsg("Error sending messages", true);
        }
    }

    async function createBooking() {
        const sId = bookStudent.value;
        const tId = bookTutor.value;
        const iId = bookInstrument.value;
        const dateVal = bookDate.value;
        const timeVal = bookTime.value;

        if (!sId || !tId) { alert("Select a student and tutor"); return; }
        if (!iId) { alert("Select an instrument"); return; }
        if (!dateVal) { alert("Select a lesson date"); return; }
        if (!timeVal) { alert("Select a lesson time"); return; }

        // Combine date and time into ISO string for the API
        const requestedStartUtc = new Date(`${dateVal}T${timeVal}`).toISOString();

        try {
            const res = await fetch('/api/admin/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    studentId: parseInt(sId),
                    tutorId: parseInt(tId),
                    instrumentId: parseInt(iId),
                    level: "BEGINNER",
                    durationMin: 60,
                    requestedStartUtc: requestedStartUtc
                })
            });
            if (!res.ok) throw new Error("Failed");
            showMsg("Booking request created");
            loadPendingBookings(); // refresh actions
        } catch (e) {
            console.error(e);
            showMsg("Error creating booking", true);
        }
    }

    async function loadPendingBookings() {
        pendingBookingBody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
        try {
            const list = await apiJson('/booking-requests'); // apiJson already prepends /api
            // Filter for pending
            const pending = (list || []).filter(b => b.status === "PENDING");

            pendingBookingBody.innerHTML = '';
            if (pending.length === 0) {
                pendingBookingBody.innerHTML = '<tr><td colspan="6" style="color:#666; text-align:center">No pending bookings</td></tr>';
                return;
            }

            pending.forEach(b => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                 <td>${b.id}</td>
                 <td>${b.student ? b.student.name : '-'}</td>
                 <td>${b.tutor ? b.tutor.name : '-'}</td>
                 <td>${b.instrument ? b.instrument.name : '-'}</td>
                 <td><span class="role-badge" style="background:#fef3c7; color:#d97706">${b.status}</span></td>
                 <td style="display:flex; gap:8px;">
                    <button class="btn btn-sm primary accept-btn" data-id="${b.id}">Accept</button>
                    <button class="btn btn-sm btn-danger decline-btn" data-id="${b.id}">Decline</button>
                 </td>
               `;
                pendingBookingBody.appendChild(tr);
            });

            // Bind
            document.querySelectorAll('.accept-btn').forEach(b => b.addEventListener('click', (e) => respondBooking(e.target.dataset.id, 'accept')));
            document.querySelectorAll('.decline-btn').forEach(b => b.addEventListener('click', (e) => respondBooking(e.target.dataset.id, 'decline')));

        } catch (e) {
            console.error(e);
            pendingBookingBody.innerHTML = '<tr><td colspan="6" style="color:red">Error loading bookings</td></tr>';
        }
    }

    async function respondBooking(id, action) {
        try {
            const res = await fetch(`/api/booking-requests/${id}/${action}`, { method: 'POST' });
            if (!res.ok) throw new Error("Action failed");
            showMsg(`Booking ${action}ed`);
            loadPendingBookings();
        } catch (e) {
            showMsg(`Error ${action}ing booking`, true);
        }
    }

    async function deleteUser(id) {
        try {
            const res = await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Delete failed');
            showMsg('User deleted');
            loadUsers();
            loadPendingBookings();
        } catch (e) {
            showMsg('Error deleting user', true);
        }
    }

    async function generateUsers(type, count) {
        try {
            const btn = type === 'students' ? document.getElementById('btnGenStudents') : document.getElementById('btnGenTutors');
            const originalText = btn.textContent;
            btn.textContent = 'Generating...';
            btn.disabled = true;

            await fetch(`/api/admin/generate/${type}?count=${count}`, { method: 'POST' });

            showMsg(`Generated ${count} ${type}`);
            // Use existing loadUsers to refresh table and selectors
            await loadUsers();

            btn.textContent = originalText;
            btn.disabled = false;
        } catch (e) {
            console.error(e);
            showMsg(`Error generating ${type}`, true);
            if (type === 'students') document.getElementById('btnGenStudents').disabled = false;
            else document.getElementById('btnGenTutors').disabled = false;
        }
    }

    // Bind Controls
    document.getElementById('btnRefresh').addEventListener('click', () => { loadUsers(); loadPendingBookings(); });

    document.getElementById('btnDeleteAll').addEventListener('click', async () => {
        if (confirm('ARE YOU SURE? This will delete ALL users and cannot be undone.')) {
            try {
                const res = await fetch('/api/admin/users', { method: 'DELETE' });
                if (!res.ok) throw new Error('Delete all failed');
                showMsg('All users deleted');
                loadUsers();
                loadPendingBookings();
            } catch (e) {
                showMsg('Error deleting all users', true);
            }
        }
    });

    document.getElementById('btnGenStudents').addEventListener('click', () => {
        const count = document.getElementById('genStudentCount').value;
        if (count > 0) generateUsers('students', count);
    });

    document.getElementById('btnGenTutors').addEventListener('click', () => {
        const count = document.getElementById('genTutorCount').value;
        if (count > 0) generateUsers('tutors', count);
    });

    document.getElementById('btnSendMsg').addEventListener('click', sendMessages);
    document.getElementById('btnCreateBooking').addEventListener('click', createBooking);

    async function loadCompletedLessons() {
        const sId = reviewStudent.value;
        const tId = reviewTutor.value;
        reviewLesson.innerHTML = '<option value="">-- Select student & tutor first --</option>';

        if (!sId || !tId) return;

        try {
            const lessons = await apiJson(`/admin/lessons?studentId=${sId}&tutorId=${tId}`);
            if (!lessons || lessons.length === 0) {
                reviewLesson.innerHTML = '<option value="">-- No completed lessons found --</option>';
                return;
            }
            reviewLesson.innerHTML = '';
            lessons.forEach(l => {
                const opt = document.createElement('option');
                opt.value = l.id;
                const date = l.startUtc ? new Date(l.startUtc).toLocaleString() : 'Unknown date';
                opt.textContent = `${l.instrumentName} - ${date}`;
                reviewLesson.appendChild(opt);
            });
        } catch (e) {
            console.error('Failed to load lessons', e);
            reviewLesson.innerHTML = '<option value="">-- Error loading lessons --</option>';
        }
    }

    reviewStudent.addEventListener('change', loadCompletedLessons);
    reviewTutor.addEventListener('change', loadCompletedLessons);

    async function createReview() {
        const sId = reviewStudent.value;
        const tId = reviewTutor.value;
        const lessonId = reviewLesson.value;
        const rating = parseInt(reviewRating.value);
        const text = reviewText.value.trim();

        if (!sId || !tId) { alert("Select a student and tutor"); return; }
        if (!lessonId) { alert("Select a completed lesson"); return; }
        if (!rating || rating < 1 || rating > 5) { alert("Select a valid rating"); return; }

        try {
            const res = await fetch('/api/admin/reviews', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    studentId: parseInt(sId),
                    tutorId: parseInt(tId),
                    lessonId: parseInt(lessonId),
                    rating: rating,
                    text: text || null
                })
            });
            if (!res.ok) throw new Error("Failed");
            showMsg("Review created successfully");
            reviewText.value = '';
            // Reload lessons to remove the one that was just reviewed
            loadCompletedLessons();
        } catch (e) {
            console.error(e);
            showMsg("Error creating review", true);
        }
    }

    document.getElementById('btnCreateReview').addEventListener('click', createReview);

    // --- Location Search Feature (3rd Party API Demo) ---
    const searchZipInput = document.getElementById('searchZip');
    const searchRadiusInput = document.getElementById('searchRadius');
    const locationSearchResults = document.getElementById('locationSearchResults');

    async function geocodeZip(zip) {
        const res = await fetch(`https://api.zippopotam.us/us/${encodeURIComponent(zip)}`);
        if (!res.ok) throw new Error('Invalid zipcode');
        const data = await res.json();
        const place = data.places && data.places[0];
        return {
            city: place['place name'],
            state: place['state abbreviation'],
            lat: parseFloat(place.latitude),
            lon: parseFloat(place.longitude)
        };
    }

    function toRad(deg) { return deg * Math.PI / 180; }

    function haversine(lat1, lon1, lat2, lon2) {
        const R = 3958.8; // Earth radius in miles
        const dLat = toRad(lat2 - lat1);
        const dLon = toRad(lon2 - lon1);
        const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    async function searchByLocation() {
        const zip = (searchZipInput.value || '').trim();
        const radius = parseFloat(searchRadiusInput.value) || 25;

        if (!zip) {
            alert('Please enter a zipcode');
            return;
        }

        locationSearchResults.innerHTML = '<p style="color:#666;">Searching...</p>';

        try {
            // Step 1: Call Zippopotamus API to geocode the zipcode
            const geoData = await geocodeZip(zip);

            // Step 2: Get all tutor profiles via the search endpoint
            const tutors = await apiJson('/tutors/search');

            // Step 3: Calculate distance for each tutor and filter by radius
            const tutorsWithDistance = tutors
                .filter(t => t.latitude != null && t.longitude != null)
                .map(t => ({
                    ...t,
                    distance: haversine(geoData.lat, geoData.lon, t.latitude, t.longitude)
                }))
                .filter(t => t.distance <= radius)
                .sort((a, b) => a.distance - b.distance);

            // Step 4: Render results
            if (tutorsWithDistance.length === 0) {
                locationSearchResults.innerHTML = `
                    <div style="padding: 12px; background: #fef3c7; border-radius: 6px; color: #b45309;">
                        <strong>No tutors found within ${radius} miles of ${geoData.city}, ${geoData.state} (${zip})</strong>
                        <p style="margin-top: 4px; font-size: 0.9rem;">
                            API Response: Lat ${geoData.lat.toFixed(4)}, Lon ${geoData.lon.toFixed(4)}
                        </p>
                    </div>
                `;
                return;
            }

            let html = `
                <div style="padding: 12px; background: #d1fae5; border-radius: 6px; color: #065f46; margin-bottom: 12px;">
                    <strong>Found ${tutorsWithDistance.length} tutor(s) within ${radius} miles of ${geoData.city}, ${geoData.state} (${zip})</strong>
                    <p style="margin-top: 4px; font-size: 0.9rem;">
                        Zippopotamus API Response: Lat ${geoData.lat.toFixed(4)}, Lon ${geoData.lon.toFixed(4)}
                    </p>
                </div>
                <table style="width: 100%;">
                    <thead>
                        <tr>
                            <th>Tutor</th>
                            <th>Location</th>
                            <th>Zipcode</th>
                            <th>Distance</th>
                            <th>Hourly Rate</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            tutorsWithDistance.forEach(t => {
                const name = t.user ? t.user.name : `Tutor #${t.userId}`;
                const location = [t.city, t.state].filter(Boolean).join(', ') || '-';
                const rate = t.hourlyRateCents ? `$${(t.hourlyRateCents / 100).toFixed(2)}/hr` : '-';
                html += `
                    <tr>
                        <td>${name}</td>
                        <td>${location}</td>
                        <td>${t.zipcode || '-'}</td>
                        <td><strong>${t.distance.toFixed(1)} mi</strong></td>
                        <td>${rate}</td>
                    </tr>
                `;
            });

            html += '</tbody></table>';
            locationSearchResults.innerHTML = html;

        } catch (e) {
            console.error(e);
            locationSearchResults.innerHTML = `
                <div style="padding: 12px; background: #fee2e2; border-radius: 6px; color: #991b1b;">
                    <strong>Error:</strong> ${e.message || 'Failed to search'}
                    <p style="margin-top: 4px; font-size: 0.9rem;">
                        Check the console for details. The Zippopotamus API may have rejected the zipcode.
                    </p>
                </div>
            `;
        }
    }

    document.getElementById('btnSearchLocation').addEventListener('click', searchByLocation);

    // Initial load
    loadUsers();
    loadInstruments();
    loadPendingBookings();
    setDefaultBookingDate();

})();
