package web.checkout.dao;

import web.checkout.vo.OrderPaymentInfo;
import web.checkout.vo.Orders;
import web.checkout.vo.ResultDTO;

public interface OrderDao {

	// 新增訂單
	void save(Orders order);

	// 查訂單
	OrderPaymentInfo selectPaymentInfoByOrderId(int orderId);

	// 產生一組唯一的 transaction_id
	int updateTransactionId(int orderId, String transactionId);

	// 檢查 transactionId 是否重複
	boolean existsTransactionId(String transactionId);

	// 用 transaction_id 查訂單是否存在
	Orders selectByTransactionId(String transactionId);

	// 查詢訂單狀態(提供前端判斷付款成功或失敗)
	ResultDTO selectByOrderId(int orderId);
}
