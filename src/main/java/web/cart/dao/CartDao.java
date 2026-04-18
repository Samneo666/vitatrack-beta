package web.cart.dao;

import java.util.List;

import web.checkout.vo.CartItem;
import web.checkout.vo.CartRow;


public interface CartDao {
//    void addItem(int userId, int productId, int quantity);

	int insert(CartItem cartItem);

	int updateById(CartItem cartItem);
	
	int deleteByIDAndSkus(Integer memberId , List<String> skus);

	CartItem SelectByMemberIdAndSku(Integer memberId, String sku);
	
	List<CartItem> selectBySkus(Integer memberId,List<String> skus);
	 //查詢指定會員尚未結帳的購物車明細。
	List<CartRow> findOpenCartByMemberId(int memberId);

	int deleteCartItems(int memberId, List<String> skus);
	
}
