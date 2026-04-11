package web.checkout.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.PaymentService;
import web.checkout.vo.EcpayCheckoutPayload;

/**
 * 付款 Controller：接收前端發起付款請求，回傳 ECPay AIO 結帳所需的 action URL 與表單參數。
 */
@RestController
@RequestMapping("/api/checkout")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 發起 ECPay 結帳（POST /api/checkout/payment）。
     * 驗證訂單後回傳綠界結帳表單的 action URL 與所有表單欄位。
     *
     * @param orderIdStr    訂單編號字串（query param）
     * @param paymentMethod ECPay 付款方式，預設為 {@code "Credit"}
     * @return 200 含結帳 payload；400 參數錯誤；409 訂單不存在或已付款
     */
    @PostMapping("/payment")
    public ResponseEntity<?> createPayment(
            @RequestParam("orderId") String orderIdStr,
            @RequestParam(value = "paymentMethod", required = false, defaultValue = "Credit") String paymentMethod) {

        // orderId 不得為空
        if (orderIdStr == null || orderIdStr.isBlank()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "missing orderId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 將字串轉為 int，非數字則回傳 400
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "orderId must be a number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 呼叫 Service 產生綠界結帳資料
        EcpayCheckoutPayload payload = paymentService.createEcpayCheckout(orderId, paymentMethod);

        // Service 回傳 null 表示訂單不存在或已付款成功
        if (payload == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "order not found or already paid");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        return ResponseEntity.ok(payload);
    }

    /**
     * 處理 {@link IllegalArgumentException}，回傳 400 Bad Request。
     *
     * @param e 捕獲的例外
     * @return 含錯誤訊息的 400 回應
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("message", escapeJson(e.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 處理所有未預期例外，回傳 500 Internal Server Error。
     *
     * @param e 捕獲的例外
     * @return 含通用錯誤訊息的 500 回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        Map<String, String> error = new LinkedHashMap<>();
        error.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 跳脫字串中的反斜線與雙引號，避免手動拼接 JSON 時格式錯誤。
     *
     * @param s 原始訊息字串
     * @return 已跳脫的安全字串；若輸入為 null 則回傳空字串
     */
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
