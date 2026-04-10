package web.checkout.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.CallbackService;

@RestController
public class CallbackController {

    private final CallbackService callbackService;

    public CallbackController(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    @PostMapping("/checkout/ecpay/callback")
    public String handleCallback(@RequestParam Map<String, String> params) {

        // 印出 ECPay 回傳資料
        for (Map.Entry<String, String> entry : params.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        // 呼叫 Service
        return callbackService.handleCallback(params);
    }
}