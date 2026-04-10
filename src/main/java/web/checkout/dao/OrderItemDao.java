package web.checkout.dao;

import java.util.List;

import web.checkout.vo.OrderItem;

public interface OrderItemDao {

	// 新增一筆訂單明細
	void save(OrderItem item);

	// 查該訂單的商品名稱清單（供 ECPay ItemName 使用）
	List<String> selectProductNamesByOrderId(int orderId);

	// 查該訂單的完整明細清單（供前端訂單確認頁顯示）
	List<OrderItem> selectByOrderId(int orderId);

}