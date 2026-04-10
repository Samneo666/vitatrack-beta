package web.checkout.service;

import java.util.List;

import web.checkout.vo.OrderItem;
import web.checkout.vo.ResultDTO;

//查詢訂單狀態(提供前端判斷付款成功或失敗)
public interface ResultService {
	ResultDTO getOrder(int orderId);

	// 查詢訂單明細（提供前端訂單確認頁顯示商品清單）
	List<OrderItem> getOrderItems(int orderId);
}
