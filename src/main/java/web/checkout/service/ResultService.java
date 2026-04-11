package web.checkout.service;

import java.util.List;

import web.checkout.vo.OrderItem;
import web.checkout.vo.ResultDTO;

/**
 * 訂單查詢服務介面：查詢付款結果與訂單商品明細。
 */
public interface ResultService {

	/**
	 * 以訂單編號查詢付款結果，供前端判斷付款成功或失敗。
	 *
	 * @param orderId 訂單編號
	 * @return 付款結果 DTO；若訂單不存在則回傳 null
	 */
	ResultDTO getOrder(int orderId);

	/**
	 * 查詢指定訂單的商品明細清單，供前端訂單確認頁顯示。
	 *
	 * @param orderId 訂單編號
	 * @return 訂單明細清單
	 */
	List<OrderItem> getOrderItems(int orderId);
}
