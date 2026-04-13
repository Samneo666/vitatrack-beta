document.addEventListener("DOMContentLoaded", function () {
    const guestHeader = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfoBtn = document.getElementById("memberInfo");
    const orderInfoBtn = document.getElementById("orderInfo");
    const logoutBtn = document.getElementById("logoutBtn");

    //------------驗證是否會員決定首頁Header------------
    // if (!guestHeader || !memberHeader) {
    //     console.error("Header element not found");
    //     return;
    // }

    //------------結帳頁面屬會員才能使用的功能，無須決定是否顯示非會員Header------------


    //------------查看會員資料------------
    memberInfoBtn.addEventListener("click", function (e) {
        e.preventDefault();
        window.location.href = "memberCenter.html";
    });
    //------------查看訂單------------
    orderInfoBtn.addEventListener("click", function (e) {
        e.preventDefault();
        window.location.href = "memberCenter.html";
    });
    //------------會員登出------------
    logoutBtn.addEventListener("click", function (e) {
        e.preventDefault();
        fetch('api/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        })
            .then(result => result.json())
            .then(result => {
                if (result.success) {
                    window.location.href = "index.html";
                }
            }).catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '登出過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
            });
    });
    //------------結帳頁面購物車點擊跳頁-----------
    const cartIcon = document.getElementById("cartIcon");
    cartIcon.addEventListener("click", function () {
        window.location.href = "cartPage.html";
    });

    //------------渲染地址訊息在頁面------------
    const bill1Btn = document.getElementById("bill1");
    const bill2Btn = document.getElementById("bill2");
    bill1Btn.addEventListener("click", function () {
        fetch('api/getProfile')
            .then(result => result.json())
            .then(result => {
                if (result.success) {
                    const profile = result.data;
                    document.getElementById("name").value = profile.name || "";
                    document.getElementById("phone").value = profile.phone || "";
                    document.getElementById("address").value = profile.address || "";
                }
            }).catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '載入資料時發生錯誤，請稍後再試。', confirmButtonText: '確認' });
            });

    });

    bill2Btn.addEventListener("click", function () {
        document.getElementById("name").value = "";
        document.getElementById("phone").value = "";
        document.getElementById("address").value = "";

    });

    //------------訂單摘要頁面載入價格資訊-----------


    // 找到 checkoutPage.js 裡處理價格資訊的那一段
    fetch('api/getCartItem')
        .then(result => result.json())
        .then(response => { // 建議改名為 response 較好理解
            console.log("原始回傳資料:", response);

            const cartList = document.getElementById("dynamicCartList");
            const subTotalElem = document.getElementById("subtotal");
            const totalPriceElem = document.getElementById("total");

            if (!cartList) return;

            let htmlContent = "";
            let totalSum = 0;

            // 關鍵修正：從 response.data 取得陣列
            const items = response.data || [];

            items.forEach(item => {
                // 根據你提供的 Log，欄位就在 item 裡面，不需要 item.data.unitPrice
                const itemSubtotal = item.unitPrice * item.quantity;
                totalSum += itemSubtotal;

                htmlContent += `
    <div class="summary-item" style="
        display: flex; 
        align-items: center; 
        padding: 12px 0; 
        border-bottom: 1px solid #f0f0f0; 
        margin-bottom: 8px;">
        
        ${item.imageUrl ? `
            <div style="margin-right: 15px; flex-shrink: 0;">
                <img src="${item.imageUrl}" alt="${item.productName}" style="
                    width: 60px; 
                    height: 60px; 
                    object-fit: contain; 
                    border-radius: 8px; 
                    border: 1px solid #eee;">
            </div>
        ` : ''}

        <div style="flex-grow: 1; display: flex; flex-direction: column; justify-content: center;">
            <div style="
                font-size: 16px; 
                font-weight: 600; 
                color: #333; 
                margin-bottom: 4px;">
                ${item.productName}
            </div>
            <div style="font-size: 13px; color: #888;">
                單價: NT$${item.unitPrice} <span style="margin: 0 4px;">×</span> 數量: ${item.quantity}
            </div>
        </div>

        <div style="
            text-align: right; 
            font-weight: 600; 
            font-size: 16px; 
            color: #222; 
            margin-left: 10px;">
            NT$${itemSubtotal}
        </div>
    </div>
`
                cartList.innerHTML = htmlContent;
                subTotalElem.textContent = totalSum;
                totalPriceElem.textContent = totalSum;

            });





        }).catch(error => {
            console.error('Error:', error);
        });
    //------------點擊結帳-----------
    const checkoutBtn = document.getElementById("btnCheckout");
    const nameInput = document.getElementById("name");
    const phoneInput = document.getElementById("phone");
    const addressInput = document.getElementById("address");
    checkoutBtn.addEventListener("click", function (e) {
        e.preventDefault();


        if (nameInput.value.trim() === '' || phoneInput.value.trim() === '' || addressInput.value.trim() === '') {
            Swal.fire({ icon: 'warning', title: '請填寫完整的收件人資訊', confirmButtonText: '確認' });
            return;
        }

        // 禁用按鈕防止重複點擊
        checkoutBtn.style.pointerEvents = 'none';
        const originalText = checkoutBtn.innerText;
        checkoutBtn.innerText = '處理中...';

        fetch('placeOrder', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },

        })
            .then(response => {
                // 先檢查 HTTP 狀態碼
                if (!response.ok) {
                    // 如果後端拋出 BusinessException，通常會進到這裡
                    return response.json().then(err => { throw err; });
                }
                return response.json();

            })
            .then(data => {
                console.log('結帳成功：', data);
                const params = new URLSearchParams({
                    orderId: data.orderId,
                    totalAmount: data.totalAmount,
                    status: data.status
                });
                window.location.href = `orderConfirmationPage.html?${params.toString()}`;
            }).catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: error.message || '結帳過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
            })
            .finally(() => {
                // 恢復按鈕狀態
                checkoutBtn.style.pointerEvents = 'auto';
                checkoutBtn.innerText = originalText;
            });
    });
});