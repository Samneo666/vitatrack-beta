document.addEventListener("DOMContentLoaded", function () {

    const email = document.getElementById("email");
    const password = document.getElementById("password");
    const loginForm = document.getElementById("loginForm");
    const loginBtn = document.getElementById("loginBtn");
    const rememberMeCheck = document.getElementById("rememberMe");

    //頁面載入讀取帳號
    const savedAccount = localStorage.getItem("account");
    if (savedAccount) {
        email.value = savedAccount;
        rememberMeCheck.checked = true;
    }

    // 忘記密碼連結
    const fpLink = document.getElementById("forgotPasswordLink");

    // === 前端格式驗證函式 ===
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    //記住帳號相關
    rememberMeCheck.addEventListener("change", function () {
        if (this.checked) {
            localStorage.setItem("account", email.value);
        } else {
            localStorage.removeItem("account");
        }
    });

   loginBtn.addEventListener("click", function (event) {
    event.preventDefault();

    // 簡化空值寫法
    if (!email.value.trim()) {
        Swal.fire({ icon: 'warning', title: '請填寫帳號和密碼', confirmButtonText: '確認' });
        return;
    }
    if (!password.value.trim()) {
        Swal.fire({ icon: 'warning', title: '請填寫帳號和密碼', confirmButtonText: '確認' });
        return;
    }
    if (!isValidEmail(email.value.trim())) {
        Swal.fire({ icon: 'warning', title: '請輸入正確的 Email 格式', confirmButtonText: '確認' });
        return;
    }

    // 記住帳號
    if (rememberMeCheck.checked) {
        localStorage.setItem("account", email.value);
    } else {
        localStorage.removeItem("account");
    }

    //驗證通過才禁用按鈕
    loginBtn.disabled = true;

    fetch('api/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginForm.email.value, password: loginForm.password.value })
    })
        .then(result => result.json())
        .then(response => {
            if (response.success) {
                Swal.fire({
                    icon: 'success',
                    title: '登入成功',
                    text: '歡迎回來！',
                    confirmButtonText: '確認'
                }).then(() => {
                    window.location.href = "memberCenter.html"; //跳轉放在彈窗確認後
                });
                //reset 移除，跳頁後自然清空
            } else {
                Swal.fire({
                    icon: 'error',
                    title: '登入失敗',
                    text: response.message,
                    confirmButtonText: '確認'
                });
                loginForm.reset(); //失敗清空
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire({ icon: 'error', title: '登入過程中發生錯誤，請稍後再試', confirmButtonText: '確認' });
        })
        .finally(() => {
            loginBtn.disabled = false; //無論成功失敗都還原
        });
});

    // 忘記密碼功能（使用 SweetAlert2 input 對話框）
    fpLink.addEventListener("click", async function (event) {
        event.preventDefault();

        const { value: emailVal, isConfirmed } = await Swal.fire({
            title: '忘記密碼',
            text: '請輸入您註冊的電子信箱',
            input: 'email',
            inputPlaceholder: 'your-email@example.com',
            showCancelButton: true,
            confirmButtonText: '送出',
            cancelButtonText: '取消',
            inputValidator: (value) => {
                if (!value) return '請輸入 Email';
                if (!isValidEmail(value.trim())) return '請輸入有效的 Email 格式';
            }
        });

        if (!isConfirmed || !emailVal) return;

        fetch('api/forgetPassword', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: emailVal.trim() })
        })
            .then(response => response.json())
            .then(result => {
                console.log("forgetPassword 回傳：", result);
                if (result.success) {
                    Swal.fire({
                        icon: 'success',
                        title: '已寄出重設連結',
                        text: '重設密碼連結已寄出，請檢查您的信箱',
                        confirmButtonText: '確認'
                    });
                } else {
                    Swal.fire({ icon: 'error', title: result.message, confirmButtonText: '確認' });
                }
            })
            .catch(error => {
                console.error("Error:", error);
                Swal.fire({ icon: 'error', title: '發生錯誤，請稍後再試', confirmButtonText: '確認' });
            });
    });

    // 明碼遮罩功能顯示
    let passwordInput = document.getElementById('password');
    let togglePasswordButton = document.getElementById('toggleEye');

    togglePasswordButton.addEventListener('click', function () {
        let type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        this.classList.toggle('ri-eye-off-line');
        this.classList.toggle('ri-eye-line');
    });

});
