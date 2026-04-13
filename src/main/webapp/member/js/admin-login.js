document.addEventListener("DOMContentLoaded", function () {

    const account = document.getElementById("account");
    const password = document.getElementById("password");
    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");

   

    //彈窗相關
    const customAlert = document.getElementById("customAlert");
    const alertMessage = document.getElementById("alertMessage");
    const alertBtn = document.getElementById("alertBtn");

    // 定義彈窗顯示函式
    function showAlert(message) {
        alertMessage.innerHTML = message;
        customAlert.style.display = "flex"; //視窗彈出(.style.display = "flex")
    }

    // 定義彈窗關閉函式
    alertBtn.onclick = function () {
        customAlert.style.display = "none";
    };


    // === 前端格式驗證函式 ===
    function isValidEmail(account) {
        // 簡單正規檢查 email 格式
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(account);
    }



    loginBtn.addEventListener("click", function (event) {


        if (!account.value || account.value == '') {
            Swal.fire({ icon: 'warning', title: '請填寫帳號和密碼', confirmButtonText: '確認' });
            return;
        }
        if (!password.value || password.value == '') {
            Swal.fire({ icon: 'warning', title: '請填寫帳號和密碼', confirmButtonText: '確認' });
            return;
        }
        // 前端格式驗證
        if (!isValidEmail(account.value.trim())) {
            Swal.fire({ icon: 'warning', title: '請輸入正確的 Email 格式', confirmButtonText: '確認' });
            return;
        }

        fetch('api/adminLogin', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                account: loginForm.account.value,
                password: loginForm.password.value })
        }).then(result => result.json())
            .then(result => {
                console.log("這是後端回傳結果：", result);
                if (result.success) {

                    showAlert("後台管理登入成功<br>歡迎回來！"); 
                    alertBtn.onclick = function () {
                        window.location.href = "admin.html";
                    };
                    loginForm.reset();

                } else {

                    showAlert("帳號或密碼錯誤<br>請重新輸入!");
                    loginForm.reset();
                }
            }).catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '登入過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
            });
    });

    

    

});