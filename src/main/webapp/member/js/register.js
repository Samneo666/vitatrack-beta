document.addEventListener("DOMContentLoaded", function () {

    const name = document.getElementById("name");
    const email = document.getElementById("email");
    const phone = document.getElementById("phone");
    const address = document.getElementById("address");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    const registerForm = document.getElementById("registerForm");
    const registerBtn = document.getElementById("registerBtn");

    name.addEventListener("blur", function () {
        const hint = document.getElementById("nameHint");
        console.log(hint);

        if (name.value.trim() === "") {
            hint.textContent = "此欄為必填欄位";
            hint.style.color = "red";
        } else {
            hint.textContent = "格式正確";
            hint.style.color = "green";
        }
    });

    email.addEventListener("blur", function () {
        const hint = document.getElementById("emailHint");
        console.log(hint);
        const rule = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        if (!email.value.match(rule)) {
            hint.textContent = "請輸入有效的Email，例如：example@mail.com";
            hint.style.color = "red";
        } else {
            hint.textContent = "格式正確";
            hint.style.color = "green";
        }
    });

    phone.addEventListener("blur", function () {
        const hint = document.getElementById("phoneHint");
        console.log(hint);
        const rule = /^09[0-9]{8}$/;
        if (!phone.value.match(rule)) {
            hint.textContent = "必須是09開頭且共10位數字";
            hint.style.color = "red";
        } else {
            hint.textContent = "格式正確";
            hint.style.color = "green";
        }
    });

    password.addEventListener("blur", function () {
        const hint = document.getElementById("passwordHint");
        console.log(hint);
        const rule = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;
        if (!password.value.match(rule)) {
            hint.textContent = "密碼至少為8個字元，且至少包含1個英文字母(大小寫皆可)與1個數字";
            hint.style.color = "red";
        } else {
            hint.textContent = "格式正確";
            hint.style.color = "green";
        }
    });

    confirmPassword.addEventListener("blur", function () {
        const hint = document.getElementById("confirmPasswordHint");
        console.log(hint);

        if (confirmPassword.value !== password.value || confirmPassword.value === "") {
            hint.textContent = "與設定密碼不一致";
            hint.style.color = "red";
        } else {
            hint.textContent = "與設定密碼一致";
            hint.style.color = "green";
        }
    });
    registerBtn.addEventListener("click", function (event) {
        // event.preventDefault();
        // 驗證註冊功能中各個必填欄位是否為空，若空return 此欄為必填欄位;前端有驗證的後端程式也要驗證
        // trim() 方法會回傳一個去除了空格的字串，它永遠不會回傳 null（如果原字串不是 null）。
        // 如果使用者只輸入空白，trim() 會回傳 ""（空字串）。
        // 1 姓名不能空白
        if (!name.value || name.value.trim() === "") {
            Swal.fire({ icon: 'warning', title: '此欄為必填欄位', confirmButtonText: '確認' });
            return;
        }

        // 2 電子郵件 必須為^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$
        if (!email.value || !email.value.match(/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/)) {
            Swal.fire({ icon: 'warning', title: 'email格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }

        // 3 手機 必須是09開頭且共10位數字("^09[0-9]{8}$") 手機號碼格式錯誤或未填寫!
        if (!phone.value || !phone.value.match(/^09[0-9]{8}$/)) {
            Swal.fire({ icon: 'warning', title: '手機號碼格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }

        // 4 密碼 密碼至少為 8 個字元，且至少包含 1 個英文字母(大小寫皆可)與 1 個數字 密碼格式錯誤或未填寫!
        if (!password.value || !password.value.match(/^(?=.*[A-Za-z])(?=.*\d).{8,}$/)) {
            Swal.fire({ icon: 'warning', title: '密碼格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }
        // 5 重新輸入密碼 和密碼 必須一致  與設定密碼不一致，請重新輸入!
        if (confirmPassword.value !== password.value || confirmPassword.value === "") {
            Swal.fire({ icon: 'warning', title: '與設定密碼不一致，請重新輸入!', confirmButtonText: '確認' });
            return;
        }

        // 6 判斷帳號是否有重複，資料庫的email不能等於新註冊的email
        //但前端無法真正「驗證」Email 是否重複（因為前端拿不到資料庫完整清單）。

        // 7 利用ajax 發出請求，接回應
        fetch('register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: registerForm.name.value,
                email: registerForm.email.value,
                phone: registerForm.phone.value,
                address: registerForm.address ? registerForm.address.value.trim() : "",
                password: registerForm.password.value,
                confirmPassword: registerForm.confirmPassword.value
            })
        })
            .then(response => response.json()) //接收ap server 回傳的json格式，反序列化為js物件
            .then(result => {
                console.log("這是後端回傳的所有資料：", result); //  將後端回傳的 JSON 字串轉換後的 JS 物件。
                if (result.success) {
                    //如果後端顯示true，要顯示註冊成功 
                    // 顯示自訂彈窗
                    // 使用 flex 才能觸發 CSS 裡的置中效果
                    const alertTitle = document.querySelector(".alert-title");
                    alertTitle.textContent = result.message;
                    const customAlert = document.getElementById("customAlert");
                    customAlert.style.display = "flex"; //彈窗顯示
                    // 清空表單
                    registerForm.reset();
                } else {
                    //如果後端顯示false，要顯示註冊失敗
                    // result.message 就是在 Service 層調用String register(Member member) 回傳的 errMsg 內容
                    Swal.fire({ icon: 'error', title: '註冊失敗', text: result.message, confirmButtonText: '確認' });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '發生系統錯誤，請稍後再試。', confirmButtonText: '確認' });
            })
    });
    // 彈窗按鈕 → 前往登入頁面 
    const loginBtn = document.getElementById("loginBtn");
    loginBtn.addEventListener("click", function () {
        console.log("前往登入頁按鈕被按！");
        window.location.href = "login.html"; // 跳轉到我的登入頁面
    });

    // 明碼遮罩功能顯示

    let passwordInput = document.getElementById('password');
    let confirmPasswordInput = document.getElementById('confirmPassword');
    let togglePasswordButton = document.getElementById('toggleEye');
    let toggleConfirmPasswordButton = document.getElementById('toggleConfirmEye');

    togglePasswordButton.addEventListener('click', function () {
        let type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        this.classList.toggle('ri-eye-off-line');
        this.classList.toggle('ri-eye-line');
    });

    toggleConfirmPasswordButton.addEventListener('click', function () {
        let confirmPasswordType = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        confirmPasswordInput.setAttribute('type', confirmPasswordType);
        this.classList.toggle('ri-eye-off-line');
        this.classList.toggle('ri-eye-line');
    });


});