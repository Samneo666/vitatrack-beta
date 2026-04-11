package web.checkout.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.checkout.service.CheckoutService;
import web.checkout.vo.CartRow;
import web.checkout.vo.CheckoutResult;
import web.member.vo.Member;

/**
 * 結帳 Controller：提供查詢購物車與送出結帳兩支 API。
 */
@RestController
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    /**
     * 查詢登入會員的未結帳購物車（GET /api/checkout）。
     * 供前端進入結帳頁面時載入購物車商品清單。
     *
     * @param member 由 session 注入的登入會員
     * @return 購物車明細清單
     */
    @GetMapping("/api/checkout")
    public List<CartRow> getCheckoutCart(@SessionAttribute("member") Member member) {
        return checkoutService.getCheckoutCart(member.getMemberId());
    }

    /**
     * 送出結帳，建立訂單與訂單明細（POST /api/checkout）。
     *
     * @param member 由 session 注入的登入會員
     * @return 結帳結果（含訂單編號、總金額、付款狀態）
     */
    @PostMapping("/api/checkout")
    public CheckoutResult checkout(@SessionAttribute("member") Member member) {
        return checkoutService.checkout(member.getMemberId());
    }
}
