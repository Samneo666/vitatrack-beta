package web.checkout.service;

import java.util.Map;

/**
 * 綠界付款回呼服務介面：驗證回呼簽章並更新訂單付款狀態。
 */
public interface CallbackService {

	/**
	 * 處理綠界 POST 回呼：驗簽、比對交易號、更新訂單付款狀態。
	 *
	 * @param params 綠界回傳的所有參數（含 CheckMacValue）
	 * @return 回覆給綠界的字串，成功為 {@code "1|OK"}，失敗為 {@code "0|ERROR"}
	 */
	String handleCallback(Map<String, String> params);
}
