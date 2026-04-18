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
        const rule = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        const emailValue = email.value.trim();

        // 1.必填檢查
        if (!emailValue) {
            hint.textContent = "Email為必填欄位";
            hint.style.color = "red";
            return;
        }

        // 2.格式檢查
        if (!emailValue.match(rule)) {
            hint.textContent = "請輸入有效的Email，例如：example@mail.com";
            hint.style.color = "red";
            return;
        }

        // 3.後端驗證
        hint.textContent = "檢查中...";
        hint.style.color = "gray";

        fetch(`api/check-email?email=${encodeURIComponent(emailValue)}`)
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    hint.textContent = "此帳號已經被註冊了";
                    hint.style.color = "red";
                } else {
                    hint.textContent = "此帳號可以使用";
                    hint.style.color = "green";
                }
            })
            .catch(error => {
                console.error("Error:", error);
                hint.textContent = "暫時無法驗證帳號狀態";
                hint.style.color = "orange";
            });
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

        // 先做所有驗證，全部通過才禁用按鈕
        if (!name.value || name.value.trim() === "") {
            Swal.fire({ icon: 'warning', title: '此欄為必填欄位', confirmButtonText: '確認' });
            return;
        }

        if (!email.value || !email.value.match(/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/)) {
            Swal.fire({ icon: 'warning', title: 'email格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }
        
        //檢查 blur 的即時驗證結果
        const emailHint = document.getElementById("emailHint");
        if (emailHint.style.color === "red") {
            Swal.fire({ icon: 'warning', title: '此帳號已被註冊，請使用其他Email!', confirmButtonText: '確認' });
            return;
        }

        // 如果使用者從未 blur（hint 還是空的），強制發一次驗證
        if (emailHint.textContent === "" || emailHint.textContent === "檢查中...") {
            Swal.fire({ icon: 'warning', title: '請稍候，Email驗證尚未完成', confirmButtonText: '確認' });
            return;
        }

        if (!phone.value || !phone.value.match(/^09[0-9]{8}$/)) {
            Swal.fire({ icon: 'warning', title: '手機號碼格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }

        if (!password.value || !password.value.match(/^(?=.*[A-Za-z])(?=.*\d).{8,}$/)) {
            Swal.fire({ icon: 'warning', title: '密碼格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }

        if (confirmPassword.value !== password.value || confirmPassword.value === "") {
            Swal.fire({ icon: 'warning', title: '與設定密碼不一致，請重新輸入!', confirmButtonText: '確認' });
            return;
        }

        // 驗證全通過，這裡才禁用（只寫一次）
        registerBtn.disabled = true;

        fetch('api/register', {
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
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    registerForm.reset();
                    Swal.fire({
                        icon: 'success',
                        title: '註冊成功',
                        text: result.message,
                        confirmButtonText: '前往登入頁面'
                    }).then(() => {
                        window.location.href = 'login.html';
                    });
                } else {
                    Swal.fire({ icon: 'error', title: '註冊失敗', text: result.message, confirmButtonText: '確認' });
                }
            })
            .catch(error => {
                console.error('Error:', error);
                Swal.fire({ icon: 'error', title: '發生系統錯誤，請稍後再試。', confirmButtonText: '確認' });
            })
            .finally(() => {
                registerBtn.disabled = false; // ✅ 只在這裡還原，finally 保證一定執行
            });
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