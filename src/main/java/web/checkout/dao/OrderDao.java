package web.checkout.dao;

import web.checkout.vo.OrderPaymentInfo;
import web.checkout.vo.Orders;
import web.checkout.vo.ResultDTO;

/**
 * 訂單資料存取介面：負責訂單的新增、查詢與交易號管理。
 */
public interface OrderDao {

	/**
	 * 新增一筆訂單至資料庫。
	 *
	 * @param order 要儲存的訂單物件
	 */
	void save(Orders order);

	/**
	 * 以訂單編號查詢付款相關資訊（付款狀態、總金額、交易號）。
	 *
	 * @param orderId 訂單編號
	 * @return 付款資訊；若訂單不存在則回傳 null
	 */
	OrderPaymentInfo selectPaymentInfoByOrderId(int orderId);

	/**
	 * 更新指定訂單的交易號（transaction_id）。
	 *
	 * @param orderId       訂單編號
	 * @param transactionId 要寫入的交易號
	 * @return 實際更新的列數
	 */
	int updateTransactionId(int orderId, String transactionId);

	/**
	 * 檢查指定交易號是否已存在於資料庫，用於確保唯一性。
	 *
	 * @param transactionId 要檢查的交易號
	 * @return 若已存在則回傳 true
	 */
	boolean existsTransactionId(String transactionId);

	/**
	 * 以交易號查詢對應的訂單。
	 *
	 * @param transactionId ECPay 回傳的交易號
	 * @return 對應訂單；若不存在則回傳 null
	 */
	Orders selectByTransactionId(String transactionId);

	/**
	 * 以訂單編號查詢付款結果，供前端判斷付款成功或失敗。
	 *
	 * @param orderId 訂單編號
	 * @return 付款結果 DTO；若訂單不存在則回傳 null
	 */
	ResultDTO selectByOrderId(int orderId);
}
