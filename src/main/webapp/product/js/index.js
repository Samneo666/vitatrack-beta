document.addEventListener("DOMContentLoaded", async function () {

    // ── 取得頁面元素 ──────────────────────────────────────────
    const guestHeader  = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfoBtn = document.getElementById("memberInfo");
    const orderInfoBtn  = document.getElementById("orderInfo");
    const logoutBtn     = document.getElementById("logoutBtn");

    // ── 驗證登入狀態，切換 Header ─────────────────────────────
    if (!guestHeader || !memberHeader) {
        console.error("找不到 Header 元素");
        return;
    }

    try {
        const response = await fetch("api/loginCheck");
        const data = await response.json();

        if (data.loggedIn) {
            guestHeader.style.display  = "none";
            memberHeader.style.display = "block";
        } else {
            guestHeader.style.display  = "block";
            memberHeader.style.display = "none";
        }
    } catch (error) {
        console.error("登入狀態檢查失敗：", error);
    }

    // ── 前往會員中心 ──────────────────────────────────────────
    memberInfoBtn.addEventListener("click", function (e) {
        e.preventDefault();
        window.location.href = "memberCenter.html";
    });

    // ── 前往訂單查詢 ──────────────────────────────────────────
    orderInfoBtn.addEventListener("click", function (e) {
        e.preventDefault();
        window.location.href = "memberCenter.html";
    });

    // ── 登出 ──────────────────────────────────────────────────
    logoutBtn.addEventListener("click", async function (e) {
        e.preventDefault();
        try {
            const response = await fetch("api/logout", {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });
            const result = await response.json();
            if (result.success) {
                alert(result.message);
                window.location.href = "index.html";
            }
        } catch (error) {
            console.error("登出失敗：", error);
            alert("登出過程中發生錯誤，請稍後再試。");
        }
    });

    // ── 點擊商品按鈕（.mn-btn-2）記錄 SKU ────────────────────
    // <a> 標籤本身會自動導頁，這裡只做 console log 方便除錯
    document.addEventListener("click", function (e) {
        const anchor = e.target.closest("a.mn-btn-2");
        if (!anchor) return;

        const params = new URLSearchParams(anchor.search);
        const sku = params.get("sku");
        if (sku) {
            console.log("[mn-btn-2] 點擊商品 SKU =", sku);
        }
    });

});
