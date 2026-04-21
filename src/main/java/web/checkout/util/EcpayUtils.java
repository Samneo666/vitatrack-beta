package web.checkout.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 綠界金流 (ECPay) 簽章工具類。
 *
 * <p>提供三個靜態方法供 PaymentServiceImpl 與 CallbackServiceImpl 共用，
 * 消除重複程式碼並確保簽章邏輯完全一致：
 * <ul>
 *   <li>{@link #sha256(String)} — SHA-256 雜湊</li>
 *   <li>{@link #ecpayUrlEncode(String)} — 符合綠界規格的 URL Encode</li>
 *   <li>{@link #genCheckMacValue(Map, String, String)} — 計算 CheckMacValue</li>
 * </ul>
 */
public final class EcpayUtils {

    private EcpayUtils() {
        // utility class, no instances
    }

    /**
     * 將字串以 SHA-256 演算法計算雜湊值，回傳小寫十六進位字串。
     *
     * @param s 要雜湊的字串（UTF-8 編碼）
     * @return 64 字元的小寫十六進位雜湊字串
     * @throws RuntimeException 若 SHA-256 演算法不可用（不應發生）
     */
    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    /**
     * 依綠界規格將字串進行 URL Encode，還原部分特殊字元後轉小寫。
     *
     * <p>Java 的 {@link URLEncoder} 會將部分字元編碼為大寫（如 {@code %2D}），
     * 而綠界規格要求這些字元不被編碼。本方法同時處理大小寫編碼結果，
     * 確保跨 JVM 版本的相容性。
     *
     * @param raw 要編碼的原始字串
     * @return 符合綠界規格的編碼結果（小寫）
     * @throws RuntimeException 若 UTF-8 編碼不可用（不應發生）
     */
    public static String ecpayUrlEncode(String raw) {
        try {
            String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8.name());
            // 還原綠界規格要求不編碼的特殊字元，同時處理大寫與小寫兩種形式
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

    /**
     * 依綠界規格計算 CheckMacValue（排序參數 → URL Encode → SHA256 → 大寫）。
     *
     * <p>計算步驟：
     * <ol>
     *   <li>將 {@code params} 按 key 字母升冪排序，移除 CheckMacValue 本身</li>
     *   <li>組成 {@code key=value&key=value} 字串</li>
     *   <li>前後加上 {@code HashKey=...} 與 {@code &HashIV=...}</li>
     *   <li>對整體字串進行 {@link #ecpayUrlEncode(String)}</li>
     *   <li>對結果進行 {@link #sha256(String)} 並轉大寫</li>
     * </ol>
     *
     * @param params  要計算的表單參數（不含 CheckMacValue）
     * @param hashKey 綠界提供的 HashKey
     * @param hashIv  綠界提供的 HashIV
     * @return 大寫的 SHA-256 CheckMacValue 字串
     */
    public static String genCheckMacValue(Map<String, String> params, String hashKey, String hashIv) {
        // 按 key 字母升冪排序，移除 CheckMacValue
        Map<String, String> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(params);
        sorted.remove("CheckMacValue");

        // 組成 key=value& 格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue() == null ? "" : e.getValue());
        }

        // 前後包上 HashKey / HashIV，再 URL Encode → SHA256 → 大寫
        String raw = "HashKey=" + hashKey + "&" + sb.toString() + "&HashIV=" + hashIv;
        return sha256(ecpayUrlEncode(raw)).toUpperCase();
    }
}
