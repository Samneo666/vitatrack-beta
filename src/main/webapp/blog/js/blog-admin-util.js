/**
 * Response Handler Utility
 * Standardizes error/success handling for unified response pattern
 */

/**
 * Handle fetch response with unified pattern
 * Success: {success: true, data: {...}}
 * Error: {success: false, errMsg: "...", errors: [...]}
 * 
 * @param {Response} response - Fetch API response
 * @param {string} defaultErrorMsg - Default error message if errMsg not provided
 * @returns {Promise} Resolves with data on success, rejects with Error on failure
 */
function handleAdminResponse(response, defaultErrorMsg = '操作失敗') {
    return response.json().then(result => {
        if (!result.success) {
            // ✅ Option B: Handle array of validation errors
            const message = result.errors 
                ? result.errors.join('\n')
                : (result.errMsg || defaultErrorMsg);
            throw new Error(message);
        }
        return result.data || result;
    });
}

/**
 * Show toast notification
 * @param {string} message - Message to display
 * @param {string} type - 'success' or 'error'
 */
function showAdminToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `admin-toast admin-toast-${type}`;
    const icon = type === 'success' ? 'ri-checkbox-circle-line' : 'ri-error-warning-line';
    toast.innerHTML = `<i class="${icon}"></i><span>${message}</span>`;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(400px)';
        setTimeout(() => toast.remove(), 300);
    }, type === 'success' ? 3000 : 5000);
}
