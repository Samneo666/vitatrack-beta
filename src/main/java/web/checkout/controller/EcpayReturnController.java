package web.checkout.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
     * 將使用者重新導向至付款確認頁，並帶入 orderId。
     *
     * @param orderId 訂單編號（由 OrderResultURL 帶入，可能為 null）
     * @return Spring MVC redirect 指令
     * @throws UnsupportedEncodingException 若 UTF-8 編碼不支援（實務上不會發生）
     */
    @PostMapping("/checkout/ecpay/return")
    public String handleReturn(@RequestParam(required = false) String orderId)
            throws UnsupportedEncodingException {

        // orderId 缺失時預設為 0，避免前端收到空值
        if (orderId == null || orderId.isBlank()) {
            orderId = "0";
        }

        // 對中文檔名進行 URL Encode，確保瀏覽器能正確解析路徑
        String target = "/"
                + URLEncoder.encode("付款確認中.html", StandardCharsets.UTF_8.name())
                + "?orderId=" + orderId;

        return "redirect:" + target;
    }
}
