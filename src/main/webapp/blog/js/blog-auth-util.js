// blog-auth.js - Shared authentication utilities

const AUTH_API_BASE = '/api';

// Check authentication status
window.checkAuth = function checkAuth() {
    return fetch(`${AUTH_API_BASE}/token/status`, { credentials: 'include' })
        .then(response => response.json())
        .then(result => {
            if (!result.success) return null;
            return result.data && result.data.authenticated ? result.data : null;
        })
        .catch(() => null);
}

// Redirect to login if not authenticated
window.requireAuth = function requireAuth() {
    return checkAuth().then(user => {
        if (!user) {
            const returnUrl = window.location.pathname;
            window.location.href = `/api/oauth/google/login?returnUrl=${encodeURIComponent(returnUrl)}`;
            return null;
        }
        return user;
    });
}

// Logout
function logout() {
    fetch(`${AUTH_API_BASE}/token/logout`, { method: 'POST', credentials: 'include' })
        .then(() => window.location.href = 'blog-list.html')
        .catch(() => window.location.href = 'blog-list.html');
}

// Authenticated fetch with auto-refresh on 401
function fetchWithAuth(url, options = {}) {
    const config = {
        ...options,
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    return fetch(url, config).then(response => {
        if (response.status === 401) {
            // Try refresh
            return fetch(`${AUTH_API_BASE}/token/refresh`, { method: 'POST', credentials: 'include' })
                .then(refreshResponse => {
                    if (refreshResponse.ok) {
                        // Retry original request
                        return fetch(url, config);
                    }
                    // Refresh failed, redirect to login
                    window.location.href = 'blog-list.html';
                    return null;
                });
        }
        return response;
    });
}

// Prevent back-button showing cached authenticated pages after logout
window.addEventListener('pageshow', function(event) {
    if (event.persisted && !window.location.pathname.includes('login')) {
        checkAuth().then(user => {
            if (!user) window.location.href = 'blog-login.html';
        });
    }
});
