package web.checkout.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.checkout.dao.CartDao;
import web.checkout.vo.CartRow;

@Repository
public class CartDaoImpl implements CartDao {

	@PersistenceContext
	private Session session;

	// 查看尚未結帳的購物車
	@Override
	public List<CartRow> findOpenCartByMemberId(int memberId) {

		String hql = "SELECT new web.checkout.vo.CartRow("
				+ "  ci.cartItemId, p.sku, p.productName, p.price, ci.quantity" + ") "
				+ "FROM CartItem ci "
				+ "JOIN ci.product p "
				+ "WHERE ci.memberId = :mid ";

		Query<CartRow> query = session.createQuery(hql, CartRow.class);
		query.setParameter("mid", memberId);

		return query.getResultList();
	}

	// 更新購物車的 order_id
	@Override
	public int attachCartItemsToOrder(int orderId, List<CartRow> cartRows) {

		// 1.取出購物車的 cart_item_id
		List<Integer> ids = new ArrayList<>();
		for (CartRow row : cartRows) {
			ids.add(row.getCartItemId());
		}

		// 2.更新購物車的 order_id
		String hql = "UPDATE CartItem ci SET ci.orderId = :oid WHERE ci.cartItemId IN (:ids)";

		Query query = session.createQuery(hql);
		query.setParameter("oid", orderId);
		query.setParameterList("ids", ids);

		return query.executeUpdate();
	}
}
