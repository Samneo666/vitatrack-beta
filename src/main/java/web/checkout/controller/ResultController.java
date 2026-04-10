package web.checkout.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.ResultService;
import web.checkout.vo.ResultDTO;

@RestController
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/api/checkout/result")
    public ResponseEntity<?> getResult(@RequestParam(required = false) String orderId) {

        // 1. 檢查 orderId 是否有帶
        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Missing required query param: orderId\"}");
        }

        // 2. 把 orderId 轉成 int
        int parsedOrderId;
        try {
            parsedOrderId = Integer.parseInt(orderId.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"orderId must be an integer\"}");
        }

        // 3. 呼叫 service 取得此筆訂單相關資料
        ResultDTO row = resultService.getOrder(parsedOrderId);

        if (row == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\":\"Order not found\"}");
        }

        // 4. 回傳 JSON 給前端
        return ResponseEntity.ok(row);
    }
}