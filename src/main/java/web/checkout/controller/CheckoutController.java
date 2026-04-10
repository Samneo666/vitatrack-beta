package web.checkout.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.CheckoutService;
import web.checkout.vo.CartRow;
import web.checkout.vo.CheckoutResult;

@RestController
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    // 進入 checkout 頁面時，先查購物車資料
    @GetMapping("/checkout")
    public List<CartRow> getCheckoutCart(@RequestParam("memberId") Integer memberId) {
        return checkoutService.getCheckoutCart(memberId);
    }

    // 送出結帳，建立訂單
    @PostMapping("/checkout")
    public CheckoutResult checkout(@RequestParam("memberId") Integer memberId) {
        return checkoutService.checkout(memberId);
    }
}