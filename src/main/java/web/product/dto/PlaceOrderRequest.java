package web.product.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 下單請求 DTO：接收前端傳入的收件人資訊。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
}
