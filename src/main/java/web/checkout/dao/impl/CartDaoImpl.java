package web.checkout.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.checkout.dao.CartDao;
import web.checkout.vo.CartRow;

/**
 * 購物車資料存取實作：以 Hibernate Session 操作 cart_item 資料表。
 */
@Repository
public class CartDaoImpl implements CartDao {

	@PersistenceContext
	private Session session;

	/**
	 * 查詢指定會員尚未結帳的購物車明細（JOIN product 取得商品名稱與單價）。
	 *
	 * @param memberId 會員編號
	 * @return 購物車明細清單；若購物車為空則回傳空清單
	 */
	@Override
	public List<CartRow> findOpenCartByMemberId(int memberId) {

		// 以 HQL 聯結 product 取得完整購物車明細，並 LEFT JOIN ProductImage 取主圖（isMain = true）
		// 僅取尚未綁定訂單的品項
		String hql = "SELECT new web.checkout.vo.CartRow("
				+ "ci.cartItemId, p.sku, p.productName, p.price, ci.quantity, pi.imageUrl) "
				+ "FROM CartItem ci "
				+ "JOIN ci.product p "
				+ "LEFT JOIN ProductImage pi "
				+ "  ON pi.sku = p.sku AND pi.isMain = true "
				+ "WHERE ci.memberId = :mid "
				+ "AND ci.orderId IS NULL";

		Query<CartRow> query = session.createQuery(hql, CartRow.class);
		query.setParameter("mid", memberId);

		return query.getResultList();
	}

	/**
	 * 將指定的購物車品項綁定至已建立的訂單（更新 cart_item.order_id）。
	 *
	 * @param orderId  要綁定的訂單編號
	 * @param cartRows 要更新的購物車品項清單
	 * @return 實際更新的列數
	 */
	@Override
	public int attachCartItemsToOrder(int orderId, List<CartRow> cartRows) {

		// 收集所有需要更新的 cart_item_id
		List<Integer> ids = new ArrayList<>();
		for (CartRow row : cartRows) {
			ids.add(row.getCartItemId());
		}

		// 批次更新 cart_item.order_id
		String hql = "UPDATE CartItem ci SET ci.orderId = :oid WHERE ci.cartItemId IN (:ids)";

		return session.createQuery(hql)
		.setParameter("oid", orderId)
		.setParameterList("ids", ids)
		.executeUpdate();
	}
}
