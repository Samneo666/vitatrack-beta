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


    // 忘記密碼相關
    const fpLink = document.getElementById("forgotPasswordLink");
    const fpAlert = document.getElementById("fpAlert");
    const fpEmailInput = document.getElementById("fpEmail");
    const fpSendBtn = document.getElementById("fpSendBtn");
    const fpCloseBtn = document.getElementById("fpCloseBtn");

    //彈窗相關
    const customAlert = document.getElementById("customAlert");
    const alertMessage = document.getElementById("alertMessage");
    const alertBtn = document.getElementById("alertBtn");

    // 定義彈窗顯示函式
    function showAlert(message) {
        alertMessage.innerHTML = message;
        customAlert.style.display = "flex";
    }

    // 定義彈窗關閉函式
    alertBtn.onclick = function () {
        customAlert.style.display = "none";
    };


    // === 前端格式驗證函式 ===
    function isValidEmail(email) {
        // 簡單正規檢查 email 格式
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    //記住帳號相關
    rememberMeCheck.addEventListener("change", function () {

        if (this.checked) {
            localStorage.setItem("account", email.value);
        } else {
            localStorage.removeItem("account");
        }
    })

    loginBtn.addEventListener("click", function (event) {
        event.preventDefault();
        //帳號和密碼一起做驗證
        //不驗證「這個 email 是否存在」
        //不提示「密碼錯誤」
        //不逐欄給錯誤提示
        if (!email.value || email.value == '') {
            alert("請填寫帳號和密碼");
            return;
        }
        if (!password.value || password.value == '') {
            alert("請填寫帳號和密碼");
            return;
        }
        // 前端格式驗證
        if (!isValidEmail(email.value.trim())) {
            alert("請輸入正確的 Email 格式");
            return;
        }
        //---------------------------------------------
        //登入時記住帳號
        if (rememberMeCheck.checked) {
            localStorage.setItem("account", email.value);
        } else {
            localStorage.removeItem("account");
        }

        // 隱藏 loading overlay
        // const overlay = document.getElementById('mn-overlay');
        // if (overlay) overlay.style.display = 'none';
        // const pagination = document.getElementById('pagination');
        // if (pagination) pagination.style.display = 'none';

        //利用ajax 發出請求，接回應
        fetch('api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: loginForm.email.value, password: loginForm.password.value })
        }).then(result => result.json())
            .then(response => {

                console.log("這是後端回傳結果：", response);

                if (response.success) {
                 
                    showAlert("會員登入成功<br>歡迎回來！"); //視窗彈出(.style.display = "flex"),會員登入成功<br>歡迎回來！ (.innerHTML)
                    alertBtn.onclick = function () {
                        window.location.href = "memberCenter.html";
                    };
                    loginForm.reset();
                } else {
                    showAlert(response.message);
                    loginForm.reset();
                }
            }).catch(error => {
                console.error('Error:', error);
                showAlert("登入過程中發生錯誤，請稍後再試");
            });
    });

    //忘記密碼功能

    //忘記密碼視窗
    fpLink.addEventListener("click", function (event) {
        event.preventDefault();
        fpAlert.style.display = "flex";
    });


    // 送出忘記密碼
    fpSendBtn.addEventListener("click", function () {
        const email = fpEmailInput.value.trim();

        if (!isValidEmail(email)) {
            alert("請輸入有效的 Email");
            return;
        }
        // 改成 AJAX 送後端
        fetch('api/forgetPassword', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email
            })
        })
            .then(response => response.json())
            .then(result => {
                console.log("forgetPassword 回傳：", result);

                if (result.success) {
                    showAlert("重設密碼連結已寄出<br>請檢查您的信箱");
                } else {
                    showAlert(result.message);
                }

                fpAlert.style.display = "none";
            })
            .catch(error => {
                console.error("Error:", error);
                showAlert("發生錯誤，請稍後再試");
            });
    });

    // 關閉忘記密碼彈窗
    fpCloseBtn.addEventListener("click", function () {
        fpAlert.style.display = "none";
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