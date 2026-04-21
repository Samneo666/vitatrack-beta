package web.checkout.service.impl;

import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.service.CallbackService;
import web.checkout.util.EcpayUtils;
import web.checkout.vo.Orders;

/**
 * 綠界付款回呼服務實作：驗證 CheckMacValue 簽章，並依付款結果更新訂單狀態。
 */
@Service
public class CallbackServiceImpl implements CallbackService {

    private static final Logger log = LogManager.getLogger(CallbackServiceImpl.class);

    @Value("${ecpay.hash_key}")
    private String hashKey;

    @Value("${ecpay.hash_iv}")
    private String hashIv;

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
        log.debug("綠界提供的 CheckMacValue={}", cmv);

        // 複製一份參數（不分大小寫排序），移除 CheckMacValue 後自行計算簽章
        Map<String, String> copy = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        copy.putAll(params);
        copy.remove("CheckMacValue");

        // 使用 EcpayUtils 計算簽章（與 PaymentServiceImpl 共用相同邏輯）
        String myCmv = EcpayUtils.genCheckMacValue(copy, hashKey, hashIv);
        log.debug("本機算出的 CheckMacValue={}", myCmv);

        // 驗簽失敗，直接回覆錯誤
        if (cmv == null || !cmv.equalsIgnoreCase(myCmv)) {
            log.warn("CheckMacValue 驗證失敗，綠界={}, 本機={}", cmv, myCmv);
            return "0|ERROR";
        }

        // 取出綠界回傳的交易號
        String merchantTradeNo = params.get("MerchantTradeNo");
        if (merchantTradeNo == null || merchantTradeNo.isBlank()) {
            log.warn("回呼缺少 MerchantTradeNo");
            return "0|ERROR";
        }

        // 以交易號查詢 DB 訂單，確認此筆交易確實存在
        Orders order = orderDao.selectByTransactionId(merchantTradeNo);
        if (order == null) {
            log.warn("找不到交易號對應的訂單: {}", merchantTradeNo);
            return "0|ERROR";
        }

        // 比對交易號是否與 DB 一致，防止資料被竄改
        if (!merchantTradeNo.equalsIgnoreCase(order.getTransactionId())) {
            log.warn("交易號不一致，DB={}, 綠界={}", order.getTransactionId(), merchantTradeNo);
            return "0|ERROR";
        }

        // 若訂單已付款成功，直接回覆 OK，不重複更新（冪等性）
        if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
            log.info("訂單 {} 已付款成功，忽略重複回呼", order.getOrderId());
            return "1|OK";
        }

        // 依 RtnCode 決定新的付款狀態（1 = 成功）
        String rtnCode = params.get("RtnCode");
        String newStatus = "1".equals(rtnCode) ? "SUCCESS" : "FAILED";
        order.setPaymentStatus(newStatus);

        // 付款成功時更新實際付款時間
        if ("SUCCESS".equals(newStatus)) {
            order.setPaymentTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }

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

        log.info("訂單 {} 付款狀態更新為 {}，RtnCode={}", order.getOrderId(), newStatus, rtnCode);
        return "1|OK";
    }
}
