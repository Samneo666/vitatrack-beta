let currentProductId;
let modalInstance;

function openAddToCart(productId) {
	
	currentProductId = productId;

	// 載入 modal HTML（相對於 productTest.html）
	fetch("./addToCart.html")

		.then(res => {
			if (!res.ok) throw new Error("modal html 載入失敗")
			return res.text();
		})
		
		.then(html => {

			//把Html塞到DOM（抓modalContainer）
			document.getElementById("modalContainer").innerHTML = html;

			// 顯示 modal
			const modalEl = document.getElementById("addToCartModal");
			modalInstance = new bootstrap.Modal(modalEl);
			modalInstance.show();

			// 傳入商品Id請求該商品的庫存
			return fetch(`/vitatrack/api/product/stock?productId=${productId}`);
		})

		.then(res => {
			if (!res.ok) throw new Error("stock api failed");
			return res.json();
		})
		.then(data => {
			document.getElementById("stockQty").innerText = data.stock;
		})
	.catch(err => {
		console.error("openAddToCart error:", err);
		Swal.fire({ icon: 'error', title: '加入購物車視窗載入失敗', confirmButtonText: '確認' });
	});
}

function changeQty(diff) {
	const input = document.getElementById("qtyInput");
	let val = parseInt(input.value, 10) + diff;
	if (val < 1) val = 1;
	input.value = val;
}

function confirmAddToCart() {
	const qty = document.getElementById("qtyInput").value;

		fetch("/vitatrack/api/cart/add", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				productId: currentProductId,
				qty: parseInt(qty)
			})
		})
		.then(res => {
			if (res.ok) {
				Swal.fire({ icon: 'success', title: '已加入購物車', confirmButtonText: '確認' });
				modalInstance.hide();
			} else {
				Swal.fire({ icon: 'error', title: '加入失敗', confirmButtonText: '確認' });
			}
		})
		.catch(err => {
			console.error("add to cart error:", err);
			Swal.fire({ icon: 'error', title: '加入購物車失敗', confirmButtonText: '確認' });
		});
}
