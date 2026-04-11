package web.checkout.dao;

import java.util.List;

import web.checkout.vo.OrderItem;

/**
 * 訂單明細資料存取介面：負責訂單明細的新增與查詢。
 */
public interface OrderItemDao {

	/**
	 * 新增一筆訂單明細至資料庫。
	 *
	 * @param item 要儲存的訂單明細物件
	 */
	void save(OrderItem item);

	/**
	 * 查詢指定訂單的所有商品名稱，供組成 ECPay ItemName 欄位使用。
	 *
	 * @param orderId 訂單編號
	 * @return 商品名稱清單，依 itemId 排序
	 */
	List<String> selectProductNamesByOrderId(int orderId);

	/**
	 * 查詢指定訂單的完整明細清單，供前端訂單確認頁顯示。
	 *
	 * @param orderId 訂單編號
	 * @return 訂單明細清單，依 itemId 排序
	 */
	List<OrderItem> selectByOrderId(int orderId);
}
