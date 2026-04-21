package web.checkout.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.service.PaymentService;
import web.checkout.util.EcpayUtils;
import web.checkout.vo.EcpayCheckoutPayload;
import web.checkout.vo.OrderPaymentInfo;

/**
 * 綠界付款服務實作：驗證訂單、產生交易號並組裝 ECPay AIO 結帳表單資料。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LogManager.getLogger(PaymentServiceImpl.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    // 綠界測試環境結帳 API 網址
    private static final String ECPAY_ACTION_URL_STAGE =
            "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    private static final String TRADE_DESC    = "Vitatrack訂單";
    private static final String PAYMENT_TYPE  = "aio";
    // 1 = SHA256 加密
    private static final String ENCRYPT_TYPE  = "1";

    // ECPay 合法的 ChoosePayment 白名單
    private static final java.util.Set<String> VALID_CHOOSE_PAYMENTS =
            new java.util.HashSet<>(java.util.Arrays.asList("Credit", "ATM", "CVS", "BARCODE", "ALL"));

    // 特店編號（從 payment.properties 注入）
    @Value("${ecpay.merchant_id}")
    private String merchantId;

    // 付款完成後綠界以 POST 通知後端的回呼網址
    @Value("${ecpay.return_url}")
    private String returnUrl;

    // 付款完成後綠界將使用者導回的前端網址（含 orderId 查詢參數）
    @Value("${ecpay.order_result_url_base}")
    private String orderResultUrlBase;

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
            log.warn("找不到訂單 {}", orderId);
            return null;
        }

        // 已付款成功的訂單不可重複付款
        if ("SUCCESS".equalsIgnoreCase(info.getPaymentStatus())) {
            log.info("訂單 {} 已付款成功，拒絕重複付款", orderId);
            return null;
        }

        // 產生唯一交易號並寫入 orders.transaction_id
        String transactionId = generateUniqueTxId();
        int updated = orderDao.updateTransactionId(orderId, transactionId);
        if (updated <= 0) {
            throw new RuntimeException("updateTransactionId updated 0 rows for orderId=" + orderId);
        }
        info.setTransactionId(transactionId);

        // 取得商品名稱清單，組成 ECPay 要求的 # 分隔格式
        List<String> names = orderItemDao.selectProductNamesByOrderId(orderId);
        if (names == null || names.isEmpty()) {
            log.warn("訂單 {} 查無商品明細", orderId);
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
        formParams.put("MerchantID",       merchantId);
        formParams.put("MerchantTradeNo",  info.getTransactionId());
        formParams.put("MerchantTradeDate", merchantTradeDate);
        formParams.put("PaymentType",      PAYMENT_TYPE);
        formParams.put("TotalAmount",      String.valueOf(info.getTotalAmount()));
        formParams.put("TradeDesc",        TRADE_DESC);
        formParams.put("ItemName",         info.getItemName());
        formParams.put("ReturnURL",        returnUrl);
        formParams.put("OrderResultURL",   orderResultUrlBase + "?orderId=" + orderId);
        formParams.put("ChoosePayment",    choosePayment);
        formParams.put("EncryptType",      ENCRYPT_TYPE);
        // CheckMacValue 必須在所有欄位填入後才計算
        formParams.put("CheckMacValue",    EcpayUtils.genCheckMacValue(formParams, hashKey, hashIv));

        log.info("建立綠界結帳 payload，orderId={}, txId={}", orderId, info.getTransactionId());
        return new EcpayCheckoutPayload(ECPAY_ACTION_URL_STAGE, formParams);
    }

    /**
     * 產生唯一交易號（以毫秒時間戳為基礎）。
     * 若發生碰撞（機率極低），加入隨機後綴重試。
     *
     * @return 格式為 {@code "TXN" + 毫秒 + 可選隨機後綴} 的唯一交易號字串
     */
    private String generateUniqueTxId() {
        String txid = "TXN" + System.currentTimeMillis();
        if (!orderDao.existsTransactionId(txid)) {
            return txid;
        }
        // 碰撞時加入 4 位隨機數字後綴重試（最多 5 次）
        for (int i = 0; i < 5; i++) {
            txid = "TXN" + System.currentTimeMillis() + (int)(Math.random() * 9000 + 1000);
            if (!orderDao.existsTransactionId(txid)) {
                return txid;
            }
        }
        log.error("無法在 5 次重試內產生唯一交易號，最後嘗試值: {}", txid);
        throw new RuntimeException("Unable to generate unique transaction ID after retries.");
    }
}
