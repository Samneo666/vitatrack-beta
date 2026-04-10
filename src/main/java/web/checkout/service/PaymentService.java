package web.checkout.service;

import web.checkout.vo.EcpayCheckoutPayload;

/**
 * 綠界付款服務介面：建立 ECPay 結帳所需的表單資料。
 */
public interface PaymentService {

	/**
	 * 驗證訂單可付款後，產生並回傳 ECPay AIO 結帳的 action URL 與表單參數。
	 *
	 * @param orderId       訂單編號
	 * @param choosePayment ECPay 付款方式（如 {@code "Credit"}、{@code "ATM"}、{@code "ALL"}），
	 *                      不合法時自動回退為 {@code "Credit"}
	 * @return 結帳 payload；若訂單不存在或已付款成功則回傳 null
	 */
	EcpayCheckoutPayload createEcpayCheckout(int orderId, String choosePayment);
}
