package web.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    private String sku;
    private String productName;
    private Integer price;
    private Integer quantity;
    private String image;



}