(function () {
  // 更新購物車圖示數量 (從後端獲取)
  async function updateCartBadge() {
    try {
      const resp = await fetch('api/getCartItem');
      if (!resp.ok) return;
      
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
  function renderBadge(count) {
    let badges = document.querySelectorAll('#cartBadge, .mn-main-cart .cart-count, .mn-main-cart .label');
    badges.forEach(function(badge) {
      badge.textContent = count;
      badge.style.display = count > 0 ? 'inline-flex' : 'none';
    });
  }

  // 加入購物車 
  async function addToCart(product) {
    if (!product || !product.sku) return;

    try {
      // 呼叫後端 API 新增商品到資料庫
      const resp = await fetch('api/addToCart', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sku: product.sku,
          quantity: product.quantity || 1
        })
      });

      if (resp.ok) {
        console.log('成功加入後端資料庫');
        // 加入成功後，重新整理 Badge 數量
        await updateCartBadge();
      }
    } catch (e) {
      console.error('加入購物車失敗', e);
      alert('無法加入購物車，請檢查網路連線或登入狀態');
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