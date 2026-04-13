(function () {
  function qs(id) {
    return document.getElementById(id);
  }

  function getParam(key) {
    var params = new URLSearchParams(window.location.search);
    return params.get(key);
  }

  function pick(obj, keys) {
    for (var i = 0; i < keys.length; i++) {
      var k = keys[i];
      if (obj && obj[k] !== undefined && obj[k] !== null) return obj[k];
    }
    return null;
  }

  function escapeHtml(str) {
    return String(str)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function formatPrice(value) {
    var n = Number(value);
    if (Number.isNaN(n)) return '$0';
    return '$' + n.toLocaleString('en-US');
  }

  function setText(el, text) {
    if (!el) return;
    el.textContent = (text === null || text === undefined) ? '' : String(text);
  }

  function setHtml(el, html) {
    if (!el) return;
    el.innerHTML = html;
  }

  function normalizeStatus(raw) {
    if (raw === null || raw === undefined) return '';
    var s = String(raw).toUpperCase();
    if (s === '1') return 'ON_SALE';
    if (s === '0') return 'OFF_SALE';
    return s;
  }

  function stockLabel(stockQty, status) {
    var qty = Number(stockQty);
    var st = normalizeStatus(status);
    if (st && st !== 'ON_SALE') return '未上架';
    if (Number.isFinite(qty) && qty <= 0) return '缺貨';
    return '有庫存';
  }

  function mapProduct(p) {
    return {
      sku: pick(p, ['sku', 'SKU']),
      name: pick(p, ['productName', 'product_name', 'name']),
      price: pick(p, ['price', 'unitPrice', 'unit_price']),
      size: pick(p, ['size']),
      stock: pick(p, ['stockQuantity', 'stock_quantity', 'stock']),
      status: pick(p, ['status']),
      shortDesc: pick(p, ['shortDescription', 'short_description', 'brief', 'summary']),
      desc: pick(p, ['description', 'desc', 'detail', 'content']),
      image: pick(p, ['imageUrl', 'image_url', 'image', 'img']),
      category: pick(p, ['categoryDesc', 'category_desc', 'categoryName', 'category_name'])
    };
  }

  async function load() {
    var skuParam = getParam('sku');

    console.log('目前網址 sku =', skuParam);

    var elName = qs('pName');
    var elPrice = qs('pPrice');
    var elSku = qs('pSku');
    var elStock = qs('pStockText');
    var elShort = qs('pShortDesc');
    var elDesc = qs('pDesc');

    if (!skuParam) {
      setText(elName, '查無商品');
      setText(elStock, '缺少 sku');
      setHtml(elDesc, '<p>請使用 ?sku=商品編號</p>');
      return;
    }

    try {
      var url = 'product-detail?sku=' + encodeURIComponent(skuParam);
      console.log('呼叫 API =', url);

      var resp = await fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
      });

      console.log('HTTP 狀態 =', resp.status);

      if (!resp.ok) throw new Error('HTTP ' + resp.status);

      var raw = await resp.json();
      console.log('原始資料 raw =', raw);

      var data = raw.data ? raw.data : raw;

      var p = mapProduct(data);
      console.log('轉換後 product =', p);

      if (!p || !p.sku) {
        throw new Error('資料格式錯誤或查無商品');
      }

      setText(elName, p.name || '未命名商品');
      setText(elSku, p.sku || '');
      setText(elPrice, formatPrice(p.price));
      setText(elStock, stockLabel(p.stock, p.status));
      setText(elShort, p.shortDesc || '');

      var safe = escapeHtml(p.desc || '').replaceAll('\n', '<br>');
      setHtml(elDesc, '<p>' + safe + '</p>');

      document.title = (p.name ? p.name + ' | VitaTrack' : 'VitaTrack');
      bindAddToCart(p);
      await loadProductImages(skuParam);

    } catch (e) {
      console.error('載入失敗 =', e);
      setText(elName, '載入失敗');
      setText(elStock, '請確認 API /product-detail');
      setHtml(elDesc, '<p>無法取得商品資料</p>');
    }
  }

  function buildRelatedCard(p, imageUrl) {
    var img = imageUrl || 'assets/img/product/5.jpg';
    var name = escapeHtml(p.name || '商品名稱');
    var price = formatPrice(p.price);
    var sku = encodeURIComponent(p.sku || '');

    return `
	    <div class="mn-product-card">
	      <div class="mn-product-img">
	        <div class="mn-img">
	          <a href="productDetailPage.html?sku=${sku}" class="image">
	            <img class="main-img" src="${img}" alt="${name}">
	          </a>
	        </div>
	      </div>
	      <div class="mn-product-detail">
	        <h5>
	          <a href="productDetailPage.html?sku=${sku}">${name}</a>
	        </h5>
	        <div class="mn-price">
	          <div class="mn-price-new">${price}</div>
	        </div>
	      </div>
	    </div>
	  `;
  }

  async function fetchRelatedImage(sku) {
    if (!sku) return null;
    try {
      var resp = await fetch('product-images?sku=' + encodeURIComponent(sku), {
        headers: { 'Accept': 'application/json' }
      });
      if (!resp.ok) return null;
      var images = await resp.json();
      if (!Array.isArray(images) || images.length === 0) return null;
      var main = images.find(function (img) { return img.isMain === true; }) || images[0];
      return (main && main.imageUrl) ? main.imageUrl : null;
    } catch (e) {
      console.warn('[fetchRelatedImage] sku=' + sku, e);
      return null;
    }
  }

  async function renderRelatedProducts(list) {
    var container = qs('relatedProductsContainer');
    if (!container) return;

    if (!Array.isArray(list) || list.length === 0) {
      container.innerHTML = '<p>目前沒有相關商品</p>';
      return;
    }

    var products = list.map(function (item) { return mapProduct(item); });

    // 並行呼叫每個 SKU 的圖片 API
    var imageUrls = await Promise.all(
      products.map(function (p) { return fetchRelatedImage(p.sku); })
    );

    var html = '';
    for (var i = 0; i < products.length; i++) {
      html += buildRelatedCard(products[i], imageUrls[i]);
    }

    container.innerHTML = html;

    if (window.jQuery && jQuery.fn.owlCarousel) {
      var $container = jQuery(container);

      if ($container.hasClass('owl-loaded')) {
        $container.trigger('destroy.owl.carousel');
        $container.removeClass('owl-loaded');
        $container.find('.owl-stage-outer').children().unwrap();
      }

      $container.owlCarousel({
        loop: false,
        margin: 20,
        nav: true,
        dots: false,
        responsive: {
          0: { items: 1 },
          768: { items: 2 },
          992: { items: Number(container.dataset.count || 3) }
        }
      });
    }
  }

  async function loadProductImages(sku) {
    var imgEl = document.getElementById('productMainImg');
    if (!imgEl || !sku) return;

    try {
      var resp = await fetch('product-images?sku=' + encodeURIComponent(sku), {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
      });

      // 404 = 無圖片資料，保留預設圖即可
      if (resp.status === 404) {
        console.warn('[product-images] 查無圖片，使用預設圖');
        return;
      }

      if (!resp.ok) throw new Error('HTTP ' + resp.status);

      var images = await resp.json();
      console.log('[product-images] 回傳圖片清單 =', images);

      if (!Array.isArray(images) || images.length === 0) return;

      // 優先顯示主圖（isMain === true），沒有則取第一張
      var main = images.find(function (img) { return img.isMain === true; }) || images[0];

      if (main && main.imageUrl) {
        imgEl.src = main.imageUrl;
        imgEl.alt = main.imageUrl.split('/').pop().replace(/\.[^.]+$/, '');
        imgEl.style.visibility = 'visible';
        console.log('[product-images] 主圖已更新 =', main.imageUrl);
      }

    } catch (e) {
      console.error('[product-images] 載入失敗 =', e);
      // 載入失敗時保留預設圖，不顯示錯誤給使用者
    }
  }

  async function loadRelatedProducts() {
    var skuParam = getParam('sku');
    var container = qs('relatedProductsContainer');

    if (!skuParam || !container) return;

    var fixedSkus = [
      '202603011284', // 維他命C 1000mg
      '202603012843', // 維生素B群
      '202603014519', // Omega-3 魚油
      '202603016140', // 益生菌 50億
      '202603013250'  // 維骨力
    ];

    // 排除目前商品，剩下三個
    var relatedSkus = fixedSkus.filter(function (sku) {
      return sku !== skuParam;
    });

    if (relatedSkus.length === 0) {
      container.innerHTML = '<p>目前沒有相關商品</p>';
      return;
    }

    try {
      var url = 'product-related?skus=' + encodeURIComponent(relatedSkus.join(','));
      console.log('呼叫相關商品 API =', url);

      var resp = await fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
      });

      if (!resp.ok) throw new Error('HTTP ' + resp.status);

      var raw = await resp.json();
      console.log('相關商品 raw =', raw);

      var list = Array.isArray(raw) ? raw : (raw.data || []);
      await renderRelatedProducts(list);

    } catch (e) {
      console.error('相關商品載入失敗 =', e);
      container.innerHTML = '<p>無法取得相關商品</p>';
    }
  }

  document.addEventListener('DOMContentLoaded', async function () {
    await load();
    await loadRelatedProducts();
    const guestHeader = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfo = document.getElementById('memberInfo');
    const orderInfoBtn = document.getElementById('orderInfo');
    const logoutBtn = document.getElementById('logoutBtn');

    memberInfo.addEventListener("click", info);
    orderInfoBtn.addEventListener("click", (e) => orderInfo(e));

    //auth-check
    //------------驗證是否會員決定首頁Header------------
    if (!guestHeader || !memberHeader) {
      console.error("Header element not found");
      return;
    }

    // 加一個判斷，確保抓到元素才執行
    fetch('api/loginCheck')
      .then(response => response.json())
      .then(data => {
        if (data.loggedIn) {
          guestHeader.style.display = "none";
          memberHeader.style.display = "block";
        } else {
          guestHeader.style.display = "block";
          memberHeader.style.display = "none";
        }
      });
    //------------查看會員資料------------
    function info(e) {
      e.preventDefault();
      window.location.href = "memberCenter.html";

    };

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

  function bindAddToCart(product) {
    var btn = document.querySelector('.mn-add-cart');
    var qtyInput = document.querySelector('.qty-input');

    if (!btn) return;

    btn.onclick = async function (e) {
      e.preventDefault();
      e.stopImmediatePropagation();

      var qty = 1;

      if (qtyInput) {
        qty = Number(qtyInput.value || 1);
        if (!Number.isFinite(qty) || qty <= 0) {
          qty = 1;
        }
      }

      // -------------呼叫後端API將商品加入資料庫購物車-------------
      try {
        const response = await fetch('api/addToCart', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            sku: product.sku,
            quantity: qty
          })
        });

        const result = await response.json();

        if (result.success) {
          alert('加入購物車成功！');
          // 更新 badge（如果有）
          if (window.CartStore && typeof window.CartStore.updateCartBadge === "function") {
            window.CartStore.updateCartBadge();
          }
        } else {
          alert('加入購物車失敗：' + (result.message || '請先登入會員'));
          if (result.message === '請先登入會員!') {
            window.location.href = 'login.html';
          }
        }
      } catch (err) {
        console.error('發生錯誤:', err);
        alert('加入購物車發生異常，請確認已登入！');
      }
    };
  }

})();

// 以下主要負責商品詳細頁的資料載入、相關商品顯示，以及加入購物車的功能。它會在 DOMContentLoaded 時執行，確保頁面元素已經載入完成。
// 主要功能包括：
// 1. 從 URL 參數取得商品 SKU，呼叫後端 API 獲取商品詳細資料，並顯示在頁面上。
// 2. 呼叫後端 API 獲取相關商品列表，並使用 Owl Carousel 顯示在頁面下方。
// 3. 綁定加入購物車按鈕的事件，當使用者點擊時，將商品加入購物車並更新購物車徽章數量。
// 4. 驗證使用者是否為會員，根據登入狀態顯示不同的頁首選單，並提供查看會員資料、訂單資訊和登出功能。

(function () {
  async function updateCartBadge() {
    try {
      const resp = await fetch('api/getCartItem');
      if (!resp.ok) return;
      const result = await resp.json();
      const cartRows = result.data || [];

      let total = 0;
      if (Array.isArray(cartRows)) {
        for (let i = 0; i < cartRows.length; i++) {
          total += Number(cartRows[i].quantity || 0);
        }
      }

      // Update all badges on the page (both header-guest and header-member)
      let badges = document.querySelectorAll('#cartBadge, .mn-main-cart .cart-count, .mn-main-cart .label');
      badges.forEach(function (badge) {
        badge.textContent = total;
        badge.style.display = total > 0 ? 'inline-flex' : 'none';
      });
    } catch (e) {
      console.error('更新購物車標籤失敗', e);
    }
  }

  window.CartStore = {
    updateCartBadge: updateCartBadge
  };

  document.addEventListener('DOMContentLoaded', function () {
    updateCartBadge();
  });

})();