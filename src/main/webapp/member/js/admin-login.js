document.addEventListener("DOMContentLoaded", function () {

    const account = document.getElementById("account");
    const password = document.getElementById("password");
    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");

   

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

                    Swal.fire({ icon: 'success', title: '後台管理登入成功', text: '歡迎回來！', confirmButtonText: '確認' }).then(() => {
                        window.location.href = "admin.html";
                    });
                    loginForm.reset();

                } else {

                    Swal.fire({ icon: 'error', title: '帳號或密碼錯誤', text: '請重新輸入!', confirmButtonText: '確認' });
                    loginForm.reset();
                }
            }).catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '登入過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
            });
    });

    

    

});