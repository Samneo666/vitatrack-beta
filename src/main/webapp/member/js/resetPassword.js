document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("resetForm");
    const resetBtn = document.getElementById("resetBtn");
    const newPassword = document.getElementById("newPassword");
    const confirmPassword = document.getElementById("confirmPassword");

    let isSubmitting = false;

    // 明碼遮罩功能顯示
    let newPasswordInput = document.getElementById('newPassword');
    let toggleNewPasswordButton = document.getElementById('toggleNewEye');
    let confirmPasswordInput = document.getElementById('confirmPassword');
    let toggleConfirmPasswordButton = document.getElementById('toggleConfirmEye');

    toggleNewPasswordButton.addEventListener('click', function () {
        let type = newPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        newPasswordInput.setAttribute('type', type);
        this.classList.toggle('ri-eye-off-line');
        this.classList.toggle('ri-eye-line');
    });

    toggleConfirmPasswordButton.addEventListener('click', function () {
        let type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        confirmPasswordInput.setAttribute('type', type);
        this.classList.toggle('ri-eye-off-line');
        this.classList.toggle('ri-eye-line');
    });

    // ===== 從網址取得 token =====
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get("token");

    if (!token || token.trim() === "") {
        Swal.fire({ icon: 'error', title: '連結無效或已失效', confirmButtonText: '確認' });
        resetBtn.disabled = true;
        return;
    }

    // 驗證新密碼格式
    newPassword.addEventListener("blur", function () {
        const pwd = newPassword.value.trim();
        const hint = document.getElementById("newPasswordHint");
        const rule = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;
        if (!pwd.match(rule)) {
            hint.textContent = "密碼至少為8個字元，且至少包含1個英文字母與1個數字";
            hint.style.color = "red";
        } else {
            hint.textContent = "格式正確";
            hint.style.color = "green";
        }
    });

    // 驗證確認密碼
    confirmPassword.addEventListener("blur", function () {
        const pwd = newPassword.value.trim();
        const confirmPwd = confirmPassword.value.trim();
        const hint = document.getElementById("confirmPasswordHint");

        if (confirmPwd !== pwd || confirmPwd === "") {
            hint.textContent = "與設定密碼不一致";
            hint.style.color = "red";
        } else {
            hint.textContent = "與設定密碼一致";
            hint.style.color = "green";
        }
    });

    // ===== 點擊重設密碼 =====
    resetBtn.addEventListener("click", function (e) {

        e.preventDefault();
        if (isSubmitting) return;

        const pwd = newPassword.value.trim();
        const confirmPwd = confirmPassword.value.trim();

        if (!pwd || !pwd.match(/^(?=.*[A-Za-z])(?=.*\d).{8,}$/)) {
            Swal.fire({ icon: 'warning', title: '密碼格式錯誤或未填寫!', confirmButtonText: '確認' });
            return;
        }

        if (confirmPwd !== pwd || confirmPwd === "") {
            Swal.fire({ icon: 'warning', title: '與設定密碼不一致，請重新輸入!', confirmButtonText: '確認' });
            return;
        }

        isSubmitting = true;
        resetBtn.disabled = true;

        fetch('api/resetPassword', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                token: token,
                newPassword: pwd
            })
        })
            .then(result => {
                if (!result.ok) {
                    throw new Error("HTTP 錯誤");
                }
                return result.json();
            })
            .then(result => {
                if (result && result.success) {
                    Swal.fire({ icon: 'success', title: '密碼重設成功', text: '請重新登入', confirmButtonText: '確認' }).then(() => {
                        window.location.href = "login.html";
                    });
                } else {
                    Swal.fire({ icon: 'error', title: result?.message || "重設失敗", confirmButtonText: '確認' });
                    isSubmitting = false;
                    resetBtn.disabled = false;
                }
            })
            .catch(error => {
                console.error(error);
                Swal.fire({ icon: 'error', title: '系統發生錯誤，請稍後再試', confirmButtonText: '確認' });
                isSubmitting = false;
                resetBtn.disabled = false;
            });

    });

});