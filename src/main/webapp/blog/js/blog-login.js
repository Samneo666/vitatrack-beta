// blog-login.js - Login page logic

document.addEventListener('DOMContentLoaded', function() {
    // Show error message if redirected with error
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    if (error) {
        const msg = error === 'invalid_state' ? '登入逾時，請重新登入' : '登入失敗，請重新嘗試';
        showToast(msg, { type: 'error', position: 'top', duration: 5000 });
        history.replaceState(null, '', 'blog-login.html');
    }
});
