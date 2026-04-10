package web.checkout.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.checkout.service.CallbackService;

/**
 * 綠界付款回呼 Controller：接收綠界 POST 通知並委由 Service 驗簽與更新訂單狀態。
 */
@RestController
public class CallbackController {

    @Autowired
    private CallbackService callbackService;

    /**
     * 接收綠界付款結果回呼（POST /checkout/ecpay/callback）。
     * 將所有回呼參數轉交 Service 處理，回覆綠界所需的結果字串。
     *
     * @param params 綠界以 form-data 回傳的所有參數
     * @return {@code "1|OK"} 表示處理成功；{@code "0|ERROR"} 表示驗證失敗
     */
    @PostMapping("/checkout/ecpay/callback")
    public String handleCallback(@RequestParam Map<String, String> params) {

        // 印出綠界回傳的所有參數，方便除錯
        for (Map.Entry<String, String> entry : params.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        return callbackService.handleCallback(params);
    }
}
