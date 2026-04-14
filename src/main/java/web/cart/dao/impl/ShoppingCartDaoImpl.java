package web.cart.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.cart.dao.CartDao;
import web.checkout.vo.CartItem;
import web.checkout.vo.CartRow;

@Repository
public class ShoppingCartDaoImpl implements CartDao {

	@PersistenceContext
	private Session session;

	@Override
	public int insert(CartItem cartItem) {
		session.persist(cartItem);
		return 1;
	}

	@Override
	public int updateById(CartItem cartItem) {
		session.merge(cartItem);
		return 1;
	}

	@Override
	public CartItem SelectByMemberIdAndSku(Integer memberId, String sku) {
		final String hql = "FROM CartItem c WHERE c.memberId = :memberId AND c.sku = :sku AND c.orderId IS NULL";
		return session.createQuery(hql, CartItem.class).setParameter("memberId", memberId).setParameter("sku", sku)
				.uniqueResult();

	}

	

	@Override
	public List<CartItem> selectBySkus(Integer memberId, List<String> skus) {
		final String hql = " FROM CartItem c WHERE c.memberId = :memberId AND c.sku IN (:skus) ";
		return session.createQuery(hql, CartItem.class).setParameter("memberId", memberId)
				.setParameterList("skus", skus).getResultList();
	}

	/**
	 * 查詢指定會員尚未結帳的購物車明細（JOIN product 取得商品名稱與單價）。
	 *
	 * @param memberId 會員編號
	 * @return 購物車明細清單；若購物車為空則回傳空清單
	 */
    // 以 HQL 聯結 product 取得完整購物車明細，並 LEFT JOIN ProductImage 取主圖（isMain = true）
    // 僅取尚未綁定訂單的品項
	@Override
	public List<CartRow> findOpenCartByMemberId(int memberId) {

		// 以 HQL 聯結 product 取得完整購物車明細，並 LEFT JOIN ProductImage 取主圖（isMain = true）
		// 僅取尚未綁定訂單的品項
		String hql = "SELECT new web.checkout.vo.CartRow("
				+ "ci.cartItemId, p.sku, p.productName, p.price, ci.quantity, pi.imageUrl) " + "FROM CartItem ci "
				+ "JOIN ci.product p " + "LEFT JOIN ProductImage pi " + "  ON pi.sku = p.sku AND pi.isMain = true "
				+ "WHERE ci.memberId = :mid " + "AND ci.orderId IS NULL";

		Query<CartRow> query = session.createQuery(hql, CartRow.class);
		query.setParameter("mid", memberId);

		return query.getResultList();
	}

	@Override
	public int deleteCartItems(int memberId, List<String> skus) {
		String hql = "DELETE FROM CartItem c WHERE c.memberId = :memberId AND c.sku IN (:skus) AND c.orderId IS NULL";
		return session.createQuery(hql).setParameter("memberId", memberId).setParameterList("skus", skus)
				.executeUpdate();
	}

    @Override
    public int deleteByIDAndSkus(Integer memberId, List<String> skus) {
        // 使用 IN 子句來處理傳入的 List<String>
        final String hql = "DELETE FROM CartItem c WHERE c.memberId = :memberId AND c.sku IN (:skus) ";
        return session.createQuery(hql).setParameter("memberId", memberId).setParameterList("skus", skus)
                .executeUpdate();
    }

	

}
