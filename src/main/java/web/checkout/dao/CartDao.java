package web.checkout.dao;

import java.util.List;

import web.checkout.vo.CartRow;

/**
 * 購物車資料存取介面：查詢未結帳購物車與綁定訂單。
 */
public interface CartDao {

	/**
	 * 查詢指定會員尚未結帳的購物車明細。
	 *
	 * @param memberId 會員編號
	 * @return 購物車明細清單；若購物車為空則回傳空清單
	 */
	List<CartRow> findOpenCartByMemberId(int memberId);

	/**
	 * 將指定的購物車品項綁定至已建立的訂單。
	 *
	 * @param orderId  要綁定的訂單編號
	 * @param cartRows 要更新的購物車品項清單
	 * @return 實際更新的列數
	 */
	int attachCartItemsToOrder(int orderId, List<CartRow> cartRows);
}
