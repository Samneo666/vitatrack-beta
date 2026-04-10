package web.checkout.service;

import java.util.List;

import web.checkout.vo.CartRow;
import web.checkout.vo.CheckoutResult;

/**
 * 結帳服務介面：處理購物車確認與訂單建立流程。
 */
public interface CheckoutService {

	/**
	 * 執行結帳流程：查購物車 → 計算總金額 → 建立訂單與訂單明細 → 綁定購物車。
	 *
	 * @param memberId 會員編號
	 * @return 結帳結果（含訂單編號、總金額、付款狀態）
	 * @throws RuntimeException 若購物車為空或資料更新失敗
	 */
	CheckoutResult checkout(int memberId);

	/**
	 * 查詢指定會員的未結帳購物車，供結帳頁面顯示商品清單。
	 *
	 * @param memberId 會員編號
	 * @return 購物車明細清單
	 */
	List<CartRow> getCheckoutCart(int memberId);
}
