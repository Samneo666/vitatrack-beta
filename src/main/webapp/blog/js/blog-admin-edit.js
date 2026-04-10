const API_BASE_URL = 'http://localhost:8080/api';
let quill;
let uploadedImageUrl = null;
let articleId = null;
let titleSlug = null;
let currentUpdatedAt = null;

// SVG placeholder for broken images
const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI4MDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEyMDAiIGhlaWdodD0iODAwIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI0NSUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSI0OCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+PGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSI2NCI+8J+TtzwvZm9udC1mYW1pbHk+PC90ZXh0Pjx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LWZhbWlseT0iQXJpYWwsIHNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMjQiIGZpbGw9IiNiYmJiYmIiIHRleHQtYW5jaG9yPSJtaWRkbGUiPuWclueJh+i8ieWFpeWksei0pTwvdGV4dD48L3N2Zz4=';

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    requireAuth().then(user => {
        if (!user) return;
        const userInfo = document.getElementById('user-info');
        if (userInfo) {
            userInfo.innerHTML = `歡迎, ${user.displayName} | <a href="#" onclick="logout(); return false;" class="logout-link">登出</a>`;
        }
        
        const urlParams = new URLSearchParams(window.location.search);
        articleId = urlParams.get('id');

        if (!articleId) {
            showAdminToast('缺少文章 ID', 'error');
            setTimeout(() => window.location.href = 'blog-admin.html', 1500);
            return;
        }

        quill = new Quill('#editor', {
            theme: 'snow',
            placeholder: '開始撰寫文章內容...',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline'],       // Essential formatting
                    [{ list: 'ordered' }, { list: 'bullet' }], // Lists
                    [{ header: [1, 2, 3, false] }],        // Headers (controlled)
                    ['blockquote'],                         // Quotes
                    ['clean']                               // Clear formatting
                ]
            }
        });

        setupImageUpload();
        setupFormSubmit();
        loadArticle();
    });
});

function loadArticle() {
    console.log('Loading article with ID:', articleId);
    
    fetchWithAuth(`${API_BASE_URL}/admin/articles/${articleId}`)
        .then(response => handleAdminResponse(response, '載入文章失敗'))
        .then(article => {
            console.log('Article loaded:', article);
            
            currentUpdatedAt = article.updatedAt;
            
            console.log('Setting titleDisplay...');
            const titleInput = document.getElementById('titleDisplay');
            console.log('titleInput element:', titleInput);
            titleInput.value = article.titleDisplay;
            console.log('titleInput.value after set:', titleInput.value);
            
            console.log('Setting summary...');
            document.getElementById('summary').value = article.summary || '';
            
            console.log('Setting category...');
            document.getElementById('category').value = article.category;
            
            console.log('Setting content...', article.content ? article.content.substring(0, 100) : 'NO CONTENT');
            if (article.content) {
                quill.root.innerHTML = article.content;
            }
            
            console.log('Form populated successfully');
            
            titleSlug = article.titleSlug;
            
            if (article.imageUrl) {
                uploadedImageUrl = article.imageUrl;
                const imagePreview = document.getElementById('imagePreview');
                const uploadContent = document.getElementById('uploadContent');
                const previewContainer = document.getElementById('previewContainer');
                const uploadArea = document.getElementById('uploadArea');

                imagePreview.src = article.imageUrl;
                imagePreview.onerror = function() {
                    this.onerror = null;
                    this.src = PLACEHOLDER_IMAGE;
                };
                uploadContent.style.display = 'none';
                previewContainer.style.display = 'block';
                uploadArea.style.padding = '20px';
                uploadArea.style.cursor = 'default';
            }
            
            // Force show main content
            const mainContent = document.querySelector('.mn-main-content');
            if (mainContent) {
                mainContent.style.visibility = 'visible';
                mainContent.style.opacity = '1';
                console.log('Main content shown');
            }
        })
        .catch(error => {
            console.error('[Load] error:', error);
            showAdminToast(error.message || '載入文章失敗', 'error');
            setTimeout(() => window.location.href = 'blog-admin.html', 1500);
        });
}

function setupImageUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    const uploadContent = document.getElementById('uploadContent');
    const previewContainer = document.getElementById('previewContainer');
    const imagePreview = document.getElementById('imagePreview');
    const changeImageBtn = document.getElementById('changeImageBtn');
    const deleteImageBtn = document.getElementById('deleteImageBtn');

    uploadArea.onclick = (e) => {
        if (e.target !== changeImageBtn && !changeImageBtn.contains(e.target) &&
            e.target !== deleteImageBtn && !deleteImageBtn.contains(e.target)) {
            if (uploadContent.style.display !== 'none') fileInput.click();
        }
    };

    changeImageBtn.onclick = (e) => {
        e.stopPropagation();
        fileInput.click();
    };

    deleteImageBtn.onclick = (e) => {
        e.stopPropagation();
        imagePreview.src = '';
        fileInput.value = '';
        uploadedImageUrl = null;
        uploadContent.style.display = 'block';
        previewContainer.style.display = 'none';
        uploadArea.style.padding = '50px';
        uploadArea.style.cursor = 'pointer';
    };

    fileInput.onchange = (e) => {
        if (e.target.files[0]) uploadImage(e.target.files[0]);
    };

    uploadArea.ondragover = (e) => {
        e.preventDefault();
        if (uploadContent.style.display !== 'none') {
            uploadArea.style.borderColor = '#3a4ee5';
            uploadArea.style.background = '#f8f9ff';
        }
    };

    uploadArea.ondragleave = () => {
        if (uploadContent.style.display !== 'none') {
            uploadArea.style.borderColor = '#d5d9e2';
            uploadArea.style.background = '#fafbfc';
        }
    };

    uploadArea.ondrop = (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = '#d5d9e2';
        uploadArea.style.background = '#fafbfc';
        if (e.dataTransfer.files[0]) {
            fileInput.files = e.dataTransfer.files;
            uploadImage(e.dataTransfer.files[0]);
        }
    };
}

function uploadImage(file) {
    if (file.size > 5 * 1024 * 1024) {
        showAdminToast('檔案大小超過 5MB，請選擇較小的圖片', 'error');
        return;
    }

    if (!titleSlug) {
        showAdminToast('無法取得文章代碼', 'error');
        return;
    }
    
    console.log('Uploading image:', file.name, 'size:', file.size, 'type:', file.type);
    console.log('Using titleSlug:', titleSlug);
    
    fetchWithAuth(`${API_BASE_URL}/admin/images/presign`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            fileName: file.name,
            contentType: file.type,
            articleSlug: titleSlug
        })
    })
        .then(response => handleAdminResponse(response, '獲取上傳URL失敗'))
        .then(data => {
            console.log('Presigned URL data:', data);
            console.log('Uploading to S3:', data.uploadUrl);
            
            return fetch(data.uploadUrl, {
                method: 'PUT',
                headers: { 'Content-Type': file.type },
                body: file
            }).then(uploadResponse => {
                console.log('S3 upload response status:', uploadResponse.status);
                if (!uploadResponse.ok) throw new Error('上傳圖片失敗');
                // Extract public URL from presigned URL
                const url = new URL(data.uploadUrl);
                return { publicUrl: `${url.origin}${url.pathname}` };
            });
        })
        .then(data => {
            uploadedImageUrl = data.publicUrl;
            
            const imagePreview = document.getElementById('imagePreview');
            const uploadContent = document.getElementById('uploadContent');
            const previewContainer = document.getElementById('previewContainer');
            const uploadArea = document.getElementById('uploadArea');

            imagePreview.onload = () => showAdminToast('圖片上傳成功', 'success');
            imagePreview.src = data.publicUrl;
            uploadContent.style.display = 'none';
            previewContainer.style.display = 'block';
            uploadArea.style.padding = '20px';
            uploadArea.style.cursor = 'default';
        })
        .catch(error => {
            console.error('[Upload] error:', error);
            showAdminToast(error.message || '圖片上傳失敗', 'error');
        });
}

function setupFormSubmit() {
    const form = document.getElementById('editForm');
    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const titleDisplay = document.getElementById('titleDisplay').value.trim();
        const summary = document.getElementById('summary').value.trim();
        const category = document.getElementById('category').value;
        const content = quill.root.innerHTML;
        const isContentEmpty = !content || content === '<p><br></p>';

        const missing = [];
        if (!titleDisplay) missing.push('文章標題');
        else if (titleDisplay.length < 2 || titleDisplay.length > 100) {
            showAdminToast('文章標題需為 2-100 字', 'error');
            return;
        }
        if (!summary) missing.push('文章摘要');
        if (!category) missing.push('文章分類');
        if (isContentEmpty) missing.push('文章內容');
        if (!uploadedImageUrl) missing.push('文章配圖');

        if (missing.length) {
            showAdminToast(`請填寫：${missing.join('、')}`, 'error');
            return;
        }

        // ArticleUpdateRequest DTO - ID in body, only client-editable fields
        const articleData = {
            id: parseInt(articleId),
            titleDisplay,
            summary: summary || titleDisplay,
            content,
            category,
            imageUrl: uploadedImageUrl,
            updatedAt: currentUpdatedAt  // Already epoch seconds from response
        };

        // Updated endpoint: PUT /api/admin/articles/{id}
        fetchWithAuth(`${API_BASE_URL}/admin/articles/${articleId}`, {
            method: 'PUT',
            body: JSON.stringify(articleData)
        })
            .then(response => {
                if (response.status === 409) {
                    return response.json().then(data => {
                        if (data.conflictType === 'version_mismatch') {
                            const modal = new bootstrap.Modal(document.getElementById('conflictModal'));
                            modal.show();
                            return Promise.reject({ handled: true });
                        }
                        throw new Error(data.errMsg || '更新文章失敗');
                    });
                }
                return handleAdminResponse(response, '更新文章失敗');
            })
            .then(article => {
                console.log('Updated article:', article.id, article.titleSlug);
                showAdminToast('文章更新成功', 'success');
                setTimeout(() => window.location.href = 'blog-admin.html', 1500);
            })
            .catch(error => {
                if (error.handled) return;
                console.error('[Update] error:', error);
                showAdminToast(error.message || '更新文章失敗', 'error');
            });
    });
}

