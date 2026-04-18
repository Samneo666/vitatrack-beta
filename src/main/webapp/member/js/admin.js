document.addEventListener("DOMContentLoaded", function () {
    const contentArea = document.getElementById('contentArea');
    const memberList = document.getElementById('memberList');
    const memberSearch = document.getElementById('memberSearch');

    memberList.addEventListener("click", memberInfo);
    memberSearch.addEventListener("click", function () {

        contentArea.innerHTML = `
    <div class="search-container" style="text-align: center; margin-top: 30px;">
        <h5>會員查詢</h5>
        <div style="display: flex; justify-content: center; align-items: center; gap: 10px;">
            <input type="text" id="keyword" class="form-control" style="width: 40%;" placeholder="請輸入姓名、電話、地址..." />
            <button id="searchBtn" class="btn btn-sm btn-primary">搜尋</button>
        </div>
        <div id="searchResult" style="margin-top: 20px;"></div>
    </div>
`;

        //綁定搜尋事件
        document.getElementById("searchBtn").addEventListener("click", searchMember);
    });


});
//--------------------------------------------------------------------------------------------------
// 會員列表功能
function memberInfo(e, page = 1) {
    e.preventDefault();

    fetch(`api/memberList?page=${page}`)
        .then(response => response.json())
        .then(pageResult => {
            console.log("後端回傳：", pageResult);
            const members = pageResult.data.content;
            const totalPages = pageResult.data.totalPages;
            const currentPage = pageResult.data.currentPage;

            let tableHtml = `
                <table id="memberTable" class="table table-hover align-middle">
                    <thead>
                        <tr>
                             <th>ID</th>
                            <th>姓名</th>
                            <th>Email</th>
                            <th>電話</th>
                            <th>地址</th>
                            <th>狀態</th>
                            <th>註冊日期</th>
                            
                        </tr>
                    </thead>
                <tbody>`;
            members.forEach(m => {

                tableHtml += `
                        <tr>
                            <td>${m.memberId}</td>
                            <td>${m.name}</td>
                            <td>${m.email}</td>
                            <td>${m.phone}</td>
                            <td>${m.address ?? '尚未填寫'}</td>
                            <td>
                                <select class="form-select form-select-sm"  id="status-${m.memberId}" onchange="saveStatus('${m.email}',  this.value )">
                                    <option value="1" ${m.memberStatus === 1 ? "selected" : ""} class="text-success">啟用</option>
                                    <option value="0" ${m.memberStatus === 0 ? "selected" : ""} class="text-danger">停用</option>
                                </select>
                            </td>
                            <td>${m.registrationTime}</td>
                        </tr>`;

            });

            tableHtml += `</tbody></table>`;

            //動態產生分頁按鈕
            let paginationHtml = `
                <nav class="d-flex justify-content-center m-t-30">
                    <ul class="pagination">
                        <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                            <a class="page-link" href="#" onclick="memberInfo(event, ${currentPage - 1})">上一頁</a>
                        </li>`;

            for (let i = 1; i <= totalPages; i++) {
                paginationHtml += `
                    <li class="page-item ${i === currentPage ? 'active' : ''}">
                        <a class="page-link" href="#" onclick="memberInfo(event, ${i})">${i}</a>
                    </li>`;
            }

            paginationHtml += `
                        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="#" onclick="memberInfo(event, ${currentPage + 1})">下一頁</a>
                        </li>
                    </ul>
                </nav>`;


            contentArea.innerHTML = tableHtml + paginationHtml;
        })
        .catch(error => {
            console.error('Error fetching member list:', error);
        });

}
//--------------------------------------------------------------------------------------------------

//儲存會員狀態變更
function saveStatus(email, newStatus) {
    fetch('api/editStatus', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            memberStatus: newStatus
        })
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                Swal.fire({ icon: 'success', title: result.message, confirmButtonText: '確認' });
            } else {
                Swal.fire({ icon: 'error', title: result.message, confirmButtonText: '確認' });
            }
        })
        .catch(error => {
            console.error('會員狀態變更:', error.message);
        });
}

//點擊編輯按鈕後，切換會員狀態

// 會員關鍵字查詢功能
function searchMember(e) {
    e.preventDefault();
    const keyword = document.getElementById('keyword').value.trim();

    if (!keyword || keyword === "") {
        Swal.fire({ icon: 'warning', title: '請輸入搜尋關鍵字', confirmButtonText: '確認' });
        return;
    }

    fetch(`api/memberSearch?keyword=${encodeURIComponent(keyword)}`)
        .then(response => response.json())
        .then(members => {
            console.log(members);
            renderResult(members.data.content);
        })
        .catch(error => {
            console.error("錯誤:", error);
        });;
}

//--------------------------------------------------------------------------------------------------
function renderResult(members) {
    const searchResult = document.getElementById('searchResult');
    if (!members || members.length === 0) {
        searchResult.innerHTML = "<p>沒有找到符合條件的會員。</p>";
        return;
    }
    let tableHtml = `
            <table id="searchResultTable" class="table table-hover align-middle">
                  <thead>
                        <tr>
                            <th>ID</th>
                            <th>姓名</th>
                            <th>Email</th>
                            <th>電話</th>
                            <th>地址</th>
                            <th>狀態</th>
                            <th>註冊日期</th>
                        </tr>
                    </thead>
                <tbody>`;
    members.forEach(m => {

        tableHtml += `
                        <tr>
                            <td>${m.memberId}</td>
                            <td>${m.name}</td>
                            <td>${m.email}</td>
                            <td>${m.phone}</td>
                            <td>${m.address ?? '尚未填寫'}</td>
                            <td>
                                <select class="form-select form-select-sm"  id="status-${m.memberId}" onchange="saveStatus('${m.email}',  this.value )">
                                    <option value="1" ${m.memberStatus === 1 ? "selected" : ""} class="text-success">啟用</option>
                                    <option value="0" ${m.memberStatus === 0 ? "selected" : ""} class="text-danger">停用</option>
                                </select>
                            </td>
                            <td>${m.registrationTime}</td>
                        </tr>`;
    });
    tableHtml += `</tbody></table>`;
    searchResult.innerHTML = tableHtml;
}

//--------------------管理員登出----------------------------------------
const logoutBtn = document.getElementById('logoutBtn');
logoutBtn.addEventListener('click', function (e) {
    e.preventDefault();
    fetch('api/adminLogout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(result => result.json())
        .then(result => {
            if (result.success) {

                Swal.fire({ icon: 'success', title: result.message, confirmButtonText: '確認' }).then(() => {
                    window.location.href = 'admin-login.html';
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire({ icon: 'error', title: '登出過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
        });
});