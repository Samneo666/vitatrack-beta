package web.cart.service;

import java.util.List;

import web.cart.dto.AddToCartItemResponse;
import web.cart.dto.RemoveCartItemResponse;
import web.cart.dto.UpdateCartItemResponse;


public interface CartService {

	AddToCartItemResponse addToCart(Integer memberId, String sku, Integer quantity);

	UpdateCartItemResponse updateQuantity(Integer memberId, String sku, Integer quantity);
	
	RemoveCartItemResponse removeItem(Integer memberId, List<String> skus);
	
	
}
