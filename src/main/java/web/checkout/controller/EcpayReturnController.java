package web.checkout.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EcpayReturnController {

    @PostMapping("/checkout/ecpay/return")
    public String handleReturn(@RequestParam(required = false) String orderId)
            throws UnsupportedEncodingException {

        if (orderId == null || orderId.isBlank()) {
            orderId = "0";
        }

        String target = "/"
                + URLEncoder.encode("付款確認中.html", StandardCharsets.UTF_8.name())
                + "?orderId=" + orderId;

        return "redirect:" + target;
    }
}