package web.member.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import web.checkout.vo.CartItem;
import web.product.vo.Product;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
	private String productName;
	private String sku;
	private String size;
	private BigDecimal price;
	private Integer quantity;
	private BigDecimal subtotal;
	private Integer stockQuantity;
	
	public CartItemResponse(CartItem cartItem, Product product ) {
		this.productName = product.getProductName();
		this.sku = product.getSku();
		this.size = product.getSize();
		this.price = product.getPrice();
		this.quantity = cartItem.getQuantity();
		this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
		this.stockQuantity = product.getStockQuantity();
		
	}

	

}
