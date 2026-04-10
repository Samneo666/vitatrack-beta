package web.checkout.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.service.PaymentService;
import web.checkout.vo.EcpayCheckoutPayload;
import web.checkout.vo.OrderPaymentInfo;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final OrderDao orderDao;
	private final OrderItemDao orderItemDao;
	private final SessionFactory sessionFactory;
	
	@Autowired
	public PaymentServiceImpl(OrderDao orderDao, OrderItemDao orderItemDao, SessionFactory sessionFactory) {
	    this.orderDao = orderDao;
	    this.orderItemDao = orderItemDao;
	    this.sessionFactory = sessionFactory;
	}


	// === 綠界測試環境網址 ===
	private static final String ECPAY_ACTION_URL_STAGE = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";
	
	// === 固定參數 ===
	private static final String MERCHANT_ID = "3002607";
	private static final String RETURN_URL = "https://leaseless-eventfully-sharyn.ngrok-free.dev/vitatrack/checkout/ecpay/callback";
	private static final String ORDER_RESULT_URL_BASE =
	        "https://leaseless-eventfully-sharyn.ngrok-free.dev/vitatrack/checkout/ecpay/return";
	private static final String TRADE_DESC = "Vitatrack訂單";
	private static final String CHOOSE_PAYMENT = "ALL";
	private static final String PAYMENT_TYPE = "aio";
	private static final String ENCRYPT_TYPE = "1";

	private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
	private static final String HASH_IV = "EkRm7IFT261dpevs";

	// 開關 Session + 啟動串金流
	@Override
	@Transactional
	public EcpayCheckoutPayload createEcpayCheckout(int orderId) {
	    Session session = sessionFactory.getCurrentSession();
	    return createEcpayCheckout(session, orderId);
	}

	// 驗證是否可付款 + 產生交易號 + 更新 orders.transaction_id + 組 itemName
	private OrderPaymentInfo validateOrderCanPay(Session session, int orderId) {

		// 1.查訂單
		OrderPaymentInfo info = orderDao.selectPaymentInfoByOrderId(orderId);
		// 1.1 訂單不存在
		if (info == null)
			return null;
		// 2.SUCCESS -> 不可再付款 || 其他狀態 -> 可付款
		if ("SUCCESS".equalsIgnoreCase(info.getPaymentStatus())) {
			return null;
		}
		// 3.產生 transactionId
		String transactionId = generateUniqueTxId(session);
		// 4.更新 orders.transaction_id
		int updated = orderDao.updateTransactionId(orderId, transactionId);
		if (updated <= 0) {
			throw new RuntimeException("updateTransactionId updated 0 rows.");
		}

		info.setTransactionId(transactionId);

		// 5.從 order_item 取 product_name
		List<String> names = orderItemDao.selectProductNamesByOrderId(orderId);
		if (names == null || names.isEmpty()) {
			return null;
		}
		// 6.綠界 ItemName 用 # 分隔
		info.setItemName(String.join("#", names));

		return info;
	}

	// 組綠界 payload
	private EcpayCheckoutPayload createEcpayCheckout(Session session, int orderId) {

		// 1.先驗證訂單
		OrderPaymentInfo info = validateOrderCanPay(session, orderId);
		if (info == null)
			return null;

		// 2.產 merchantTradeDate
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String merchantTradeDate = sdf.format(now);

		// 3.組綠界 payload
		Map<String, String> formParams = new LinkedHashMap<>();

		formParams.put("MerchantID", MERCHANT_ID);
		formParams.put("MerchantTradeNo", info.getTransactionId());
		formParams.put("MerchantTradeDate", merchantTradeDate);
		formParams.put("PaymentType", PAYMENT_TYPE);
		formParams.put("TotalAmount", String.valueOf(info.getTotalAmount()));
		formParams.put("TradeDesc", TRADE_DESC);
		formParams.put("ItemName", info.getItemName());
		formParams.put("ReturnURL", RETURN_URL);
		formParams.put("OrderResultURL", ORDER_RESULT_URL_BASE + "?orderId=" + orderId);
		formParams.put("ChoosePayment", CHOOSE_PAYMENT);
		formParams.put("EncryptType", ENCRYPT_TYPE);
		formParams.put("CheckMacValue", genCheckMacValueSha256(formParams));

		// 4.回綠界 payload
		return new EcpayCheckoutPayload(ECPAY_ACTION_URL_STAGE, formParams);
	}

	// 產生 transactionId
	private String generateUniqueTxId(Session session) {

		// 1.產生transactionId
		String txid = "TXN" + System.currentTimeMillis();
		// 2.檢查是否重複
		boolean exists = orderDao.existsTransactionId(txid);
		if (!exists)
			return txid;
		return txid;
	}

	// 計算 CheckMacValue
	private String genCheckMacValueSha256(Map<String, String> params) {

		// 1.排序(A->Z)
		Map<String, String> sorted = new java.util.TreeMap<>();
		sorted.putAll(params);

		// 2.不含 CheckMacValue
		sorted.remove("CheckMacValue");

		// 3.組成 key=value&key=value...
		String sb = "";
		for (Map.Entry<String, String> e : sorted.entrySet()) {

			if (!sb.equals(""))
				sb += "&";

			String value = (e.getValue() == null) ? "" : e.getValue();

			sb += e.getKey() + "=" + value;
		}

		// 4.前後加 HashKey / HashIV
		String raw = "HashKey=" + HASH_KEY + "&" + sb + "&HashIV=" + HASH_IV;

		// 5.URL Encode
		String encoded = ecpayUrlEncode(raw);

		// 6.SHA256 -> 大寫
		return sha256(encoded).toUpperCase();
	}

	// 轉成 URL Encode 格式
	private String ecpayUrlEncode(String raw) {
		try {
			// 1.使用java內建工具
			String encoded = java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8.name());

			encoded = encoded.replace("%2D", "-").replace("%2d", "-");
			encoded = encoded.replace("%5F", "_").replace("%5f", "_");
			encoded = encoded.replace("%2E", ".").replace("%2e", ".");
			encoded = encoded.replace("%21", "!");
			encoded = encoded.replace("%28", "(").replace("%29", ")");
			encoded = encoded.replace("%2A", "*").replace("%2a", "*");
			encoded = encoded.replace("%7E", "~").replace("%7e", "~");
			// 2.轉成小寫
			return encoded.toLowerCase();

		} catch (Exception e) {
			throw new RuntimeException("URL Encode failed", e);
		}
	}

	// 轉成 SHA-256 雜湊值
	private String sha256(String s) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));

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