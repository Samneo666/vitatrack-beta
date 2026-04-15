package web.checkout.vo;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor

public class CheckoutResult {
    private final int orderId;
    private final int totalAmount;
    private final String status;
    

}
