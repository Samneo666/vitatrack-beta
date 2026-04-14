(function () {
  // 更新購物車圖示數量 (從後端獲取)
  async function updateCartBadge() {
    try {
      const resp = await fetch('api/getCartItem');
      if (resp.status === 401) {
            renderBadge(0); // 未登入，badge 顯示 0，不報錯
            return [];
        }
      
      const result = await resp.json();
      const cartRows = result.data || [];
      
      let total = 0;
      cartRows.forEach(item => {
        total += Number(item.quantity || 0);
      });

      renderBadge(total);
      return cartRows; // 回傳給其他函式使用
    } catch (e) {
      console.error('同步購物車失敗', e);
      renderBadge(0);
    }
  }

  // 渲染畫面的輔助函式
  // 修改 addcart.js 中的 renderBadge 函式
function renderBadge(count) {
  // 同時更新所有可能出現 count 的地方
  let badges = document.querySelectorAll('.cart-count, .label.lbl-1');
  badges.forEach(function(badge) {
    badge.textContent = count;
    // 確保父層如果是隱藏的，也要考慮進去，或者統一顯示
    badge.style.display = count > 0 ? 'inline-flex' : 'none';
  });
}

  // 加入購物車 
async function addToCart(product) {
    if (!product || !product.sku) return;

    try {
        const resp = await fetch('api/addToCart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sku: product.sku, quantity: product.quantity || 1 })
        });

        const result = await resp.json();

        if (result.success) {
            Swal.fire({ icon: 'success', title: '已加入購物車', timer: 1000, showConfirmButton: false });
            await updateCartBadge(); // ← badge 在這裡更新
        } else {
            Swal.fire({ icon: 'error', title: result.message || '請先登入會員', confirmButtonText: '確認' });
            if (result.message === '請先登入會員!') {
                window.location.href = 'login.html';
            }
        }
    } catch (e) {
        console.error('加入購物車失敗', e);
        Swal.fire({ icon: 'error', title: '加入購物車發生異常，請確認已登入！', confirmButtonText: '確認' });
    }
}

  // 暴露 API 給外部使用
  window.CartStore = {
    addToCart: addToCart,
    updateCartBadge: updateCartBadge,
    // 獲取目前購物車清單 (非同步)
    getCart: async function() {
        try {
            const resp = await fetch('api/getCartItem');
            if (!resp.ok) return [];
            const result = await resp.json();
            return result.data || [];
        } catch (e) {
            console.error('取得購物車失敗', e);
            return [];
        }
    }
  };

  document.addEventListener('DOMContentLoaded', updateCartBadge);
})();