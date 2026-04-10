package web.checkout.dao;

import java.util.List;

import web.checkout.vo.OrderItem;

public interface OrderItemDao {

	// 新增一筆訂單明細
	void save(OrderItem item);

	// 查該訂單的商品名稱清單
	List<String> selectProductNamesByOrderId(int orderId);

}