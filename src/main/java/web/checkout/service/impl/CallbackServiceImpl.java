package web.checkout.service.impl;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.service.CallbackService;
import web.checkout.vo.Orders;

/**
 * 綠界付款回呼服務實作：驗證 CheckMacValue 簽章，並依付款結果更新訂單狀態。
 */
@Service
public class CallbackServiceImpl implements CallbackService {

	private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
	private static final String HASH_IV = "EkRm7IFT261dpevs";

	@Autowired
	private OrderDao orderDao;

	/**
	 * 處理綠界 POST 回呼：驗簽、比對交易號、更新訂單付款狀態與原始回應資料。
	 *
	 * @param params 綠界回傳的所有參數（含 CheckMacValue）
	 * @return 回覆給綠界的字串，成功為 {@code "1|OK"}，失敗為 {@code "0|ERROR"}
	 */
	@Transactional
	@Override
	public String handleCallback(Map<String, String> params) {

		// 取得綠界提供的 CheckMacValue
		String cmv = params.get("CheckMacValue");
		System.out.println("綠界提供的 CheckMacValue=" + cmv);

		// 複製一份參數（不分大小寫排序），移除 CheckMacValue 後自行計算簽章
		Map<String, String> copy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		copy.putAll(params);
		copy.remove("CheckMacValue");

		// 將剩餘參數組成 key=value& 格式
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : copy.entrySet()) {
			sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
		}

		// 前後包上 HashKey / HashIV，再經 UrlEncode → SHA256 → 大寫
		String toSign = "HashKey=" + HASH_KEY + "&" + sb.toString() + "HashIV=" + HASH_IV;
		String myCmv = sha256(urlEncode(toSign)).toUpperCase();
		System.out.println("本機算出的 CheckMacValue=" + myCmv);

		// 驗簽失敗，直接回覆錯誤
		if (cmv == null || !cmv.equalsIgnoreCase(myCmv)) {
			return "0|ERROR";
		}

		// 取出綠界回傳的交易號
		String merchantTradeNo = params.get("MerchantTradeNo");
		if (merchantTradeNo == null || merchantTradeNo.isBlank()) {
			return "0|ERROR";
		}

		// 以交易號查詢 DB 訂單，確認此筆交易確實存在
		Orders order = orderDao.selectByTransactionId(merchantTradeNo);
		if (order == null) {
			return "0|ERROR";
		}

		// 比對交易號是否與 DB 一致，防止資料被竄改
		if (!merchantTradeNo.equalsIgnoreCase(order.getTransactionId())) {
			return "0|ERROR";
		}

		// 若訂單已付款成功，直接回覆 OK，不重複更新
		if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
			return "1|OK";
		}

		// 依 RtnCode 決定新的付款狀態（1 = 成功）
		String rtnCode = params.get("RtnCode");
		String newStatus = "1".equals(rtnCode) ? "SUCCESS" : "FAILED";
		order.setPaymentStatus(newStatus);

		// 將所有回呼參數序列化為字串，存入 raw_response
		StringBuilder respSb = new StringBuilder();
		for (Map.Entry<String, String> e : params.entrySet()) {
			respSb.append(e.getKey()).append("=").append(e.getValue()).append("&");
		}
		if (respSb.length() > 0) {
			// 移除最後多餘的 &
			respSb.setLength(respSb.length() - 1);
		}
		order.setRawResponse(respSb.toString());

		// 付款失敗時記錄失敗原因，成功時清空
		order.setFailureReason("1".equals(rtnCode) ? null : params.get("RtnMsg"));

		return "1|OK";
	}

	/**
	 * 依 ECPay 規格將字串進行 URL Encode，並還原部分特殊字元後轉小寫。
	 *
	 * @param s 要編碼的原始字串
	 * @return 符合 ECPay 規格的編碼結果
	 */
	private String urlEncode(String s) {
		try {
			String encoded = java.net.URLEncoder.encode(s, "UTF-8");
			// 還原 ECPay 規格要求不編碼的特殊字元
			encoded = encoded
					.replace("%2d", "-")
					.replace("%5f", "_")
					.replace("%2e", ".")
					.replace("%21", "!")
					.replace("%2a", "*")
					.replace("%28", "(")
					.replace("%29", ")")
					.toLowerCase();
			return encoded;
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			byte[] sha256Bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));

			// 將每個 byte 轉為兩位十六進位字元
			StringBuilder hex = new StringBuilder();
			for (byte b : sha256Bytes) {
				String h = Integer.toHexString(0xff & b);
				if (h.length() == 1) {
					hex.append('0');
				}
				hex.append(h);
			}
			return hex.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
