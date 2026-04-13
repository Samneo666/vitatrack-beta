const API_BASE = 'http://localhost:8080/api';

const el = id => document.getElementById(id);
const formatDate = date => new Date(date).toLocaleDateString('zh-TW', { year: 'numeric', month: 'long', day: 'numeric' });

let currentPage = 0;
const pageSize = 10;
let totalPages = 0;
let currentUser = null;

function loadArticles(page = 0) {
    fetchWithAuth(`${API_BASE}/admin/articles?page=${page}&size=${pageSize}`)
        .then(response => handleAdminResponse(response, 'Failed to fetch articles'))
        .then(data => {
            currentPage = data.page;
            totalPages = data.totalPages;
            renderArticleTable(data.articles || []);
            renderPagination(data);
        })
        .catch(error => {
            console.error('[Load] error:', error);
            showAdminToast('文章載入失敗', 'error');
        });
}

function renderArticleTable(articles) {
    const tbody = el('articles-table-body');
    tbody.innerHTML = articles.map(article => `
        <tr class="pro-gl-content">
            <td class="d-none d-md-table-cell admin-table-cell-left">
                <span>${article.titleSlug}</span>
            </td>
            <td data-label="Product" class="mn-cart-pro-name admin-table-cell-left">
                <a href="blog-detail.html?slug=${article.titleSlug}" class="mn-item">
                    ${article.titleDisplay}
                </a>
            </td>
            <td class="d-none d-md-table-cell admin-table-cell-left">
                <span>${formatDate(article.createdAt)}</span>
            </td>
            <td class="d-none d-md-table-cell admin-table-cell-views">
                <span>${article.totalViews || 0}</span>
            </td>
            <td class="d-none d-lg-table-cell admin-table-cell-left">
                <span>${article.totalLikes || 0}</span>
            </td>
            <td class="d-none d-lg-table-cell admin-table-cell-left">
                <span>${article.totalShares || 0}</span>
            </td>
            <td class="admin-table-cell-left">
                <span class="tbl-btn admin-table-actions">
                    <a class="mn-btn-2 add-to-cart mn-add-cart" href="javascript:void(0)" 
                       onclick="editArticle('${article.id}')" title="編輯">
                        <span><i class="ri-edit-line"></i></span>
                    </a>
                    <a class="mn-btn-1 mn-remove-wish btn" href="javascript:void(0)" 
                       onclick="showDeleteModal('${article.id}', '${escapeHtml(article.titleDisplay)}')" title="刪除">
                        <span><i class="ri-close-line"></i></span>
                    </a>
                </span>
            </td>
        </tr>
    `).join('');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

let deleteModalArticleId = null;

// Remove focus before modal closes to prevent accessibility warning
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.addEventListener('hide.bs.modal', () => {
            document.activeElement?.blur();
        });
    }
});

function showDeleteModal(id, title) {
    deleteModalArticleId = id;
    document.getElementById('delete-article-title').textContent = title;
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

function confirmDelete() {
    if (!deleteModalArticleId) return;
    
    fetchWithAuth(`${API_BASE}/admin/articles/${deleteModalArticleId}`, { 
        method: 'DELETE'
    })
        .then(response => handleAdminResponse(response, '刪除文章失敗'))
        .then(result => {
            bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
            showAdminToast(result.message || '文章刪除成功', 'success');
            loadArticles(currentPage);
        })
        .catch(error => {
            console.error('[Delete] error:', error);
            showAdminToast(error.message || '文章刪除失敗', 'error');
        });
}

async function deleteArticle(id, title) {
    const result = await Swal.fire({
        icon: 'warning',
        title: `確定要刪除文章「${title}」嗎？此操作無法復原。`,
        showCancelButton: true,
        confirmButtonText: '確認',
        cancelButtonText: '取消'
    });
    if (!result.isConfirmed) {
        return;
    }
    
    fetchWithAuth(`${API_BASE}/admin/articles/${id}`, { 
        method: 'DELETE'
    })
        .then(response => handleAdminResponse(response, '刪除文章失敗'))
        .then(result => {
            showAdminToast(result.message || '文章已刪除', 'success');
            loadArticles(currentPage);
        })
        .catch(error => {
            console.error('[Delete] error:', error);
            showAdminToast(error.message || '刪除失敗', 'error');
        });
}



function editArticle(id) {
    window.location.href = `blog-admin-edit.html?id=${id}`;
}

// Load articles on page load (after auth check)
document.addEventListener('DOMContentLoaded', () => {
    requireAuth().then(user => {
        if (user) {
            currentUser = user;
            const userInfo = el('user-info');
            if (userInfo) {
                userInfo.innerHTML = `歡迎, ${user.displayName} | <a href="#" onclick="logout(); return false;" class="logout-link">登出</a>`;
            }
            loadArticles(0);
        }
    });
});
function renderPagination(data) {
    const container = document.getElementById('pagination-container');
    
    if (data.totalPages <= 1) {
        container.style.display = 'none';
        return;
    }

    const start = Math.max(0, data.page - 2);
    const end = Math.min(data.totalPages - 1, start + 4);
    const adjustedStart = Math.max(0, end - 4);
    const pages = Array.from({ length: end - adjustedStart + 1 }, (_, i) => adjustedStart + i);

    const paginationHtml = `
        <div class="mn-pro-pagination" style="display: flex; justify-content: space-between; align-items: center; margin-top: 30px;">
            <span>顯示第 ${data.page * data.size + 1}-${Math.min((data.page + 1) * data.size, data.totalElements)} 筆，共 ${data.totalElements} 筆</span>
            <ul class="mn-pro-pagination-inner">
                ${!data.hasPrevious ? '' : '<li><a class="prev" onclick="loadArticles(' + (data.page - 1) + ')"><i class="ri-arrow-left-double-line"></i></a></li>'}
                ${pages.map(p => `<li><a class="${p === data.page ? 'active' : ''}" onclick="loadArticles(${p})">${p + 1}</a></li>`).join('')}
                ${!data.hasNext ? '' : '<li><a class="next" onclick="loadArticles(' + (data.page + 1) + ')"><i class="ri-arrow-right-double-line"></i></a></li>'}
            </ul>
        </div>
    `;
    
    container.innerHTML = paginationHtml;
    container.style.display = 'block';
}
