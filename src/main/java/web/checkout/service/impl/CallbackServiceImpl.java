package web.checkout.service.impl;

import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.service.CallbackService;
import web.checkout.vo.Orders;

@Service
public class CallbackServiceImpl implements CallbackService {

	private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
	private static final String HASH_IV = "EkRm7IFT261dpevs";
	private final OrderDao orderDao;
	private final SessionFactory sessionFactory;

	@Autowired
	public CallbackServiceImpl(OrderDao orderDao, SessionFactory sessionFactory) {
		this.orderDao = orderDao;
		this.sessionFactory = sessionFactory;
	}

	@Transactional
	@Override
	public String handleCallback(Map<String, String> params) {

		Session session = sessionFactory.getCurrentSession();

		// 1.取得綠界提供的 CheckMacValue
		String cmv = params.get("CheckMacValue");
		System.out.println("綠界提供的 CheckMacValue=" + cmv);

		// 2.自行計算 CheckMacValue
		// 2.1 建一份新的 Map
		Map<String, String> copy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		copy.putAll(params);
		// 2.2 移除 CheckMacValue
		copy.remove("CheckMacValue");
		// 2.3 組成字串
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : copy.entrySet()) {
			sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
		}
		// 2.4 將 StringBuilder 轉 String
		String raw = sb.toString();
		// 2.5 自行加上 HASH_KEY, HASH_IV
		String toSign = "HashKey=" + HASH_KEY + "&" + raw + "HashIV=" + HASH_IV;
		// 2.6 UrlEncode
		String encoded = urlEncode(toSign);
		// 2.7 轉 sha256 轉 字串 轉 大寫
		String myCmv = sha256(encoded).toUpperCase();
		System.out.println("Jerry 算出的 CheckMacValue=" + myCmv);

		// 3.比對CheckMacValue
		boolean ok = (cmv != null) && cmv.equalsIgnoreCase(myCmv);
		// 3.1 驗簽失敗，回覆ECPay，並不繼續動作
		if (!ok) {
			return "0|ERROR";
		}

		// 4.取出 ECPay 的 TransactionId(MerchantTradeNo)
		// 4.1 取出 ECPay 的 TransactionId(MerchantTradeNo)
		String merchantTradeNo = params.get("MerchantTradeNo");
		// 4.2 沒收到MerchantTradeNo，回覆ECPay，並不繼續動作
		if (merchantTradeNo == null || merchantTradeNo.isBlank()) {
			return "0|ERROR";
		}

		// 5.取出 DB 的 TransactionId
		Orders order = orderDao.selectByTransactionId(merchantTradeNo);
		// 5.1 DB 找不到這筆交易
		if (order == null) {
			return "0|ERROR";
		}

		// 6.比對 TransactionId
		boolean ok1 = merchantTradeNo.equalsIgnoreCase(order.getTransactionId());
		// 6.1 TransactionId 不一致
		if (!ok1) {
			return "0|ERROR";
		}

		// 7.查目前 payment_status
		// 7.1 查目前 payment_status
		String currentStatus = order.getPaymentStatus();

		// 7.2 如果已 SUCCESS -> 直接 1|OK
		if ("SUCCESS".equalsIgnoreCase(currentStatus)) {
			return "1|OK";
		}

		// 7.3取得ECPay的RtnCode
		String rtnCode = params.get("RtnCode");
		String newStatus = "1".equals(rtnCode) ? "SUCCESS" : "FAILED";

		// 8.依 RtnCode 更新 SUCCESS / FAILED
		order.setPaymentStatus(newStatus);

		// 9.更新 raw_response、failureReason
		// 9.1 組 raw_response
		StringBuilder respSb = new StringBuilder();
		for (Map.Entry<String, String> e : params.entrySet()) {
			respSb.append(e.getKey()).append("=").append(e.getValue()).append("&");
		}
		if (respSb.length() > 0) {
			respSb.setLength(respSb.length() - 1);
		}
		String rawResponse = respSb.toString();

		// 9.2 產 failureReason
		String failureReason = "1".equals(rtnCode) ? null : params.get("RtnMsg");

		// 9.3 寫回 DB
		order.setRawResponse(rawResponse);
		order.setFailureReason(failureReason);

		return "1|OK";

	}

	// UrlEncode 方法
	private String urlEncode(String s) {
		try {
			// 1.用UTF-8 編碼
			String encoded = java.net.URLEncoder.encode(s, "UTF-8");
			// 2.%2d 換回 -
			encoded = encoded.replace("%2d", "-")
					// 3.%5f 換回 _
					.replace("%5f", "_")
					// 4.%2e 換回 .
					.replace("%2e", ".")
					// 5.%21 換回 !
					.replace("%21", "!")
					// 6.%2a 換回 *
					.replace("%2a", "*")
					// 7.%28 換回 (
					.replace("%28", "(")
					// 8.%29 換回 )
					.replace("%29", ")")
					// 9.轉小寫
					.toLowerCase();
			return encoded;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// 轉 sha256 方法
	private String sha256(String s) {
		try {
			// 1.sha256 演算法_計算器
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
			// 2.字串 轉 byte[]
			byte[] bytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			// 3.byte[] 轉 sha256
			byte[] sha256Bytes = md.digest(bytes);
			// 4.sha256 byte[] 轉 StringBuilder
			StringBuilder hex = new StringBuilder();

			for (byte b : sha256Bytes) {
				String h = Integer.toHexString(0xff & b);

				if (h.length() == 1) {
					hex.append('0');
				}

				hex.append(h);
			}
			// 5.StringBuilder 轉 String
			return hex.toString();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}