package web.checkout.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import web.checkout.dao.OrderItemDao;
import web.checkout.vo.OrderItem;

@Repository
public class OrderItemDaoImpl implements OrderItemDao {
	
	private final SessionFactory sessionFactory;

	public OrderItemDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	// 新增一筆訂單明細
    @Override
    public void save(OrderItem item) {
    	
    	Session session = sessionFactory.getCurrentSession();
    	
        session.persist(item);
    }

    // 查該訂單的商品名稱清單
    @Override
    public List<String> selectProductNamesByOrderId(int orderId) {
    	
    	Session session = sessionFactory.getCurrentSession();

        // 查 OrderItem entity 欄位 productName
        String hql = "SELECT oi.productName FROM OrderItem oi WHERE oi.order.orderId = :oid ORDER BY oi.itemId";

        return session.createQuery(hql, String.class)
                .setParameter("oid", orderId)
                .getResultList();
    }
}