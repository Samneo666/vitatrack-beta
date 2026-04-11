package web.checkout.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.service.PaymentService;
import web.checkout.vo.EcpayCheckoutPayload;
import web.checkout.vo.OrderPaymentInfo;

/**
 * 綠界付款服務實作：驗證訂單、產生交易號並組裝 ECPay AIO 結帳表單資料。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private OrderItemDao orderItemDao;

	@PersistenceContext
	private Session session;

	// 綠界測試環境結帳 API 網址
	private static final String ECPAY_ACTION_URL_STAGE = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

	// 特店編號（從 payment.properties 注入）
	@Value("${ecpay.merchant_id}")
	private String merchantId;

	// 付款完成後綠界以 POST 通知後端的回呼網址
	@Value("${ecpay.return_url}")
	private String returnUrl;

	// 付款完成後綠界將使用者導回的前端網址（含 orderId 查詢參數）
	@Value("${ecpay.order_result_url_base}")
	private String orderResultUrlBase;

	private static final String TRADE_DESC = "Vitatrack訂單";
	private static final String PAYMENT_TYPE = "aio";
	// 1 = SHA256 加密
	private static final String ENCRYPT_TYPE = "1";

	// ECPay 合法的 ChoosePayment 白名單
	private static final java.util.Set<String> VALID_CHOOSE_PAYMENTS =
	        new java.util.HashSet<>(java.util.Arrays.asList("Credit", "ATM", "CVS", "BARCODE", "ALL"));

	@Value("${ecpay.hash_key}")
	private String hashKey;

	@Value("${ecpay.hash_iv}")
	private String hashIv;

	/**
	 * 驗證訂單可付款後，產生並回傳 ECPay AIO 結帳的 action URL 與表單參數。
	 *
	 * @param orderId       訂單編號
	 * @param choosePayment ECPay 付款方式，不合法時自動回退為 {@code "Credit"}
	 * @return 結帳 payload；若訂單不存在或已付款成功則回傳 null
	 */
	@Override
	@Transactional
	public EcpayCheckoutPayload createEcpayCheckout(int orderId, String choosePayment) {
		// 白名單驗證，不合法的付款方式預設為 "Credit"
		String payment = (choosePayment != null && VALID_CHOOSE_PAYMENTS.contains(choosePayment))
		        ? choosePayment : "Credit";
		return buildEcpayCheckout(orderId, payment);
	}

	/**
	 * 驗證訂單是否可付款，並產生唯一交易號、取得商品名稱清單。
	 *
	 * @param orderId 訂單編號
	 * @return 填入交易號與商品名稱的付款資訊；若訂單不存在、已付款或無明細則回傳 null
	 */
	private OrderPaymentInfo validateOrderCanPay(int orderId) {

		// 查詢訂單的付款資訊
		OrderPaymentInfo info = orderDao.selectPaymentInfoByOrderId(orderId);
		if (info == null) {
			return null;
		}

		// 已付款成功的訂單不可重複付款
		if ("SUCCESS".equalsIgnoreCase(info.getPaymentStatus())) {
			return null;
		}

		// 產生唯一交易號並寫入 orders.transaction_id
		String transactionId = generateUniqueTxId();
		int updated = orderDao.updateTransactionId(orderId, transactionId);
		if (updated <= 0) {
			throw new RuntimeException("updateTransactionId updated 0 rows.");
		}
		info.setTransactionId(transactionId);

		// 取得商品名稱清單，組成 ECPay 要求的 # 分隔格式
		List<String> names = orderItemDao.selectProductNamesByOrderId(orderId);
		if (names == null || names.isEmpty()) {
			return null;
		}
		info.setItemName(String.join("#", names));

		return info;
	}

	/**
	 * 組裝並回傳 ECPay AIO 結帳所需的 action URL 與表單參數（含 CheckMacValue）。
	 *
	 * @param orderId       訂單編號
	 * @param choosePayment 已驗證的 ECPay 付款方式
	 * @return 結帳 payload；若訂單驗證失敗則回傳 null
	 */
	private EcpayCheckoutPayload buildEcpayCheckout(int orderId, String choosePayment) {

		// 驗證訂單並取得付款所需資訊
		OrderPaymentInfo info = validateOrderCanPay(orderId);
		if (info == null) {
			return null;
		}

		// 產生 ECPay 要求格式的交易日期
		String merchantTradeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

		// 依序填入 ECPay 表單欄位（順序影響 CheckMacValue 計算）
		Map<String, String> formParams = new LinkedHashMap<>();
		formParams.put("MerchantID", merchantId);
		formParams.put("MerchantTradeNo", info.getTransactionId());
		formParams.put("MerchantTradeDate", merchantTradeDate);
		formParams.put("PaymentType", PAYMENT_TYPE);
		formParams.put("TotalAmount", String.valueOf(info.getTotalAmount()));
		formParams.put("TradeDesc", TRADE_DESC);
		formParams.put("ItemName", info.getItemName());
		formParams.put("ReturnURL", returnUrl);
		formParams.put("OrderResultURL", orderResultUrlBase + "?orderId=" + orderId);
		formParams.put("ChoosePayment", choosePayment);
		formParams.put("EncryptType", ENCRYPT_TYPE);
		// CheckMacValue 必須在所有欄位填入後才計算
		formParams.put("CheckMacValue", genCheckMacValueSha256(formParams));

		return new EcpayCheckoutPayload(ECPAY_ACTION_URL_STAGE, formParams);
	}

	/**
	 * 產生以當前毫秒時間戳為基礎的唯一交易號。
	 *
	 * @return 格式為 {@code "TXN" + 毫秒時間戳} 的交易號字串
	 */
	private String generateUniqueTxId() {
		String txid = "TXN" + System.currentTimeMillis();
		// 若極低機率碰撞，仍回傳同一個值（時間戳已不同）
		boolean exists = orderDao.existsTransactionId(txid);
		if (!exists) {
			return txid;
		}
		return txid;
	}

	/**
	 * 依 ECPay 規格計算 CheckMacValue（排序參數 → URL Encode → SHA256 → 大寫）。
	 *
	 * @param params 要計算的表單參數（不含 CheckMacValue）
	 * @return 大寫的 SHA256 雜湊字串
	 */
	private String genCheckMacValueSha256(Map<String, String> params) {

		// 將參數按字母升冪排序，移除 CheckMacValue 本身
		Map<String, String> sorted = new java.util.TreeMap<>();
		sorted.putAll(params);
		sorted.remove("CheckMacValue");

		// 組成 key=value&key=value 格式
		String sb = "";
		for (Map.Entry<String, String> e : sorted.entrySet()) {
			if (!sb.equals("")) {
				sb += "&";
			}
			String value = (e.getValue() == null) ? "" : e.getValue();
			sb += e.getKey() + "=" + value;
		}

		// 前後包上 HashKey / HashIV，再依序 URL Encode → SHA256 → 大寫
		String raw = "HashKey=" + hashKey + "&" + sb + "&HashIV=" + hashIv;
		return sha256(ecpayUrlEncode(raw)).toUpperCase();
	}

	/**
	 * 依 ECPay 規格將字串進行 URL Encode，並還原部分特殊字元後轉小寫。
	 *
	 * @param raw 要編碼的原始字串
	 * @return 符合 ECPay 規格的編碼結果（小寫）
	 */
	private String ecpayUrlEncode(String raw) {
		try {
			String encoded = java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8.name());
			// 還原 ECPay 規格要求不編碼的特殊字元（大小寫皆處理）
			encoded = encoded.replace("%2D", "-").replace("%2d", "-");
			encoded = encoded.replace("%5F", "_").replace("%5f", "_");
			encoded = encoded.replace("%2E", ".").replace("%2e", ".");
			encoded = encoded.replace("%21", "!");
			encoded = encoded.replace("%28", "(").replace("%29", ")");
			encoded = encoded.replace("%2A", "*").replace("%2a", "*");
			encoded = encoded.replace("%7E", "~").replace("%7e", "~");
			return encoded.toLowerCase();
		} catch (Exception e) {
			throw new RuntimeException("URL Encode failed", e);
		}
	}

	/**
	 * 將字串以 SHA-256 演算法計算雜湊值，回傳小寫十六進位字串。
	 *
	 * @param s 要雜湊的字串
	 * @return 64 字元的小寫十六進位雜湊字串
	 */
	private String sha256(String s) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			// 將每個 byte 格式化為兩位十六進位字元
			StringBuilder sb = new StringBuilder(bytes.length * 2);
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException("SHA-256 failed", e);
		}
	}
}
