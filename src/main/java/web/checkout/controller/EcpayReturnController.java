package web.checkout.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 綠界付款完成導回 Controller：將使用者從綠界頁面導回本站的付款確認頁。
 */
@Controller
public class EcpayReturnController {

    /**
     * 接收綠界付款完成後的 POST 導回（POST /checkout/ecpay/return）。
     * 將使用者重新導向至付款等待確認頁，並帶入 orderId。
     *
     * @param orderId 訂單編號（由 OrderResultURL 帶入，可能為 null）
     * @return Spring MVC redirect 指令
     */
    @PostMapping("/checkout/ecpay/return")
    public String handleReturn(@RequestParam(required = false) String orderId) {

        // orderId 缺失時預設為 0，避免前端收到空值
        if (orderId == null || orderId.isBlank()) {
            orderId = "0";
        }

        return "redirect:/paymentPendingPage.html?orderId=" + orderId;
    }
}
