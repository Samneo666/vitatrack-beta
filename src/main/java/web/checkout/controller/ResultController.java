package web.checkout.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.ResultService;
import web.checkout.vo.OrderItem;
import web.checkout.vo.ResultDTO;

/**
 * 訂單查詢 Controller：提供付款結果與訂單商品明細兩支 GET API。
 */
@RestController
public class ResultController {

    @Autowired
    private ResultService resultService;

    /**
     * 查詢訂單付款結果（GET /api/checkout/result?orderId=X）。
     * 供付款確認頁輪詢，判斷付款是否成功。
     *
     * @param orderId 訂單編號字串（query param，必填）
     * @return 200 含付款結果 DTO；400 參數錯誤；404 訂單不存在
     */
    @GetMapping("/api/checkout/result")
    public ResponseEntity<?> getResult(@RequestParam(required = false) String orderId) {

        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Missing required query param: orderId\"}");
        }

        int parsedOrderId;
        try {
            parsedOrderId = Integer.parseInt(orderId.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"orderId must be an integer\"}");
        }

        ResultDTO row = resultService.getOrder(parsedOrderId);
        if (row == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"Order not found\"}");
        }

        return ResponseEntity.ok(row);
    }

    /**
     * 查詢訂單商品明細（GET /api/checkout/order-items?orderId=X）。
     * 供訂單確認頁顯示商品名稱、單價、數量與小計。
     *
     * @param orderId 訂單編號字串（query param，必填）
     * @return 200 含商品明細清單（輕量 Map）；400 參數錯誤；404 查無明細
     */
    @GetMapping("/api/checkout/order-items")
    public ResponseEntity<?> getOrderItems(@RequestParam(required = false) String orderId) {

        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Missing required query param: orderId\"}");
        }

        int parsedOrderId;
        try {
            parsedOrderId = Integer.parseInt(orderId.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"orderId must be an integer\"}");
        }

        List<OrderItem> items = resultService.getOrderItems(parsedOrderId);
        if (items == null || items.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"No items found for this order\"}");
        }

        // 轉成輕量 Map，避免 OrderItem.order（ManyToOne）序列化時造成循環引用
        List<Map<String, Object>> result = items.stream().map(oi -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("itemId",      oi.getItemId());
            row.put("sku",         oi.getSku());
            row.put("productName", oi.getProductName());
            row.put("unitPrice",   oi.getUnitPrice());
            row.put("quantity",    oi.getQuantity());
            row.put("subtotal",    oi.getSubtotal());
            return row;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
