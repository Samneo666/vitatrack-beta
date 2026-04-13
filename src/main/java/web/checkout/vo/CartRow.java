package web.checkout.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRow {
	
    private int cartItemId;
    private String sku;
    private String productName;
    private BigDecimal unitPrice; 
    private int quantity;
	private String imageUrl;


}
