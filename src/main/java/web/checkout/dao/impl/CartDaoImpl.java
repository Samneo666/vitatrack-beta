package web.checkout.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.checkout.dao.CartDao;
import web.checkout.vo.CartRow;

@Repository
public class CartDaoImpl implements CartDao {

	private final SessionFactory sessionFactory;

	public CartDaoImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	// 查看尚未結帳的購物車
	@Override
	public List<CartRow> findOpenCartByMemberId(int memberId) {

		Session session = sessionFactory.getCurrentSession();

		String hql = "SELECT new web.checkout.vo.CartRow("
				+ "  ci.cartItemId, p.sku, p.productName, p.price, ci.quantity" + ") " + "FROM CartItem ci "
				+ "JOIN ci.product p " + "WHERE ci.memberId = :mid ";

		Query<CartRow> query = session.createQuery(hql, CartRow.class);

		query.setParameter("mid", memberId);

		List<CartRow> result = query.getResultList();

		return result;
	}

	// 更新購物車的 order_id
	@Override
	public int attachCartItemsToOrder(int orderId, List<CartRow> cartRows) {

		Session session = sessionFactory.getCurrentSession();

		// 1.取出購物車的 cart_item_id
		List<Integer> ids = new ArrayList<>();

		for (CartRow row : cartRows) {

			Integer id = row.getCartItemId();

			ids.add(id);
		}

		// 2.更新購物車的 order_id
		String hql = "UPDATE CartItem ci SET ci.orderId = :oid WHERE ci.cartItemId IN (:ids)";

		Query query = session.createQuery(hql);

		query.setParameter("oid", orderId);

		query.setParameterList("ids", ids);

		int updatedCount = query.executeUpdate();

		return updatedCount;
	}
}