// blog-reader-util.js - Shared utilities for public blog pages

const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
const API_BASE = `${window.location.origin}${contextPath}/api`;

const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI4MDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyMDAiIGhlaWdodD0iODAwIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI0NSUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSI0OCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+PGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSI2NCI+8J+TtzwvZm9udC1mYW1pbHk+PC90ZXh0Pjx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LWZhbWlseT0iQXJpYWwsIHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjQiIGZpbGw9IiNiYmJiYmIiIHRleHQtYW5jaG9yPSJtaWRkbGUiPuWclueJh+i8ieWFpeWksei0pTwvdGV4dD48L3N2Zz4=';

const el = id => document.getElementById(id);

const toggle = (elem, show) => elem && (elem.style.display = show ? 'block' : 'none');

const escapeHtml = str => (str || '').replace(/[&<>"']/g, m => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[m]);

const formatDate = date => new Date(date).toLocaleDateString('zh-TW', { 
    year: 'numeric', month: 'long', day: 'numeric' 
});

const showToast = (message, options = {}) => {
    const {
        type = 'info',        // 'success', 'error', 'info'
        duration = 3000,
        position = 'bottom'   // 'bottom' or 'top'
    } = options;
    
    const colors = {
        success: { bg: 'var(--primary)', color: 'var(--title-color)' },
        error: { bg: '#f90c4c', color: '#fff' },
        info: { bg: 'var(--primary)', color: 'var(--title-color)' }
    };
    
    const icons = {
        success: '<i class="ri-check-line" style="font-size: 20px;"></i>',
        error: '<i class="ri-error-warning-line" style="font-size: 20px;"></i>',
        info: ''
    };
    
    const style = colors[type];
    const icon = icons[type];
    
    const toastHtml = `
        <div class="toast align-items-center border-0" role="alert" 
             style="background-color: ${style.bg}; color: ${style.color};">
            <div class="d-flex align-items-center">
                ${icon ? `<div class="ps-3">${icon}</div>` : ''}
                <div class="toast-body">${message}</div>
            </div>
        </div>`;
    
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        const positionClass = position === 'top' 
            ? 'top-0 end-0 p-3' 
            : 'bottom-0 start-50 translate-middle-x';
        container.className = `toast-container position-fixed ${positionClass}`;
        if (position === 'bottom') {
            container.style.marginBottom = '30px';
        }
        document.body.appendChild(container);
    }
    
    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastEl = container.lastElementChild;
    const toast = new bootstrap.Toast(toastEl, { delay: duration });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
};

const showNotFoundError = (containerId, message, buttonText, buttonHref) => {
    const container = el(containerId);
    container.style.display = 'block';
    container.innerHTML = `
        <div class="col-12">
            <div class="error-display">
                <i class="ri-file-unknow-line error-icon"></i>
                <h3 class="mn-title error-title">找不到文章</h3>
                <p class="error-message">${message}</p>
                <a href="${buttonHref}" class="mn-btn-2 error-button"><span>${buttonText}</span></a>
            </div>
        </div>
    `;
};

const showLoadError = (containerId, message = '請檢查網路再試一次', buttonText = '重新載入', retryAction = 'location.reload()') => {
    const container = el(containerId);
    container.style.display = 'block';
    container.innerHTML = `
        <div class="col-12">
            <div class="error-display">
                <i class="ri-wifi-off-line error-icon"></i>
                <h3 class="mn-title error-title">無法載入文章</h3>
                <p class="error-message">${message}</p>
                <a href="javascript:${retryAction}" class="mn-btn-2 error-button"><span>${buttonText}</span></a>
            </div>
        </div>
    `;
};
