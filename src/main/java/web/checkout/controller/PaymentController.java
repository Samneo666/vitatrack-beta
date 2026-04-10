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

@RestController
@RequestMapping("/api/checkout")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<?> createPayment(
            @RequestParam("orderId") String orderIdStr,
            @RequestParam(value = "paymentMethod", required = false, defaultValue = "Credit") String paymentMethod) {

        // 1.如果沒有傳 orderId 或內容是空
        if (orderIdStr == null || orderIdStr.isBlank()) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "missing orderId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 2.字串轉 int
        int orderId;
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "orderId must be a number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 3.呼叫 Service（傳入前端選擇的付款方式）
        EcpayCheckoutPayload payload = paymentService.createEcpayCheckout(orderId, paymentMethod);

        // 4.訂單不存在或已付款
        if (payload == null) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("message", "order not found or already paid");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // 5.成功
        return ResponseEntity.ok(payload);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("message", escapeJson(e.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();

        Map<String, String> error = new LinkedHashMap<>();
        error.put("message", "server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 避免 message 內有雙引號導致 JSON 壞掉
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}