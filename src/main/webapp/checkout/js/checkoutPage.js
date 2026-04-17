document.addEventListener("DOMContentLoaded", function () {
    // --- 1. 統一宣告所有 DOM 元素 (避免重複宣告) ---
    const guestHeader = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfoBtn = document.getElementById("memberInfo");
    const orderInfoBtn = document.getElementById("orderInfo");
    const logoutBtn = document.getElementById("logoutBtn");
    const cartIcon = document.getElementById("cartIcon");
    
    // 收件人資訊元素
    const bill1Btn = document.getElementById("bill1");
    const bill2Btn = document.getElementById("bill2");
    const nameInput = document.getElementById("name");
    const phoneInput = document.getElementById("phone");
    const addressInput = document.getElementById("address");
    
    // 結帳按鈕
    const checkoutBtn = document.getElementById("btnCheckout");

    // --- 2. 會員導向功能 ---
    if (memberInfoBtn) {
        memberInfoBtn.addEventListener("click", (e) => {
            e.preventDefault();
            window.location.href = "memberCenter.html";
        });
    }
    if (orderInfoBtn) {
        orderInfoBtn.addEventListener("click", (e) => {
            e.preventDefault();
            window.location.href = "memberCenter.html";
        });
    }

    // --- 3. 登出功能 ---
    if (logoutBtn) {
        logoutBtn.addEventListener("click", function (e) {
            e.preventDefault();
            fetch('api/logout', { method: 'POST' })
                .then(res => res.json())
                .then(result => {
                    if (result.success) window.location.href = "index.html";
                })
                .catch(err => {
                    console.error('登出失敗:', err);
                    if (typeof Swal !== 'undefined') Swal.fire({ icon: 'error', title: '登出失敗' });
                });
        });
    }

    // --- 4. 購物車圖示 ---
    if (cartIcon) {
        cartIcon.addEventListener("click", () => window.location.href = "cartPage.html");
    }

    // --- 5. 地址資訊填充 ---
    if (bill1Btn) {
        bill1Btn.addEventListener("click", function () {
            fetch('api/getProfile')
                .then(res => res.json())
                .then(result => {
                    if (result.success && result.data) {
                        const p = result.data;
                        if (nameInput) nameInput.value = p.name || "";
                        if (phoneInput) phoneInput.value = p.phone || "";
                        if (addressInput) addressInput.value = p.address || "";
                    }
                });
        });
    }

    if (bill2Btn) {
        bill2Btn.addEventListener("click", () => {
            if (nameInput) nameInput.value = "";
            if (phoneInput) phoneInput.value = "";
            if (addressInput) addressInput.value = "";
        });
    }

    // --- 6. 渲染購物車摘要 ---
    fetch('api/getCartItem')
        .then(res => res.json())
        .then(response => {
            console.log(response); // 確認後端回傳的資料結構
            
            const cartList = document.getElementById("dynamicCartList");
            const subTotalElem = document.getElementById("subtotal");
            const totalPriceElem = document.getElementById("total");
            if (!cartList) return;

            let htmlContent = "";
            let totalSum = 0;
            const items = response.data || [];

            items.forEach(item => {
                const sub = item.unitPrice * item.quantity;
                totalSum += sub;
                htmlContent += `
                    <div class="summary-item" style="display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
                        <img src="${item.imageUrl}" alt="${item.productName}" style="width: 60px; height: 60px; object-fit: contain; margin-right: 12px; border-radius: 4px;">
                        <div style="flex-grow: 1;">
                            <div style="font-weight: 600;">${item.productName}</div>
                            <div style="font-size: 13px; color: #888;">NT$${item.unitPrice} × ${item.quantity}</div>
                        </div>
                        <div style="font-weight: 600;">NT$${sub}</div>
                    </div>`;
            });

            cartList.innerHTML = items.length ? htmlContent : "<p>購物車是空的</p>";
            if (subTotalElem) subTotalElem.textContent = totalSum;
            if (totalPriceElem) totalPriceElem.textContent = totalSum;
        });

    // --- 7. 結帳送出 ---
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", function (e) {
            e.preventDefault();

            // 檢查空值
            if (!nameInput.value.trim() || !phoneInput.value.trim() || !addressInput.value.trim()) {
                Swal.fire({ icon: 'warning', title: '請填寫完整的收件人資訊' });
                return;
            }

            checkoutBtn.disabled = true; // 使用 disabled 屬性更安全
            const originalText = checkoutBtn.innerText;
            checkoutBtn.innerText = '處理中...';

            // 準備要送給後端的資料
            const orderData = {
                receiverName: nameInput.value,
                receiverPhone: phoneInput.value,
                receiverAddress: addressInput.value
            };

            fetch('placeOrder', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData) // <-- 這裡原本漏掉了
            })
            .then(res => {
                if (!res.ok) return res.json().then(err => { throw err; });
                return res.json();
            })
            .then(data => {
                const params = new URLSearchParams({
                    orderId: data.orderId,
                    totalAmount: data.totalAmount,
                    status: data.status
                });
                window.location.href = `orderConfirmationPage.html?${params.toString()}`;
            })
            .catch(err => {
                console.error(err);
                Swal.fire({ icon: 'error', title: err.message || '結帳失敗' });
            })
            .finally(() => {
                checkoutBtn.disabled = false;
                checkoutBtn.innerText = originalText;
            });
        });
    }
});