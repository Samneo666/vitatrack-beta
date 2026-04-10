package web.product.dao.impl;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;

import org.springframework.stereotype.Repository;
import web.product.dao.CartItemDao;
import web.product.vo.CartItem;

@Repository
public class CartItemDaoImpl implements CartItemDao {

	@PersistenceContext
	private Session session;

	@Override
	public int insert(CartItem cartItem) {

		session.save(cartItem);

		return 1;
	}

	@Override
	public CartItem selectByMemberIdAndSku(Integer memberId, String sku) {
		
			return session
					.createQuery("FROM ProductCartItem WHERE memberId = :memberId AND sku = :sku", CartItem.class)
					.setParameter("memberId", memberId).setParameter("sku", sku).uniqueResult();
	}

	@Override
	public int update(CartItem cartItem) {	
		
			session.update(cartItem);
			
			return 1;
		
	}
}