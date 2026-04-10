package web.checkout.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.checkout.dao.OrderItemDao;
import web.checkout.vo.OrderItem;

@Repository
public class OrderItemDaoImpl implements OrderItemDao {

	@PersistenceContext
	private Session session;

	// 新增一筆訂單明細
	@Override
	public void save(OrderItem item) {
		session.persist(item);
	}

	// 查該訂單的商品名稱清單（供 ECPay ItemName 使用）
	@Override
	public List<String> selectProductNamesByOrderId(int orderId) {

		String hql = "SELECT oi.productName FROM OrderItem oi WHERE oi.order.orderId = :oid ORDER BY oi.itemId";

		return session.createQuery(hql, String.class)
				.setParameter("oid", orderId)
				.getResultList();
	}

	// 查該訂單的完整明細清單（供前端訂單確認頁顯示）
	@Override
	public List<OrderItem> selectByOrderId(int orderId) {

		String hql = "FROM OrderItem oi WHERE oi.order.orderId = :oid ORDER BY oi.itemId";

		return session.createQuery(hql, OrderItem.class)
				.setParameter("oid", orderId)
				.getResultList();
	}
}
