package web.checkout.vo;

import java.util.Map;

// 1. 綠界要送出的 actionUrl
// 2. 綠界要送出的 form 參數

public class EcpayCheckoutPayload {

    // 綠界付款頁的網址
    private String actionUrl;

    // 綠界需要的 12 個參數
    private Map<String, String> formParams;

    public EcpayCheckoutPayload(String actionUrl, Map<String, String> formParams) {
        this.actionUrl = actionUrl;
        this.formParams = formParams;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Map<String, String> getFormParams() {
        return formParams;
    }

    public void setFormParams(Map<String, String> formParams) {
        this.formParams = formParams;
    }
}