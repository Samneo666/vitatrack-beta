package web.checkout.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.cart.dao.CartDao;
import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.service.CheckoutService;
import web.checkout.vo.CartRow;
import web.checkout.vo.CheckoutResult;
import web.checkout.vo.OrderItem;
import web.checkout.vo.Orders;

/**
 * 結帳服務實作：處理購物車結帳、建立訂單與訂單明細。
 */
@Service
public class CheckoutServiceImpl implements CheckoutService {

	@Autowired
	private CartDao cartDao;

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private OrderItemDao orderItemDao;

	/**
	 * 執行結帳流程：查購物車 → 計算總金額 → 建立訂單與訂單明細 → 綁定購物車。
	 *
	 * @param memberId 會員編號
	 * @return 結帳結果（含訂單編號、總金額、付款狀態）
	 * @throws RuntimeException 若購物車為空或購物車綁定訂單失敗
	 */
	@Override
	@Transactional
	public CheckoutResult checkout(int memberId) {

		// 查詢該會員的未結帳購物車
		List<CartRow> cartRows = cartDao.findOpenCartByMemberId(memberId);
		if (cartRows == null || cartRows.isEmpty()) {
			throw new RuntimeException("Cart is empty.");
		}

		// 累加各品項小計得出總金額
		BigDecimal totalAmount = BigDecimal.valueOf(0);
		for (CartRow row : cartRows) {
			BigDecimal quantityBD = BigDecimal.valueOf(row.getQuantity());
			totalAmount = totalAmount.add(row.getUnitPrice().multiply(quantityBD));
		}
		int totalAmountInt = totalAmount.intValue();

		// 建立訂單，初始付款狀態為 PENDING
		Orders order = new Orders();
		order.setMemberId(memberId);
		order.setTotalAmount(totalAmount);
		order.setPaymentStatus("PENDING");
		order.setPaymentMethod("ECPAY");
		order.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		order.setAmount(totalAmount);
		order.setTransactionId("TXN" + System.currentTimeMillis());
		order.setPaymentTime(new java.sql.Timestamp(System.currentTimeMillis()));
		orderDao.save(order);

		// 依購物車內容建立各筆訂單明細
		Integer orderId = order.getOrderId();
		for (CartRow row : cartRows) {
			OrderItem oi = new OrderItem();
			oi.setOrder(order);
			oi.setSku(row.getSku());
			oi.setProductName(row.getProductName());
			oi.setUnitPrice(row.getUnitPrice());
			oi.setQuantity(row.getQuantity());
			// 計算小計：單價 × 數量
			BigDecimal subtotal = row.getUnitPrice().multiply(BigDecimal.valueOf(row.getQuantity()));
			oi.setSubtotal(subtotal);
			orderItemDao.save(oi);
		}

		
		List<String> skus = cartRows.stream()
			    .map(CartRow::getSku)
			    .collect(java.util.stream.Collectors.toList());
		
		int deletedCount = cartDao.deleteCartItems(memberId, skus);
		if (deletedCount <= 0) {
		    throw new RuntimeException("deleteCartItems deleted 0 rows.");
		}
		return new CheckoutResult(orderId, totalAmountInt, "PENDING");
	}

	/**
	 * 查詢指定會員的未結帳購物車，供結帳頁面顯示商品清單。
	 *
	 * @param memberId 會員編號
	 * @return 購物車明細清單
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CartRow> getCheckoutCart(int memberId) {
		return cartDao.findOpenCartByMemberId(memberId);
	}
}
