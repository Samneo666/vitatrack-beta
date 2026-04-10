package web.product.service.impl;



import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import core.exception.BusinessException;
import web.checkout.dao.CartDao;
import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.vo.CartRow;
import web.checkout.vo.OrderItem;
import web.checkout.vo.Orders;
import web.product.dto.OrderCreationResponse;
import web.product.service.PlaceOrderService;




@Service
public class PlaceOrderServiceImpl implements PlaceOrderService{

	@Autowired
	CartDao cartDao;
	
	@Autowired
	OrderDao orderDao;
	
	@Autowired
	OrderItemDao orderItemDao;
	
	
	
	
	@Override
	@Transactional
	public OrderCreationResponse checkout(int memberId) {
		List<CartRow> cartRows = cartDao.findOpenCartByMemberId(memberId);
		// 1.1如果查不到，顯示Cart is empty.
		if (cartRows == null || cartRows.isEmpty()) {
			throw new BusinessException("購物車是空的!");
		}

		// 2.計算總金額
		BigDecimal totalAmount;
		totalAmount = BigDecimal.valueOf(0);
		for (CartRow row : cartRows) {
			// 2.1 取得單價
			BigDecimal price = row.getUnitPrice();

			// 2.2 取得數量
			int quantity = row.getQuantity();

			// 2.3 將 quantity 轉成 BigDecimal
			BigDecimal quantityBD = BigDecimal.valueOf(quantity);

			// 2.4 單價 × 數量
			BigDecimal rowSubtotal = price.multiply(quantityBD);
			totalAmount = totalAmount.add(rowSubtotal);
		}
		// 2.5 將 BigDecimal 轉成 int
		int totalAmountInt = totalAmount.intValue();

		// 3.建立 orders
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
	

		// 4.建立 order_item
		Integer orderId = order.getOrderId();
		for (CartRow row : cartRows) {
			OrderItem oi = new OrderItem();
			// 4.1 order_id
			oi.setOrder(order);
			// 4.2 sku
			oi.setSku(row.getSku());
			// 4.3 product_name
			oi.setProductName(row.getProductName());
			// 4.4 unit_price
			oi.setUnitPrice(row.getUnitPrice());
			// 4.5 quantity
			oi.setQuantity(row.getQuantity());
			// 4.6 subtotal
			BigDecimal price = row.getUnitPrice();
			int quantity = row.getQuantity();
			BigDecimal quantityBD = BigDecimal.valueOf(quantity);
			BigDecimal subtotal = price.multiply(quantityBD);
			oi.setSubtotal(subtotal);
			orderItemDao.save(oi);
		}

		// 5.更新 cart_item.order_id
		int updatedCount = cartDao.attachCartItemsToOrder(orderId, cartRows);
		if (updatedCount <= 0) {
			throw new BusinessException("商品加入訂單更新0筆!");
		}

		// 6.回傳 CheckoutResult
		return new OrderCreationResponse (memberId, orderId, totalAmountInt, "PENDING");
	
	}
  
}
