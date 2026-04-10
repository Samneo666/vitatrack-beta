document.addEventListener('DOMContentLoaded', function () {
  // 依你的專案名稱調整：/vitatrack
  var CONTEXT_PATH = '/vitatrack';

  var tbody = document.getElementById('productTbody');

  // ===== 新增彈窗（dialog） =====
  var addDialog = document.getElementById('addDialog');
  var btnOpenAdd = document.getElementById('btnOpenAdd');
  var btnCloseAdd = document.getElementById('btnCloseAdd');
  var btnSaveAdd = document.getElementById('btnSaveAdd');

  var aSku = document.getElementById('aSku');
  var aProductName = document.getElementById('aProductName');
  var aPrice = document.getElementById('aPrice');
  var aSize = document.getElementById('aSize');
  var aStock = document.getElementById('aStock');
  var aDescription = document.getElementById('aDescription');
  var aShortDescription = document.getElementById('aShortDescription');
  var aStatus = document.getElementById('aStatus');
  var aCategoryId = document.getElementById('aCategoryId');

  function openAddDialog() {
    if (!addDialog) {
      alert('找不到 addDialog，請確認 HTML 有 <dialog id="addDialog">');
      return;
    }
    addDialog.showModal();
  }

  function closeAddDialog() {
    if (addDialog && addDialog.open) addDialog.close();
  }

  if (btnOpenAdd) btnOpenAdd.addEventListener('click', openAddDialog);
  if (btnCloseAdd) btnCloseAdd.addEventListener('click', closeAddDialog);

  if (btnSaveAdd) {
    btnSaveAdd.addEventListener('click', function () {
      // 送 JSON 給 doPost
      var payload = {
        categoryId: aCategoryId ? Number(aCategoryId.value) : null,
        productName: aProductName ? aProductName.value.trim() : '',
        price: aPrice ? Number(aPrice.value) : null,
        size: aSize ? aSize.value.trim() : '',
        stockQuantity: aStock ? Number(aStock.value) : null,
        description: aDescription ? aDescription.value : '',
        shortDescription: aShortDescription ? aShortDescription.value : '',
        status: aStatus ? aStatus.value : null,
        createdByAdminId: 1 
      };

      fetch(CONTEXT_PATH + '/product-add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json; charset=UTF-8' },
        body: JSON.stringify(payload)
      })
        .then(function (resp) {
          return resp.json().catch(function () {
            throw new Error('新增回傳不是 JSON');
          });
        })
        .then(function (json) {
          var ok = (typeof json === 'boolean') ? json : (json && json.success);
          if (ok) {
            closeAddDialog();
            loadProducts();
			alert('新增成功');
          } else {
            alert('新增失敗' + (json && json.message ? '：' + json.message : ''));
          }
        })
        .catch(function (err) {
          console.error(err);
          alert('新增失敗，請看 Console');
        });
    });
  }

  // ===== 編輯彈窗 =====
  var currentEditingSku = null;

  var editModal = document.getElementById('editModal');
  var btnCloseEdit = document.getElementById('btnCloseEdit');
  var btnSaveEdit = document.getElementById('btnSaveEdit');

  var mSku = document.getElementById('mSku');
  var mStatus = document.getElementById('mStatus');
  var mCreatedBy = document.getElementById('mCreatedBy');
  var mUpdatedBy = document.getElementById('mUpdatedBy');

  var mProductName = document.getElementById('mProductName');
  var mPrice = document.getElementById('mPrice');
  var mSize = document.getElementById('mSize');
  var mStock = document.getElementById('mStock');
  var mDescription = document.getElementById('mDescription');
  var mShortDescription = document.getElementById('mShortDescription');

  function openEditModal(product) {
    if (!editModal) {
      alert('找不到 editModal，請確認 HTML 有 <div id="editModal">');
      return;
    }

    currentEditingSku = product.sku;

    // 不可編輯欄位
    if (mSku) mSku.value = product.sku == null ? '' : product.sku;
    if (mCreatedBy) mCreatedBy.value = product.createdByAdminId == null ? '' : product.createdByAdminId;
    if (mUpdatedBy) mUpdatedBy.value = product.updatedByAdminId == null ? '' : product.updatedByAdminId;

    // 狀態
    if (mStatus) {
      mStatus.value = product.status == null ? (mStatus.options[0] ? mStatus.options[0].value : '') : product.status;
    }

    // 可編輯欄位
    if (mProductName) mProductName.value = product.productName == null ? '' : product.productName;
    if (mPrice) mPrice.value = product.price == null ? '' : product.price;
    if (mSize) mSize.value = product.size == null ? '' : product.size;
    if (mStock) mStock.value = product.stockQuantity == null ? '' : product.stockQuantity;
    if (mDescription) mDescription.value = product.description == null ? '' : product.description;
    if (mShortDescription) mShortDescription.value = product.shortDescription == null ? '' : product.shortDescription;

    editModal.style.display = 'block';
  }

  function closeEditModal() {
    if (editModal) editModal.style.display = 'none';
    currentEditingSku = null;
  }

  if (btnCloseEdit) btnCloseEdit.addEventListener('click', closeEditModal);

  if (btnSaveEdit) {
    btnSaveEdit.addEventListener('click', function () {
      if (currentEditingSku == null) {
        alert('找不到要編輯的商品 SKU');
        return;
      }

      var payload = {
        sku: currentEditingSku,
        productName: mProductName ? mProductName.value.trim() : null,
        price: mPrice ? Number(mPrice.value) : null,
        size: mSize ? mSize.value.trim() : null,
        stockQuantity: mStock ? Number(mStock.value) : null,
        description: mDescription ? mDescription.value : null,
        shortDescription: mShortDescription ? mShortDescription.value : null,
        status: mStatus ? mStatus.value : null
      };

      fetch(CONTEXT_PATH + '/product-update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json; charset=UTF-8' },
        body: JSON.stringify(payload)
      })
        .then(function (resp) {
          return resp.json().catch(function () {
            throw new Error('更新回傳不是 JSON');
          });
        })
        .then(function (json) {
          if (json && json.success) {
            closeEditModal();
            loadProducts();
			alert('更新成功');
          } else {
            alert('更新失敗' + (json && json.message ? '：' + json.message : ''));
          }
        })
        .catch(function (err) {
          console.error(err);
          alert('更新失敗，請看 Console');
        });
    });
  }

  // ===== 列表 =====
  function createTd(text) {
    var td = document.createElement('td');
    td.textContent = text == null ? '' : text;
    return td;
  }

  function renderProducts(list) {
    tbody.innerHTML = '';

    for (var i = 0; i < list.length; i++) {
      var product = list[i];
      var tr = document.createElement('tr');

      tr.appendChild(createTd(product.sku));
      tr.appendChild(createTd(product.productName));
      tr.appendChild(createTd(product.price));
      tr.appendChild(createTd(product.size));
      tr.appendChild(createTd(product.stockQuantity));
      tr.appendChild(createTd(product.description));
      tr.appendChild(createTd(product.shortDescription));
      tr.appendChild(createTd(product.status));
      tr.appendChild(createTd(product.createdByAdminId));
      tr.appendChild(createTd(product.updatedByAdminId));

      // 操作欄：編輯 / 刪除
      var tdAction = document.createElement('td');

      var btnEdit = document.createElement('button');
      btnEdit.type = 'button';
      btnEdit.textContent = '編輯';
      btnEdit.addEventListener('click', (function (p) {
        return function () {
          openEditModal(p);
        };
      })(product));

      var btnDelete = document.createElement('button');
      btnDelete.type = 'button';
      btnDelete.textContent = '刪除';
      btnDelete.addEventListener('click', (function (p) {
        return function () {
          if (!confirm('確定刪除' + p.productName + ' ?')) return;

          fetch(CONTEXT_PATH + '/product-delete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify({ sku: p.sku })
          })
            .then(function (resp) {
              return resp.json().catch(function () {
                throw new Error('刪除回傳不是 JSON');
              });
            })
            .then(function (json) {
              if (json && json.success) {
                loadProducts();
              } else {
                alert('刪除失敗' + (json && json.message ? '：' + json.message : ''));
              }
            })
            .catch(function (err) {
              console.error(err);
              alert('刪除失敗，請看 Console');
            });
        };
      })(product));

      tdAction.appendChild(btnEdit);
      tdAction.appendChild(document.createTextNode(' '));
      tdAction.appendChild(btnDelete);

      tr.appendChild(tdAction);
      tbody.appendChild(tr);
    }
  }

  function loadProducts() {
    fetch(CONTEXT_PATH + '/product-list')
      .then(function (resp) {
        return resp.json();
      })
      .then(function (data) {
        if (!Array.isArray(data)) {
          console.error('product-list 回傳不是 array：', data);
          alert('載入商品清單失敗（回傳格式不符）');
          return;
        }
        renderProducts(data);
      })
      .catch(function (err) {
        console.error(err);
        alert('載入商品清單失敗');
      });
  }

  loadProducts();
});
