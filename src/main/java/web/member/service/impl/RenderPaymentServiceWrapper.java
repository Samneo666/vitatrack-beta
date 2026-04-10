package web.member.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import web.checkout.service.PaymentService;
import web.checkout.vo.EcpayCheckoutPayload;

/**
 * 這是一個裝飾器 (Decorator)，用來在不更改組員原本 PaymentServiceImpl 程式碼的情況下，
 * 攔截產生出來的綠界表單參數，並將 ngrok 的網址替換為 Render 部署網址（從 payment.properties 讀取）。
 */
@Service
@Primary // 讓 Spring 優先注入這個 Wrapper 而不是原本的 Service
@PropertySource("classpath:payment.properties")
public class RenderPaymentServiceWrapper implements PaymentService {

    private final PaymentService delegate;

    @Value("${ecpay.return_url}")
    private String renderReturnUrl;

    @Value("${ecpay.order_result_url_base}")
    private String renderOrderResultUrlBase;

    // 與原本相同的 HashKey 和 HashIV
    private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private static final String HASH_IV = "EkRm7IFT261dpevs";

    @Autowired
    public RenderPaymentServiceWrapper(@Qualifier("paymentServiceImpl") PaymentService delegate) {
        this.delegate = delegate;
    }

    @Override
    public EcpayCheckoutPayload createEcpayCheckout(int orderId) {
        // 1. 呼叫組員原本的 Service 取得包含 ngrok 網址的 payload
        EcpayCheckoutPayload payload = delegate.createEcpayCheckout(orderId);

        if (payload == null) {
            return null;
        }

        // 2. 取得原本產生的表單參數
        Map<String, String> formParams = payload.getFormParams();

        // 3. 替換為 Render 部署的網址
        formParams.put("ReturnURL", renderReturnUrl);
        formParams.put("OrderResultURL", renderOrderResultUrlBase + "?orderId=" + orderId);

        // 4. 因為參數改變了，所以必須重新計算 CheckMacValue
        formParams.put("CheckMacValue", genCheckMacValueSha256(formParams));

        return payload;
    }

    // --- 以下為重新計算 CheckMacValue 的邏輯 (與組員原本寫法一致) ---

    private String genCheckMacValueSha256(Map<String, String> params) {
        Map<String, String> sorted = new java.util.TreeMap<>(params);
        sorted.remove("CheckMacValue");

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            String value = (e.getValue() == null) ? "" : e.getValue();
            sb.append(e.getKey()).append("=").append(value);
        }

        String raw = "HashKey=" + HASH_KEY + "&" + sb.toString() + "&HashIV=" + HASH_IV;
        String encoded = ecpayUrlEncode(raw);
        return sha256(encoded).toUpperCase();
    }

    private String ecpayUrlEncode(String raw) {
        try {
            String encoded = java.net.URLEncoder.encode(raw, java.nio.charset.StandardCharsets.UTF_8.name());
            encoded = encoded.replace("%2D", "-").replace("%2d", "-");
            encoded = encoded.replace("%5F", "_").replace("%5f", "_");
            encoded = encoded.replace("%2E", ".").replace("%2e", ".");
            encoded = encoded.replace("%21", "!");
            encoded = encoded.replace("%28", "(").replace("%29", ")");
            encoded = encoded.replace("%2A", "*").replace("%2a", "*");
            encoded = encoded.replace("%7E", "~").replace("%7e", "~");
            return encoded.toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("URL Encode failed", e);
        }
    }

    private String sha256(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }
}
