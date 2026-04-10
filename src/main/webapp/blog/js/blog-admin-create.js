const API_BASE_URL = 'http://localhost:8080/api';
let quill;
let uploadedImageUrl = null;
let currentUser = null;

// Initialize Quill editor
document.addEventListener('DOMContentLoaded', function() {
    requireAuth().then(user => {
        if (!user) return;
        currentUser = user;
        const userInfo = document.getElementById('user-info');
        if (userInfo) {
            userInfo.innerHTML = `歡迎, ${user.displayName} | <a href="#" onclick="logout(); return false;" class="logout-link">登出</a>`;
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
    });
});

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
        if (uploadContent.style.display !== 'none') {
            uploadArea.style.borderColor = '#d5d9e2';
            uploadArea.style.background = '#fafbfc';
        }
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

    const titleDisplay = document.getElementById('titleDisplay').value.trim();
    if (!titleDisplay) {
        showAdminToast('請先輸入文章標題', 'error');
        return;
    }

    const tempSlug = 'temp-' + Date.now();
    
    fetchWithAuth(`${API_BASE_URL}/admin/images/presign`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            fileName: file.name,
            contentType: file.type,
            articleSlug: tempSlug
        })
    })
        .then(response => handleAdminResponse(response, '獲取上傳URL失敗'))
        .then(data => {
            return fetch(data.uploadUrl, {
                method: 'PUT',
                headers: { 'Content-Type': file.type },
                body: file
            }).then(uploadResponse => {
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
    const form = document.getElementById('createForm');
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

        // ArticleCreateRequest DTO - only client-editable fields
        const articleData = {
            titleDisplay,
            summary,
            content,
            category,
            imageUrl: uploadedImageUrl
        };

        // Updated endpoint: POST /api/admin/articles
        fetchWithAuth(`${API_BASE_URL}/admin/articles`, {
            method: 'POST',
            body: JSON.stringify(articleData)
        })
            .then(response => handleAdminResponse(response, '發布文章失敗'))
            .then(article => {
                console.log('Created article:', article.id, article.titleSlug);
                showAdminToast('文章發布成功', 'success');
                setTimeout(() => window.location.href = 'blog-admin.html', 1500);
            })
            .catch(error => {
                console.error('[Create] error:', error);
                showAdminToast(error.message || '發布文章失敗', 'error');
            });
    });
}


