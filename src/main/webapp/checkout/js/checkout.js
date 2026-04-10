document.addEventListener("DOMContentLoaded", function () {
    const guestHeader = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfoBtn = document.getElementById("memberInfo");
    const orderInfoBtn = document.getElementById("orderInfo");
    const logoutBtn = document.getElementById("logoutBtn");

    //------------驗證是否會員決定首頁Header------------
    if (!guestHeader || !memberHeader) {
        console.error("Header element not found");
        return;
    }

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
        fetch('/api/logout', {
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
                alert("登出過程中發生錯誤，請稍後再試。");
            });
    });
});