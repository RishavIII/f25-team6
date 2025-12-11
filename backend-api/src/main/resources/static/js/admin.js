(function () {
    const tableBody = document.getElementById('userTableBody');
    const msgEl = document.getElementById('msg');

    function showMsg(text, isError = false) {
        msgEl.textContent = text;
        msgEl.style.color = isError ? '#b91c1c' : '#047857';
        setTimeout(() => msgEl.textContent = '', 4000);
    }

    async function loadUsers() {
        try {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center">Loading...</td></tr>';
            const users = await apiJson('/admin/users');
            renderTable(users);
        } catch (e) {
            console.error(e);
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:red">Error loading users</td></tr>';
        }
    }

    function renderTable(users) {
        tableBody.innerHTML = '';
        if (!users || users.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center">No users found</td></tr>';
            return;
        }

        users.sort((a, b) => a.id - b.id);

        users.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${u.id}</td>
                <td>${u.name || '-'}</td>
                <td>${u.email || '-'}</td>
                <td><span class="role-badge role-${u.role}">${u.role}</span></td>
                <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
                <td>
                    <button class="btn btn-danger btn-sm delete-btn" data-id="${u.id}">Delete</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });

        // Bind delete buttons
        document.querySelectorAll('.delete-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.dataset.id;
                if (confirm(`Are you sure you want to delete user ID ${id}?`)) {
                    await deleteUser(id);
                }
            });
        });
    }

    async function deleteUser(id) {
        try {
            const res = await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('Delete failed');
            showMsg('User deleted');
            loadUsers();
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
    document.getElementById('btnRefresh').addEventListener('click', loadUsers);

    document.getElementById('btnDeleteAll').addEventListener('click', async () => {
        if (confirm('ARE YOU SURE? This will delete ALL users and cannot be undone.')) {
            try {
                const res = await fetch('/api/admin/users', { method: 'DELETE' });
                if (!res.ok) throw new Error('Delete all failed');
                showMsg('All users deleted');
                loadUsers();
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

    // Initial load
    loadUsers();

})();
