document.addEventListener("DOMContentLoaded", function () {
  const guestHeader = document.getElementById("header-guest");
  const memberHeader = document.getElementById("header-member");
  const logoutBtn = document.getElementById("logoutBtn");
  const orderInfoBtn = document.getElementById('orderInfo');



  //------------查看訂單------------
  orderInfoBtn.addEventListener('click', function (e) {
    e.preventDefault();
    window.location.href = "memberCenter.html";
  });
  //------------會員登出------------
  logoutBtn.addEventListener('click', function (e) {
    e.preventDefault();
    fetch('api/logout', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
      .then(response => response.json())
      .then(result => {
        if (result.success) {
          alert(result.message);
          window.location.href = 'index.html';
        }
        else {
          alert(result.message);
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('登出失敗，請稍後再試');
      });
  });

  //auth-check
  //------------驗證是否會員決定首頁Header------------
  if (!guestHeader || !memberHeader) {
    console.error("Header element not found");
    return;
  }

  fetch('api/loginCheck')
    .then(response => response.json())
    .then(data => {
      if (data.loggedIn) {
        guestHeader.style.display = "none";
        memberHeader.style.display = "block";
      }
      else {
        guestHeader.style.display = "block";
        memberHeader.style.display = "none";
      }
    });

  renderCartPage();

});


async function getCart() {
  const resp = await fetch("api/getCartItem", {
    cache: 'no-store' 
  });
  return await resp.json();


}

function saveCart(cart) {
  localStorage.setItem("cart", JSON.stringify(cart));
}

function formatPrice(value) {
  return "NT$" + Number(value || 0).toLocaleString("zh-TW");
}

//------------移除購物車商品------------
async function removeItem(sku) {
  if (!confirm("確定要移除此商品嗎？")) return;

  try {
    const response = await fetch('api/removeCartItem', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ skus: [sku] })
    });

    // 檢查 HTTP 狀態碼 
    if (!response.ok) {
      throw new Error(`HTTP 錯誤！狀態碼：${response.status}`);
    }
    const result = await response.json();
    if (result.success) {
      renderCartPage();
      if (window.CartStore && typeof window.CartStore.updateCartBadge === "function") {
        window.CartStore.updateCartBadge();
      }
    } else {
      alert("移除失敗：" + result.message);
    }
  } catch (err) {
    console.error('Remove error:', err);
  }
}
//------------渲染購物車頁面-------------
async function renderCartPage() {
  var cart = await getCart();
  var container = document.getElementById("cartList");
  var totalEl = document.getElementById("cartTotal");

  if (!container) return;

  if (cart.length === 0) {
    container.innerHTML = `
      <tr>
        <td colspan="5" style="text-align:center; padding:30px;">購物車是空的</td>
      </tr>
    `;
    if (totalEl) totalEl.textContent = "NT$0";
    return;
  }

  var html = "";
  var total = 0;

  for (var i = 0; i < cart.length; i++) {
    var item = cart[i];
    var quantity = Number(item.quantity || 1);
    var price = Number(item.unitPrice || 0);
    var subtotal = quantity * price;
    total += subtotal;

    html += `
      <tr class="mn-cart-product">
        <td data-label="Product" class="mn-cart-pro-name">
          <a href="product-detail.html?sku=${item.sku || ''}">
            <img class="mn-cart-pro-img" src="${item.image || 'assets/img/product/default.jpg'}" alt="">
            ${item.name || item.productName || ''}
          </a>
        </td>
        <td data-label="Price" class="mn-cart-pro-price">
          <span class="amount">${formatPrice(price)}</span>
        </td>
        <td data-label="Quantity" class="mn-cart-pro-qty" style="text-align:center;">
          <div class="cart-qty-box">
            <button type="button" onclick="changeQty('${item.sku}', -1)">-</button>
            <input type="text" value="${quantity}" readonly style="width:60px; text-align:center;">
            <button type="button" onclick="changeQty('${item.sku}', 1)">+</button>
          </div>
        </td>
        <td data-label="Total" class="mn-cart-pro-subtotal">
          ${formatPrice(subtotal)}
        </td>
        <td data-label="Remove" class="mn-cart-pro-remove">
          <a href="javascript:void(0)" onclick="removeItem('${item.sku}')">
            <i class="ri-delete-bin-line"></i>
          </a>
        </td>
      </tr>
    `;
  }

  container.innerHTML = html;

  if (totalEl) {
    totalEl.textContent = formatPrice(total);
  }
}



//-------------改變商品數量 (增減數量)-------------
async function changeQty(sku, delta) {
  // 先取得目前數量
  var cart = await getCart();
  var newQuantity = 1;
  for (var i = 0; i < cart.length; i++) {
    if (cart[i].sku === sku) {
      newQuantity = Number(cart[i].quantity || 1) + delta;
      break;
    }
  }

  // 數量少於等於零，等同刪除
  if (newQuantity <= 0) {
    removeItem(sku);
    return;
  }

  // 發送更新請求
  const data = { sku: sku, quantity: newQuantity };
  try {
    const response = await fetch('api/updateCartItem', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });

    const result = await response.json();
    if (result.success) {
      renderCartPage();
    } else {
      alert("更新商品數量失敗：" + result.message);
    }
  } catch (err) {
    console.error('Update quantity error:', err);
  }
}

async function checkoutNow() {
  var cart = await getCart();

  if (!cart.length) {
    alert("購物車是空的");
    return;
  }
  // var payloadItems = cart.map(function (item) {
  //   return {
  //     sku: item.sku,
  //     productName: item.productName || item.name || "",
  //     price: Number(item.price || 0),
  //     quantity: Number(item.quantity || item.qty || 1)
  //   };
  // });

  // fetch("/vitatrack/cart-item/checkout", {
  //   method: "POST",
  //   headers: {
  //     "Content-Type": "application/json"
  //   },
  //   body: JSON.stringify({
  //     items: payloadItems
  //   })
  // })
  //   .then(function (response) {
  //     return response.json();
  //   })
  //   .then(function (result) {
  //     if (result.success) {
  //       localStorage.removeItem("cart");

  //       if (window.CartStore && typeof window.CartStore.updateCartBadge === "function") {
  //         window.CartStore.updateCartBadge();
  //       }

  //       window.location.href = "checkoutPage.html";
  //     } else {
  //       alert(result.message || "結帳失敗");
  //     }
  //   })
  //   .catch(function (error) {
  //     console.error(error);
  //     alert("系統錯誤，請稍後再試");
  //   });
  //-------------------------------------------------------
  // 購物車資料在資料庫隨時保持最新，這裡不需要再寫回一次
  // 直接切換到結帳頁面即可
  window.location.href = "checkoutPage.html";
}

