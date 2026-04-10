// blog-list.js - Article list page logic

let currentPage = 0;
let currentCategory = '';
let currentAuthor = '';

function loadArticles() {
    toggle(el('mn-overlay'), true);  
    el('articles-container').style.display = 'none';  
    toggle(el('pagination'), false);  
    
    const params = new URLSearchParams({ page: currentPage, size: 6 });
    if (currentCategory) params.append('category', currentCategory);
    if (currentAuthor) params.append('authorSlug', currentAuthor);

    fetch(`${API_BASE}/articles?${params}`)
        .then(response => {
            if (response.status === 400) throw new Error('Bad Request');
            if (response.status === 404) throw new Error('Article not found'); 
            if (response.status === 500) throw new Error('Server Error');
            if (!response.ok) throw new Error('Network Error');
            return response.json();
        })
        .then(result => {
            if (!result.success) {
                throw new Error(result.errMsg || 'Server Error');
            }
            const data = result.data;
            renderArticles(data.articles);
            renderPagination(data); 
            el('articles-container').style.display = 'flex';  
        })
        .catch(error => {
            console.error('[Load] error:', error);
            if (error.message === 'Bad Request') {
                showLoadError('articles-container', '請求錯誤', '重新載入', 'location.reload()');
            } else if (error.message === 'Article not found') {
                showNotFoundError('articles-container', '找不到內容', '返回列表', 'blog-list.html');
            } else if (error.message === 'Server Error') {
                showLoadError('articles-container', '伺服器錯誤', '重試', 'loadArticles()');
            } else {
                showLoadError('articles-container', '網路錯誤', '重試', 'loadArticles()');
            }
        })
        .finally(() => {
            toggle(el('mn-overlay'), false);  
        });
}

function renderArticles(articles) {
    const html = articles.map(a => `
        <div class="col-md-4 col-sm-6 col-12 mn-blog-block m-b-24">
            <div class="mn-blog-card">
                <div class="blog-info blog-card-clickable" onclick="location.href='blog-detail.html?slug=${a.titleSlug}'">
                    <figure class="blog-img blog-card-image">
                        <a href="blog-detail.html?slug=${a.titleSlug}">
                            <img src="${a.imageUrl || PLACEHOLDER_IMAGE}" 
                                 onerror="this.onerror=null; this.src='${PLACEHOLDER_IMAGE}';"
                                 alt="${escapeHtml(a.titleDisplay)}" />
                        </a>
                    </figure>
                    <div class="detail blog-card-detail">
                        <label>${formatDate(a.createdAt)} - <a href="blog-list.html?category=${encodeURIComponent(a.category)}" onclick="event.stopPropagation()">${escapeHtml(a.category)}</a></label>
                        <h3><a href="blog-detail.html?slug=${a.titleSlug}">${escapeHtml(a.titleDisplay)}</a></h3>
                        <div class="more-info">
                            <a href="blog-detail.html?slug=${a.titleSlug}">閱讀更多<i class="ri-arrow-right-double-line"></i></a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
    el('articles-container').innerHTML = html;
}

function renderPagination(data) {
    if (data.totalPages <= 1) {
        toggle(el('pagination'), false);
        return;
    }

    const isFirst = data.page === 0;
    const isLast = data.page >= data.totalPages - 1;

    el('pagination').style.display = 'flex';
    el('pagination-info').textContent = `顯示第 ${data.page * data.size + 1}-${Math.min((data.page + 1) * data.size, data.totalElements)} 項，共 ${data.totalElements} 項`;

    const start = Math.max(0, data.page - 2);
    const end = Math.min(data.totalPages - 1, start + 4);
    const adjustedStart = Math.max(0, end - 4);
    const pages = Array.from({ length: end - adjustedStart + 1 }, (_, i) => adjustedStart + i);
    
    const buttons = [];
    if (!isFirst) buttons.push({ page: data.page - 1, html: '<li><a class="prev"><i class="ri-arrow-left-double-line"></i></a></li>' });
    pages.forEach(p => buttons.push({ page: p, html: `<li><a class="${p === data.page ? 'active' : ''}">${p + 1}</a></li>` }));
    if (!isLast) buttons.push({ page: data.page + 1, html: '<li><a class="next"><i class="ri-arrow-right-double-line"></i></a></li>' });
    
    el('pagination-buttons').innerHTML = buttons.map(b => b.html).join('');
    el('pagination-buttons').querySelectorAll('a').forEach((btn, i) => {
        btn.onclick = () => goToPage(buttons[i].page);
    });
}

function goToPage(page) {
    currentPage = page;
    loadArticles();
}

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const categoryParam = urlParams.get('category');
    const authorParam = urlParams.get('authorSlug');
    
    if (categoryParam) {
        currentCategory = categoryParam;
    }
    if (authorParam) {
        currentAuthor = authorParam;
    }
    loadArticles();
});
