package web.product.vo;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class CheckoutRequest {

    private List<CartItemRequest> items;

    
}