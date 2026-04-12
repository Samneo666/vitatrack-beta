// 監聽選單點擊
// 根據點擊的分頁，抓取資料渲染對應html

document.addEventListener("DOMContentLoaded", function () {
    const contentArea = document.getElementById('content-area');
    const menuLinks = document.querySelectorAll('.menu-link');
    const profile = menuLinks[0];
    const password = menuLinks[1];
    const privacy = menuLinks[2];
    const order = menuLinks[3];
    const memberInfo = document.getElementById('memberInfo');
    const orderInfoBtn = document.getElementById('orderInfo');
    const logoutBtn = document.getElementById('logoutBtn');
    const cartIcon = document.getElementById('cartIcon');


    loadMember();

    memberInfo.addEventListener("click", info);
    orderInfoBtn.addEventListener("click", (e) => orderInfo(e));

    profile.addEventListener("click", info);
    order.addEventListener("click", (e) => orderInfo(e));
    cartIcon.addEventListener("click", function (e) {
        e.preventDefault();
        window.location.href = "cart.html";
    });

    password.addEventListener("click", showPasswordChangeBtn);
    privacy.addEventListener("click", showDeleteBtn);

    //---------------------會員頁面歡迎詞及安全攔截-----------------------------------------------------------------------------
    function loadMember() {
        fetch('api/getProfile')
            .then(res => {
                // 先判斷 HTTP status
                if (!res.ok) {
                    if (res.status === 401) {
                        alert("您尚未登入，請先登入會員。");
                        window.location.href = "login.html";
                        return Promise.reject(); // 中斷後續
                    }
                }
                return res.json();
            })
            .then(result => {
                if (!result) return;

                console.log("會員資料：", result);

                // API 邏輯錯誤
                if (result.success === false) {
                    alert(result.message);
                    window.location.href = "login.html";
                    return;
                }

                const welcomeText = document.getElementById('welcomeText');

                if (welcomeText) {
                    // 注意 data
                    welcomeText.textContent = `${result.data.name}，歡迎回來`;
                }
            })
            .catch(err => {
                if (err) {
                    console.error("系統錯誤", err);
                    alert("系統異常，請稍後再試");
                }
            });
    }
    //--------------------------查看會員資訊------------------------------------------------------------------------
    function info(e) {
        e.preventDefault();

        fetch('api/getProfile')
            .then(res => res.json())
            .then(member => {
                console.log(member);
                contentArea.innerHTML = `
                 <div class="member-card">
                    <header class="member-card-header">會員資訊</header>
                    <div class="member-card-body">
                        <div class="member-form">
                            <div class="form-row d-flex align-items-center" data-editable="false"><label>使用者帳號</label><p class="readonly">${member.data.email}</p></div>
                            <div class="form-row d-flex align-items-center" data-field="name"><label>姓名</label><p class="readonly">${member.data.name}</p></div>
                            <div class="form-row d-flex align-items-center" data-editable="false"><label>Email</label><p class="readonly">${member.data.email}</p></div>
                            <div class="form-row d-flex align-items-center" data-field="address"><label>地址</label><p class="readonly">${member.data.address ?? '尚未填寫'}</p></div>
                            <div class="form-row d-flex align-items-center" data-field="phone"><label>手機號碼</label><p class="readonly">${member.data.phone}</p></div>

                            <div class="form-actions" >
                          
                                <button  class="mn-btn-1 " id="editBtn" type="button"><span>編輯</span></button>
                                <button class="mn-btn-1" id="saveBtn" type="button">
                                     <span>儲存變更</span>
                                </button>
                            </div>
                        </div>                      
                    </div>
                 </div>`;
            });


    };

    contentArea.addEventListener('click', function (e) {
        const btn = e.target.closest('button');
        if (!btn) {
            return;
        }
        if (btn.id === 'saveBtn') {
            saveChanges();
        }
        if (btn && btn.id === 'editBtn') {
            console.dir(btn);
            enableEdit('editBtn');
        }
        if (btn && btn.id === 'passwordChangeBtn') {
            passwordChange();
        }
        if (btn && btn.id === 'submitChangePasswordBtn') {
            console.log("submitChangePassword loaded");
            submitChangePassword();
        }

        if (btn && btn.id === 'deleteBtn') {
            deleteAccount();
        }

    });



    //--------------------------------------------------------------------------------------------------
    function enableEdit() {
        const rows = document.querySelectorAll('.form-row');
        rows.forEach(row => {
            if (row.dataset.editable === "false") return;
            const p = row.querySelector('p.readonly');
            if (!p) return; //如果找不到 <p> 元素，直接返回
            const input = document.createElement('input');
            input.type = 'text';
            input.value = p.textContent; //用當前顯示的值作為預設值
            input.className = 'edit-input';
            row.replaceChild(input, p);
        });
    }
    //---------------會員資料變更----------------------------------------------------------------------------
    function saveChanges() {

        const updatedData = {};
        //取得所有有 class="form-row" 的元素
        const rows = document.querySelectorAll('.form-row');

        rows.forEach(row => {
            const field = row.dataset.field;
            //找到裡面的 input
            const input = row.querySelector('input.edit-input');
            //如果這列有 field，且找到 input，就把值放進 updatedData
            if (field && input) {
                updatedData[field] = input.value;
            }
        });

        fetch('api/updateProfile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedData)
        })
            .then(result => result.json())
            .then(result => {
                console.log();

                if (result.success) {
                    alert(result.message);
                    location.reload();
                } else {
                    alert("更新失敗：" + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("更新過程中發生錯誤，請稍後再試。");
            });
    }
    //-------------點擊產生密碼變更按鈕-------------------------------------------------------------------------------------
    function showPasswordChangeBtn() {
        contentArea.innerHTML = `
     <div class="member-card">
        <header class="member-card-header">變更密碼</header>
        <div class="member-card-body">
            <div class="member-form">
                <div class="form-row d-flex align-items-center">
                    <label>密碼設定</label>
                    <div class="readonly-wrapper d-flex align-items-center">
                        <p class="readonly mb-0">********</p>
                        <button class="mn-btn-1 ms-3" id="passwordChangeBtn" type="button">
                            <span>變更</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>`;
    }
    //------------點擊產生帳號註銷按鈕--------------------------------------------------------------------------------------
    function showDeleteBtn() {
        contentArea.innerHTML = `
        <div class="member-card">
        <header class="member-card-header">隱私設定</header>
        <div class="member-card-body">
            <div class="member-form">
                <div class="form-row d-flex align-items-center">
                    <label>註銷帳號</label>
                    <div class="readonly-wrapper d-flex align-items-center">
                        <p class="readonly mb-0">*註銷帳號將使帳號停用!</p>
                        <button class="mn-btn-1 ms-3" id="deleteBtn" type="button">
                            <span>註銷</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>`;

    }
    //-------------更換密碼-------------------------------------------------------------------------------------
   function passwordChange() {
    contentArea.innerHTML = `
    <div class="member-card">
        <header class="member-card-header">重設密碼</header>
        <div class="member-card-body">
            <form method="post" id="resetForm">
                
                <div class="mn-resetPwd-wrap">
                    <label for="oldPassword">原密碼*</label>
                    <div id="oldPasswordHint" class="hint-text"></div>
                    <div class="input-group-custom">
                        <input type="password" id="oldPassword" name="oldPassword" placeholder="請輸入原密碼" required>
                        <i class="ri-eye-off-line" id="toggleOldPwdEye"></i>
                    </div>
                </div> <div class="mn-resetPwd-wrap">
                    <label for="newPassword">新密碼*</label>
                    <div id="newPasswordHint" class="hint-text"></div>
                    <div class="input-group-custom">
                        <input type="password" id="newPassword" name="newPassword" placeholder="請輸入新密碼" required>
                        <i class="ri-eye-off-line" id="toggleNewPwdEye"></i>
                    </div>
                </div> <div class="mn-resetPwd-wrap">
                    <label for="confirmPassword">確認密碼*</label>
                    <div id="confirmPasswordHint" class="hint-text"></div>
                    <div class="input-group-custom">
                        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="請再次輸入新密碼" required>
                        <i class="ri-eye-off-line" id="toggleConfirmPwdEye"></i>
                    </div>
                </div> <div class="mn-resetPwd-btn-area">
                    <button class="mn-btn-1 btn" type="button" id="submitChangePasswordBtn">
                        <span>確認重設密碼</span>
                    </button>
                </div>
                
            </form>
        </div>
    </div>`;

        const oldPassword = document.getElementById("oldPassword");
        const newPassword = document.getElementById("newPassword");
        const confirmPassword = document.getElementById("confirmPassword");

        // 明碼遮罩功能 - 每個眼睛圖示各自控制對應的密碼欄位
        function setupToggleEye(toggleId, inputId) {
            const toggleBtn = document.getElementById(toggleId);
            const input = document.getElementById(inputId);
            if (toggleBtn && input) {
                toggleBtn.addEventListener('click', function () {
                    const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                    input.setAttribute('type', type);
                    this.classList.toggle('ri-eye-off-line');
                    this.classList.toggle('ri-eye-line');
                });
            }
        }
        setupToggleEye('toggleOldPwdEye', 'oldPassword');
        setupToggleEye('toggleNewPwdEye', 'newPassword');
        setupToggleEye('toggleConfirmPwdEye', 'confirmPassword');


        // 驗證原密碼是否為空
        oldPassword.addEventListener("blur", function () {
            const pwd = oldPassword.value.trim();
            const hint = document.getElementById("oldPasswordHint");
            if (!pwd) {
                hint.textContent = "請輸入原密碼以驗證身份";
                hint.style.color = "red";
            } else {
                hint.textContent = "";
                hint.style.color = "green";
            }
        });
        // 驗證新密碼格式
        newPassword.addEventListener("blur", function () {
            const pwd = newPassword.value.trim();
            const hint = document.getElementById("newPasswordHint");
            const rule = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;
            if (pwd === "") return;
            if (!pwd.match(rule)) {
                hint.textContent = "密碼至少為8個字元，且至少包含1個英文字母(大小寫皆可)與1個數字";
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
    }
    //---------------------變更密碼-----------------------------------------------------------------------------
    function submitChangePassword() {

        const oldPassword = document.querySelector('#oldPassword').value.trim();
        const newPassword = document.querySelector('#newPassword').value.trim();
        const confirmPassword = document.querySelector('#confirmPassword').value.trim();

        // 基本檢查
        if (!oldPassword) {
            return alert("請輸入原密碼以驗證身份");
        }

        if (!newPassword || newPassword.length < 8) {
            return alert("新密碼格式不符!");
        }
        if (newPassword !== confirmPassword) {
            return alert("確認密碼與新密碼不一致!");
        }

        fetch('changePassword', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({

                oldPassword: document.querySelector('#oldPassword').value,
                newPassword: document.querySelector('#newPassword').value,
                confirmPassword: document.querySelector('#confirmPassword').value
            })
        })
            .then(result => result.json())
            .then(result => {
                console.log("回傳資料:", result);
                if (result.success) {
                    alert(result.message);
                    window.location.href = "login.html";
                } else {
                    alert(result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("系統發生錯誤，請稍後再試");

            });
    }
    //-------------------註銷帳號------------------------------------------------------------------------------
    function deleteAccount() {
        if (!confirm("確定要註銷帳號嗎？此操作將使帳號停用！")) {
            return;
        }

        // 2. 二次確認：要求輸入密碼 (安全關鍵)      !!!!改用輸入框!!!!!!
        const password = prompt("為了安全起見，請輸入您的密碼以確認註銷：");

        if (!password) {
            alert("必須輸入密碼才能執行此操作");
            return;
        }

        fetch('api/deleteAccount', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                password: password
            })
        })
            .then(result => result.json())
            .then(result => {
                if (result.success) {
                    alert(result.message);
                    localStorage.removeItem("token");
                    window.location.href = 'index.html';
                } else {
                    alert("註銷帳號失敗：" + result.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("處理過程中發生錯誤，請稍後再試。");
            });
    }

    //-------------------查看訂單-----------------------------------------------------------------------------
    window.orderInfo = function (e, page = 1) {
        e.preventDefault();

        fetch(`api/myOrder?page=${page}`)
            .then(res => res.json())
            .then(result => {
                console.log("後端回傳：", result);
                const orders = result.content;
                const totalPages = result.totalPages;
                const currentPage = result.currentPage;

                if (!orders || orders.length === 0) {
                    contentArea.innerHTML = `
                    <div class="member-card">
                        <header class="member-card-header">我的訂單</header>
                        <div class="member-card-body">
                            <p>目前沒有訂單紀錄</p>
                        </div>
                    </div>
                `;
                    return;
                }

                let rows = "";

                orders.forEach(order => {

                    rows += `
                    <tr>
                        <td>${order.orderId}</td>
                        <td>${order.createdAt}</td>
                        <td>NT$${order.totalAmount}</td>
                        <td>${order.paymentMethod}</td>
                        <td>${order.status ?? '待付款'}</td>
                        <td>${order.paymentStatus}</td>
                    </tr>
                `;

                });

                let tablehtml = `
                <div class="member-card">

                    <header class="member-card-header" >
                        我的訂單
                    </header>

                    <div class="member-card-body">

                        <table id="orderTable" class="table table-hover align-middle">

                            <thead>
                                <tr>
                                    <th>訂單編號</th>
                                    <th>訂單時間</th>
                                    <th>金額</th>
                                    <th>付款方式</th>
                                    <th>訂單狀態</th>
                                    <th>付款狀態</th>
                                </tr>
                            </thead>

                            <tbody>
                                ${rows}
                            </tbody>

                        </table>

                    </div>

                </div>
            `;


                let paginationHtml = `
                <nav class="d-flex justify-content-center m-t-30">
                    <ul class="pagination">
                        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                            <a class="page-link" href="javascript:void(0)" onclick="orderInfo(event, ${currentPage - 1})">上一頁</a>
                        </li>`;

                for (let i = 1; i <= totalPages; i++) {
                    paginationHtml += `
                    <li class="page-item ${i === currentPage ? 'active' : ''}">
                        <a class="page-link" href="javascript:void(0)" onclick="orderInfo(event, ${i})">${i}</a>
                    </li>`;
                }

                paginationHtml += `
                        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="javascript:void(0)" onclick="orderInfo(event, ${currentPage + 1})">下一頁</a>
                        </li>
                    </ul>
                </nav>`;

                contentArea.innerHTML = tablehtml + paginationHtml;
            })
            .catch(error => {
                console.error(error);
                alert("取得訂單資料過程發生錯誤，請稍後再試。");
            });

    }
    //---------------------查看購物車---------------------------------------------------------------------------
    function loadCartItems() {
        // 假設 contentArea 已定義，是用來放購物車或訂單的區域


        fetch('api/myCartItem')
            .then(res => res.json())
            .then(cartItems => {
                console.log("這是後端回傳:", cartItems);

                // 如果購物車沒有商品
                if (!cartItems || cartItems.length === 0) {
                    contentArea.innerHTML = ` <div class="member-card">
                        <header class="member-card-header">我的購物車</header>
                        <div class="member-card-body">
                            <p>購物車沒有商品</p>
                        </div>
                    </div>`;
                    return;
                }


                // 生成 table rows
                let rows = '';
                cartItems.forEach(item => {
                    rows += `
                    <tr>
                        <td>${item.productName}</td>
                        <td>${item.sku}</td>
                        <td>${item.size}</td>
                        <td>${item.price}</td>
                        <td>${item.quantity}</td>
                        <td>${item.subtotal}</td>
                        <td>${item.stockQuantity}</td>
                    </tr>
                `;
                });

                // 生成完整 table
                const tableHtml = `
                <div class="member-card">
                    <header class="member-card-header">
                        我的購物車                                                                        
                    </header>
                    <div class="member-card-body">
                        <table id="cartTable" class="table table-hover align-middle">
                            <thead>
                                <tr>
                                    <th>商品名稱</th>
                                    <th>商品編號</th>
                                    <th>規格/尺寸</th>
                                    <th>價格</th>
                                    <th>數量</th>
                                    <th>小計</th>
                                    <th>庫存量</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${rows}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;


                // 渲染到 contentArea
                contentArea.innerHTML = tableHtml;

            })
            .catch(err => {
                console.error("載入購物車失敗", err);
                alert("購物車載入失敗，請稍後再試");
            });
    }

    //---------------------會員登出----------------------------------------------------------------------------
    logoutBtn.addEventListener('click', function (e) {
        e.preventDefault();
        fetch('api/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(result => result.json())
            .then(result => {
                if (result.success) {
                    alert(result.message);
                    window.location.href = 'index.html';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert("登出過程中發生錯誤，請稍後再試。");
            });
    });


});