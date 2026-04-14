(function () {
  /**
   * [工具函式]
   */
  function qs(id) { return document.getElementById(id); }
  function getParam(key) { return new URLSearchParams(window.location.search).get(key); }
  
  function pick(obj, keys) {
    for (var i = 0; i < keys.length; i++) {
      var k = keys[i];
      if (obj && obj[k] !== undefined && obj[k] !== null) return obj[k];
    }
    return null;
  }

  function escapeHtml(str) {
    return String(str).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#39;');
  }

  function formatPrice(value) {
    var n = Number(value);
    return Number.isNaN(n) ? '$0' : '$' + n.toLocaleString('en-US');
  }

  function setText(el, text) { if (el) el.textContent = (text == null) ? '' : String(text); }
  function setHtml(el, html) { if (el) el.innerHTML = html; }

  function normalizeStatus(raw) {
    var s = String(raw || '').toUpperCase();
    return (s === '1') ? 'ON_SALE' : (s === '0' ? 'OFF_SALE' : s);
  }

  function stockLabel(stockQty, status) {
    var qty = Number(stockQty);
    if (normalizeStatus(status) !== 'ON_SALE') return '未上架';
    return (Number.isFinite(qty) && qty <= 0) ? '缺貨' : '有庫存';
  }

  /**
   * [資料轉換]
   */
  function mapProduct(p) {
    return {
      sku: pick(p, ['sku', 'SKU']),
      name: pick(p, ['productName', 'product_name', 'name']),
      price: pick(p, ['price', 'unitPrice', 'unit_price']),
      stock: pick(p, ['stockQuantity', 'stock_quantity', 'stock']),
      status: pick(p, ['status']),
      shortDesc: pick(p, ['shortDescription', 'short_description', 'brief']),
      desc: pick(p, ['description', 'desc', 'detail']),
      image: pick(p, ['imageUrl', 'image_url', 'image', 'img'])
    };
  }

  /**
   * [商品詳情載入]
   */
  async function load() {
    var skuParam = getParam('sku');
    var elName = qs('pName'), elPrice = qs('pPrice'), elSku = qs('pSku');
    var elStock = qs('pStockText'), elShort = qs('pShortDesc'), elDesc = qs('pDesc');

    if (!skuParam) {
      setText(elName, '查無商品');
      setHtml(elDesc, '<p>請使用 ?sku=商品編號</p>');
      return;
    }

    try {
      var resp = await fetch('product-detail?sku=' + encodeURIComponent(skuParam), {
        headers: { 'Accept': 'application/json' }
      });
      if (!resp.ok) throw new Error('HTTP ' + resp.status);

      var raw = await resp.json();
      var p = mapProduct(raw.data || raw);

      if (!p.sku) throw new Error('查無商品');

      setText(elName, p.name || '未命名商品');
      setText(elSku, p.sku);
      setText(elPrice, formatPrice(p.price));
      setText(elStock, stockLabel(p.stock, p.status));
      setText(elShort, p.shortDesc || '');
      setHtml(elDesc, '<p>' + escapeHtml(p.desc || '').replaceAll('\n', '<br>') + '</p>');

      document.title = (p.name ? p.name + ' | VitaTrack' : 'VitaTrack');
      
      bindAddToCart(p);
      await loadProductImages(skuParam);

    } catch (e) {
      console.error('載入失敗', e);
      setText(elName, '載入失敗');
    }
  }

  //**
 /* [相關商品載入與圖片渲染]
 */
async function loadRelatedProducts() {
    var skuParam = getParam('sku');
    var container = qs('relatedProductsContainer');
    if (!skuParam || !container) return;

    try {
        var url = 'product-related?sku=' + encodeURIComponent(skuParam);
        var resp = await fetch(url);
        var raw = await resp.json();
        var list = Array.isArray(raw) ? raw : (raw.data || []);

        // 只取前三筆
        const subList = list.slice(0, 3);
        
        // 渲染外殼
        container.innerHTML = `<div class="row w-100" id="relatedRow"></div>`;
        const row = qs('relatedRow');

        for (var item of subList) {
            var p = mapProduct(item);
            var sku = p.sku;
            
            // 先產生佔位 HTML (包含一個特定的 ID 方便稍後填入圖片)
            row.innerHTML += `
                <div class="col-md-4 col-sm-6 m-b-24">
                    <div class="mn-product-card text-center">
                        <div class="mn-img">
                            <a href="productDetailPage.html?sku=${sku}">
                                <img id="img-rel-${sku}" src="assets/img/product/default.jpg" alt="${escapeHtml(p.name)}">
                            </a>
                        </div>
                        <div class="mn-product-detail">
                            <h5 class="m-t-10"><a href="productDetailPage.html?sku=${sku}">${escapeHtml(p.name)}</a></h5>
                            <div class="mn-price-new" style="font-weight:bold; color:#5d69f4;">${formatPrice(p.price)}</div>
                        </div>
                    </div>
                </div>`;

            // 非同步抓取該 SKU 的圖片
            fetchImageForRelated(sku);
        }
    } catch (e) {
        console.error('相關商品載入失敗', e);
        container.innerHTML = '<p>無法取得相關商品</p>';
    }
}

/**
 * 針對特定 SKU 抓取圖片並替換
 */
async function fetchImageForRelated(sku) {
    try {
        // 依照你的要求，呼叫後端 API: product-images
        var resp = await fetch('product-images?sku=' + encodeURIComponent(sku));
        if (!resp.ok) return;
        var images = await resp.json();
        
        if (Array.isArray(images) && images.length > 0) {
            // 尋找 isMain 為 1 的圖片，或取第一張
            var main = images.find(img => img.is_main == 1 || img.isMain) || images[0];
            var imgEl = document.getElementById(`img-rel-${sku}`);
            if (imgEl && main.imageUrl) {
                imgEl.src = main.imageUrl;
            }
        }
    } catch (e) {
        console.warn(`SKU: ${sku} 圖片載入失敗`);
    }
}

  async function renderRelatedProducts(list) {
    var container = qs('relatedProductsContainer');
    var html = '';
    
    for (var item of list) {
      var p = mapProduct(item);
      var sku = encodeURIComponent(p.sku || '');
      html += `
        <div class="mn-product-card">
          <div class="mn-product-img">
            <a href="productDetailPage.html?sku=${sku}">
              <img src="${p.image || 'assets/img/product/default.jpg'}" alt="${escapeHtml(p.name)}">
            </a>
          </div>
          <div class="mn-product-detail">
            <h5><a href="productDetailPage.html?sku=${sku}">${escapeHtml(p.name || '商品名稱')}</a></h5>
            <div class="mn-price-new">${formatPrice(p.price)}</div>
          </div>
        </div>`;
    }
    container.innerHTML = html || '<p>目前沒有相關商品</p>';
    
    // 初始化 Owl Carousel (如果存在)
    // if (window.jQuery && jQuery.fn.owlCarousel) {
    //   var $container = jQuery(container);
    //   $container.owlCarousel({
    //     loop: false, margin: 20, nav: true, dots: false,
    //     responsive: { 0: { items: 1 }, 768: { items: 2 }, 992: { items: 3 } }
    //   });
    // }
  }

  async function loadProductImages(sku) {
    var imgEl = qs('productMainImg');
    if (!imgEl) return;
    try {
      var resp = await fetch('product-images?sku=' + encodeURIComponent(sku));
      if (!resp.ok) return;
      var images = await resp.json();
      if (Array.isArray(images) && images.length > 0) {
        var main = images.find(img => img.isMain) || images[0];
        imgEl.src = main.imageUrl;
      }
    } catch (e) { console.warn('圖片載入失敗'); }
  }

  /**
   * [購物車功能 - 依照要求保留 fetch 邏輯]
   */
function bindAddToCart(product) {
    var btn = document.querySelector('.mn-add-cart');
    var qtyInput = document.querySelector('.qty-input');
    if (!btn) return;

    btn.onclick = async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();
        var qty = Math.max(1, Number(qtyInput ? qtyInput.value : 1));

        // 不再自己 fetch，交給 CartStore 統一處理
        await window.CartStore.addToCart({ sku: product.sku, quantity: qty });
    };
}

  /**
   * [保留不更改的頁面初始化與驗證邏輯]
   */
  document.addEventListener('DOMContentLoaded', async function () {
    await load();
    await loadRelatedProducts();

    const guestHeader = document.getElementById("header-guest");
    const memberHeader = document.getElementById("header-member");
    const memberInfo = document.getElementById('memberInfo');
    const orderInfoBtn = document.getElementById('orderInfo');
    const logoutBtn = document.getElementById('logoutBtn');

    // 這裡預防元素不存在報錯
    if(memberInfo) memberInfo.addEventListener("click", info);
    
    //------------驗證是否會員決定首頁Header------------
    if (!guestHeader || !memberHeader) {
      console.error("Header element not found");
    } else {
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
    }

    function info(e) {
      e.preventDefault();
      window.location.href = "memberCenter.html";
    }

    if(orderInfoBtn) {
      orderInfoBtn.addEventListener('click', function (e) {
        e.preventDefault();
        window.location.href = "memberCenter.html";
      });
    }

    if(logoutBtn) {
      logoutBtn.addEventListener('click', function (e) {
        e.preventDefault();
        fetch('api/logout', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
        })
          .then(result => result.json())
          .then(result => {
            if (result.success) {
              Swal.fire({ icon: 'success', title: result.message, confirmButtonText: '確認' }).then(() => {
                window.location.href = 'index.html';
              });
            }
          })
          .catch(error => {
            console.error('Error:', error);
            Swal.fire({ icon: 'error', title: '登出過程中發生錯誤，請稍後再試。', confirmButtonText: '確認' });
          });
      });
    }
  });

})();