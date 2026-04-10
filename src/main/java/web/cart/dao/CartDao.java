package web.cart.dao;

import java.util.List;

import web.checkout.vo.CartItem;


public interface CartDao {
//    void addItem(int userId, int productId, int quantity);

	int insert(CartItem cartItem);

	int updateById(CartItem cartItem);
	
	int deleteByIDAndSkus(Integer memberId , List<String> skus);

	CartItem SelectByMemberIdAndSku(Integer memberId, String sku);
	
	List<CartItem> selectBySkus(Integer memberId,List<String> skus);
	
}
