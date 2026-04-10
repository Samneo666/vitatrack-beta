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

/**
 * 結帳 Controller：提供查詢購物車與送出結帳兩支 API。
 */
@RestController
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    /**
     * 查詢指定會員的未結帳購物車（GET /checkout）。
     * 供前端進入結帳頁面時載入購物車商品清單。
     *
     * @param memberId 會員編號（query param）
     * @return 購物車明細清單
     */
    @GetMapping("/checkout")
    public List<CartRow> getCheckoutCart(@RequestParam("memberId") Integer memberId) {
        return checkoutService.getCheckoutCart(memberId);
    }

    /**
     * 送出結帳，建立訂單與訂單明細（POST /checkout）。
     *
     * @param memberId 會員編號（query param）
     * @return 結帳結果（含訂單編號、總金額、付款狀態）
     */
    @PostMapping("/checkout")
    public CheckoutResult checkout(@RequestParam("memberId") Integer memberId) {
        return checkoutService.checkout(memberId);
    }
}
