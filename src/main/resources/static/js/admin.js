/**
 * admin.js - Handles the Admin Dashboard page.
 * Only accessible by users with ADMIN role.
 */

window.addEventListener('load', () => {
    const token = localStorage.getItem('authToken');
    if (!token) { window.location.href = '/login.html'; return; }

    const role = localStorage.getItem('userRole');
    if (role !== 'ADMIN') {
        alert('Access denied! Admin only.');
        window.location.href = '/analyzer.html';
        return;
    }

    document.getElementById('navUsername').textContent = localStorage.getItem('username');

    // Load all data
    loadAnalytics();
    loadUsers();
    loadSubmissions();
});

/**
 * Load analytics summary cards.
 */
async function loadAnalytics() {
    const token = localStorage.getItem('authToken');

    try {
        const response = await fetch('/api/admin/analytics', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (response.status === 401) { logout(); return; }
        if (response.status === 403) {
            alert('Access denied!');
            window.location.href = '/analyzer.html';
            return;
        }

        const data = await response.json();

        // Update stat cards
        document.getElementById('statUsers').textContent = data.totalUsers;
        document.getElementById('statSubmissions').textContent = data.totalSubmissions;
        document.getElementById('statSuccess').textContent = data.successfulExecutions;
        document.getElementById('statFailed').textContent = data.failedExecutions;

        // Update language bars
        const total = data.totalSubmissions || 1;
        document.getElementById('javaCount').textContent = data.javaSubmissions;
        document.getElementById('pythonCount').textContent = data.pythonSubmissions;
        document.getElementById('cCount').textContent = data.cSubmissions;

        // Animate bars
        setTimeout(() => {
            document.getElementById('javaBar').style.width = ((data.javaSubmissions / total) * 100) + '%';
            document.getElementById('pythonBar').style.width = ((data.pythonSubmissions / total) * 100) + '%';
            document.getElementById('cBar').style.width = ((data.cSubmissions / total) * 100) + '%';
        }, 100);

    } catch (err) {
        console.error('Analytics load error:', err);
    }
}

/**
 * Load all users table.
 */
async function loadUsers() {
    const token = localStorage.getItem('authToken');

    try {
        const response = await fetch('/api/admin/users', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const users = await response.json();
        const tbody = document.getElementById('usersTableBody');

        if (!users || users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center">No users found.</td></tr>';
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escapeHtml(u.username)}</strong></td>
                <td>${escapeHtml(u.email)}</td>
                <td>
                    <span class="badge ${u.role === 'ADMIN' ? 'badge-admin' : 'badge-success'}">
                        ${u.role === 'ADMIN' ? '👑' : '👤'} ${u.role}
                    </span>
                </td>
                <td>
                    <span class="badge ${u.active ? 'badge-success' : 'badge-error'}">
                        ${u.active ? 'Active' : 'Disabled'}
                    </span>
                </td>
                <td>${u.submissionCount}</td>
                <td>${formatDate(u.createdAt)}</td>
                <td>
                    <button class="btn btn-sm ${u.active ? 'btn-danger' : 'btn-success'}"
                            onclick="toggleUser(${u.id}, this)">
                        <i class="fas fa-${u.active ? 'ban' : 'check'}"></i>
                        ${u.active ? 'Disable' : 'Enable'}
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        document.getElementById('usersTableBody').innerHTML =
            `<tr><td colspan="8" class="text-center" style="color:red">Error: ${err.message}</td></tr>`;
    }
}

/**
 * Load all submissions table.
 */
async function loadSubmissions() {
    const token = localStorage.getItem('authToken');

    try {
        const response = await fetch('/api/admin/submissions', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const subs = await response.json();
        const tbody = document.getElementById('submissionsTableBody');

        if (!subs || subs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center">No submissions yet.</td></tr>';
            return;
        }

        tbody.innerHTML = subs.map(s => `
            <tr>
                <td>${s.id}</td>
                <td><strong>${s.user ? escapeHtml(s.user.username) : 'N/A'}</strong></td>
                <td><span class="lang-tag ${s.language}">${getLangEmoji(s.language)} ${s.language}</span></td>
                <td>
                    ${s.executionSuccess
                        ? '<span class="badge badge-success"><i class="fas fa-check"></i> OK</span>'
                        : '<span class="badge badge-error"><i class="fas fa-times"></i> Error</span>'}
                </td>
                <td>${s.lineCount}</td>
                <td>${s.cyclomaticComplexity}</td>
                <td>${formatDate(s.submittedAt)}</td>
                <td><code class="code-preview">${escapeHtml(s.code ? s.code.substring(0,60)+'...' : '')}</code></td>
            </tr>
        `).join('');

    } catch (err) {
        document.getElementById('submissionsTableBody').innerHTML =
            `<tr><td colspan="8" class="text-center" style="color:red">Error: ${err.message}</td></tr>`;
    }
}

/**
 * Toggle user active/inactive status.
 */
async function toggleUser(userId, btn) {
    const token = localStorage.getItem('authToken');

    if (!confirm('Are you sure you want to change this user\'s status?')) return;

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

    try {
        const response = await fetch(`/api/admin/users/${userId}/toggle`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const message = await response.text();
        alert(message);

        // Reload users table to reflect change
        loadUsers();
        loadAnalytics();

    } catch (err) {
        alert('Error: ' + err.message);
        btn.disabled = false;
    }
}

/**
 * Switch between Users and Submissions tabs.
 */
function showAdminTab(tab) {
    document.querySelectorAll('.admin-tab-content').forEach(el => el.style.display = 'none');
    document.querySelectorAll('.admin-tabs .tab').forEach(el => el.classList.remove('active'));

    document.getElementById('adminTab-' + tab).style.display = 'block';
    event.target.classList.add('active');
}

// ============================================================
// HELPERS
// ============================================================

function getLangEmoji(lang) {
    const map = { Java: '☕', Python: '🐍', C: '⚙️' };
    return map[lang] || '💻';
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
