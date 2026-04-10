package web.product.dao;

import web.product.vo.CartItem;

public interface CartItemDao {

	    int insert(CartItem cartItem);

	    CartItem selectByMemberIdAndSku(Integer memberId, String sku);

	    int update(CartItem cartItem);
}