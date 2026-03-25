/**
 * auth.js - Handles login and registration on the auth page.
 *
 * Uses the REST API endpoints:
 * POST /api/auth/login
 * POST /api/auth/register
 *
 * On success, saves JWT token to localStorage and redirects.
 */

// ============================================================
// UTILITY FUNCTIONS
// ============================================================

/**
 * Show/hide the login or register form tab.
 */
function showTab(tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');

    if (tab === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        loginTab.classList.remove('active');
        registerTab.classList.add('active');
    }
}

/**
 * Toggle password visibility (show/hide).
 */
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    input.type = input.type === 'password' ? 'text' : 'password';
}

// ============================================================
// LOGIN
// ============================================================

/**
 * Send login request to the backend.
 * On success: save token and redirect to analyzer page.
 */
async function login() {
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    // Hide previous errors
    errorDiv.style.display = 'none';

    // Basic validation
    if (!username || !password) {
        showError(errorDiv, 'Please enter username and password.');
        return;
    }

    // Show loading state on button
    const btn = document.querySelector('#loginForm .btn-primary');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing in...';
    btn.disabled = true;

    try {
        // Call login API
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            // SUCCESS: Save user info and redirect
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('userRole', data.role);

            // Redirect based on role
            window.location.href = '/analyzer.html';
        } else {
            // Server returned error message
            showError(errorDiv, data || 'Invalid username or password.');
        }
    } catch (err) {
        showError(errorDiv, 'Connection error. Is the server running?');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// ============================================================
// REGISTER
// ============================================================

/**
 * Send registration request to the backend.
 * On success: show success message and switch to login tab.
 */
async function register() {
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const errorDiv = document.getElementById('regError');
    const successDiv = document.getElementById('regSuccess');

    errorDiv.style.display = 'none';
    successDiv.style.display = 'none';

    // Validation
    if (!username || username.length < 3) {
        showError(errorDiv, 'Username must be at least 3 characters.');
        return;
    }
    if (!email || !email.includes('@')) {
        showError(errorDiv, 'Please enter a valid email address.');
        return;
    }
    if (!password || password.length < 6) {
        showError(errorDiv, 'Password must be at least 6 characters.');
        return;
    }

    const btn = document.querySelector('#registerForm .btn-primary');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating account...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.text();

        if (response.ok) {
            // Show success, clear form, switch to login
            successDiv.innerHTML = '<i class="fas fa-check-circle"></i> ' + data +
                                   ' Switching to login...';
            successDiv.style.display = 'block';

            // Clear form fields
            document.getElementById('regUsername').value = '';
            document.getElementById('regEmail').value = '';
            document.getElementById('regPassword').value = '';

            // Auto-switch to login after 2 seconds
            setTimeout(() => showTab('login'), 2000);
        } else {
            showError(errorDiv, data || 'Registration failed. Please try again.');
        }
    } catch (err) {
        showError(errorDiv, 'Connection error. Is the server running?');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// ============================================================
// HELPER
// ============================================================

function showError(div, msg) {
    div.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
    div.style.display = 'block';
}

// ============================================================
// KEY BINDINGS
// ============================================================

// Allow pressing Enter to submit forms
document.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const loginForm = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        if (loginForm && loginForm.style.display !== 'none') {
            login();
        } else if (registerForm && registerForm.style.display !== 'none') {
            register();
        }
    }
});

// ============================================================
// INIT: Redirect if already logged in
// ============================================================
window.addEventListener('load', () => {
    const token = localStorage.getItem('authToken');
    if (token) {
        // Already logged in, go to analyzer
        window.location.href = '/analyzer.html';
    }
});
