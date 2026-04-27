package web.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.member.vo.Member;
import web.product.dto.OrderCreationResponse;
import web.product.dto.PlaceOrderRequest;
import web.product.service.PlaceOrderService;

@RestController
public class PlaceOrderController {

    @Autowired
    private PlaceOrderService placeOrderService;

    /**
     * 送出結帳，建立訂單（POST /placeOrder）。
     *
     * @param member  由 session 注入的登入會員（required=false 搭配手動 null 檢查，回傳 401）
     * @param request 前端傳入的收件人資訊（receiverName / receiverPhone / receiverAddress）
     * @return 訂單建立結果；若未登入則回傳 401
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<?> checkout(
            @SessionAttribute(required = false) Member member,
            @RequestBody PlaceOrderRequest request) {

        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "請先登入", null));
        }

        OrderCreationResponse result = placeOrderService.checkout(member.getMemberId(), request);
        return ResponseEntity.ok(result);
    }
}
