package web.checkout.dao;

import java.util.List;

import web.checkout.vo.CartRow;

public interface CartDao {

	// 查看尚未結帳的購物車
	List<CartRow> findOpenCartByMemberId(int memberId);

	// 更新cart_item 的 order_id
	int attachCartItemsToOrder(int orderId, List<CartRow> cartRows);
}
