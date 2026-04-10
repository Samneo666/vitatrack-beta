package web.product.service;

import java.util.List;

import web.product.vo.CartItem;

public interface CartItemService {
	
	boolean saveCartItems(List<CartItem> cartItems);
}