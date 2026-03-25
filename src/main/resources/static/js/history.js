/**
 * history.js - Handles the submission history page.
 * Loads and displays user's past code submissions.
 */

window.addEventListener('load', () => {
    const token = localStorage.getItem('authToken');
    if (!token) { window.location.href = '/login.html'; return; }

    // Set username
    const username = localStorage.getItem('username');
    document.getElementById('navUsername').innerHTML =
        `<i class="fas fa-user-circle"></i> ${username}`;

    if (localStorage.getItem('userRole') === 'ADMIN') {
        document.getElementById('adminLink').style.display = 'flex';
    }

    loadHistory();
});

/**
 * Load submission history from the API.
 */
async function loadHistory() {
    const token = localStorage.getItem('authToken');

    try {
        const response = await fetch('/api/code/history', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (response.status === 401) { logout(); return; }

        const data = await response.json();

        document.getElementById('historyLoading').style.display = 'none';

        if (!data || data.length === 0) {
            document.getElementById('historyEmpty').style.display = 'flex';
            return;
        }

        document.getElementById('historyTable').style.display = 'block';
        renderTable(data);

    } catch (err) {
        document.getElementById('historyLoading').innerHTML =
            `<p style="color:red">Error loading history: ${err.message}</p>`;
    }
}

/**
 * Render the history table rows.
 */
function renderTable(submissions) {
    const tbody = document.getElementById('historyTableBody');

    tbody.innerHTML = submissions.map((sub, index) => `
        <tr>
            <td>${index + 1}</td>
            <td><span class="lang-tag ${sub.language}">${getLangEmoji(sub.language)} ${sub.language}</span></td>
            <td><code class="code-preview">${escapeHtml(sub.codePreview)}</code></td>
            <td>
                ${sub.executionSuccess
                    ? '<span class="badge badge-success"><i class="fas fa-check"></i> Success</span>'
                    : '<span class="badge badge-error"><i class="fas fa-times"></i> Error</span>'}
            </td>
            <td>${sub.lineCount}</td>
            <td>${formatDate(sub.submittedAt)}</td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="viewSubmission(${sub.id})">
                    <i class="fas fa-eye"></i> View
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Load and display full details of a submission in a modal.
 */
async function viewSubmission(id) {
    const token = localStorage.getItem('authToken');

    try {
        const response = await fetch(`/api/code/submission/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const sub = await response.json();

        document.getElementById('modalBody').innerHTML = `
            <div style="display:flex;gap:10px;margin-bottom:16px;flex-wrap:wrap">
                <span class="lang-tag ${sub.language}">${getLangEmoji(sub.language)} ${sub.language}</span>
                ${sub.executionSuccess
                    ? '<span class="badge badge-success"><i class="fas fa-check"></i> Success</span>'
                    : '<span class="badge badge-error"><i class="fas fa-times"></i> Error</span>'}
                <span style="color:var(--text-muted);font-size:13px">
                    <i class="fas fa-clock"></i> ${formatDate(sub.submittedAt)}
                </span>
            </div>

            <h4 style="margin-bottom:8px">Code:</h4>
            <pre style="background:#1E1E2E;color:#D4D4D4;padding:14px;border-radius:8px;font-size:13px;overflow:auto;max-height:200px">${escapeHtml(sub.code)}</pre>

            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px;margin:16px 0">
                <div class="metric-card"><div class="metric-icon"><i class="fas fa-list-ol"></i></div><div class="metric-value">${sub.lineCount}</div><div class="metric-label">Lines</div></div>
                <div class="metric-card"><div class="metric-icon"><i class="fas fa-brain"></i></div><div class="metric-value">${sub.cyclomaticComplexity}</div><div class="metric-label">Complexity</div></div>
                <div class="metric-card"><div class="metric-icon"><i class="fas fa-puzzle-piece"></i></div><div class="metric-value">${sub.methodCount}</div><div class="metric-label">Methods</div></div>
            </div>

            ${sub.output ? `<h4 style="margin-bottom:8px">Output:</h4>
            <pre style="background:#0D1117;color:#85E89D;padding:12px;border-radius:8px;font-size:13px;overflow:auto;max-height:150px">${escapeHtml(sub.output)}</pre>` : ''}

            ${sub.errorOutput ? `<h4 style="margin-bottom:8px;color:var(--error)">Errors:</h4>
            <pre style="background:#1A0505;color:#FF8080;padding:12px;border-radius:8px;font-size:13px;overflow:auto;max-height:150px">${escapeHtml(sub.errorOutput)}</pre>` : ''}

            <h4 style="margin:16px 0 8px">Explanation:</h4>
            <pre style="background:var(--gray-light);padding:14px;border-radius:8px;font-size:13px;overflow:auto;max-height:200px;white-space:pre-wrap">${sub.beginnerExplanation || 'N/A'}</pre>
        `;

        document.getElementById('submissionModal').style.display = 'flex';

    } catch (err) {
        alert('Error loading submission: ' + err.message);
    }
}

function closeModal() {
    document.getElementById('submissionModal').style.display = 'none';
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
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
