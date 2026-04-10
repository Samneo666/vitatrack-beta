package web.cart.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.cart.dao.CartDao;
import web.checkout.vo.CartItem;

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
		final String hql = "FROM CartItem c WHERE c.memberId = :memberId AND c.sku = :sku ";
		return session.createQuery(hql, CartItem.class).setParameter("memberId", memberId).setParameter("sku", sku)
				.uniqueResult();

	}

	@Override
	public int deleteByIDAndSkus(Integer memberId, List<String> skus) {
		// 使用 IN 子句來處理傳入的 List<String>
		final String hql = "DELETE FROM CartItem c WHERE c.memberId = :memberId AND c.sku IN (:skus) ";
		return session.createQuery(hql).setParameter("memberId", memberId).setParameterList("skus", skus)
				.executeUpdate();
	}

	@Override
	public List<CartItem> selectBySkus(Integer memberId, List<String> skus) {
		final String hql = " FROM CartItem c WHERE c.memberId = :memberId AND c.sku IN (:skus) ";
		return session.createQuery(hql, CartItem.class).setParameter("memberId", memberId)
				.setParameterList("skus", skus).getResultList();
	}

}
