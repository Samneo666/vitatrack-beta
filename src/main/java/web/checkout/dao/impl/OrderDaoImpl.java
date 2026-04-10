package web.checkout.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.checkout.dao.OrderDao;
import web.checkout.vo.OrderPaymentInfo;
import web.checkout.vo.Orders;
import web.checkout.vo.ResultDTO;

@Repository
public class OrderDaoImpl implements OrderDao {
	
	private final SessionFactory sessionFactory;

	public OrderDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	// 新增訂單
	@Override
	public void save(Orders order) {
		Session session = sessionFactory.getCurrentSession();
		session.persist(order);
	}

	// 查訂單
	@Override
	public OrderPaymentInfo selectPaymentInfoByOrderId(int orderId) {
		
		Session session = sessionFactory.getCurrentSession();

		Orders o = session.get(Orders.class, orderId);
		if (o == null)
			return null;

		OrderPaymentInfo info = new OrderPaymentInfo();
		info.setOrderId(o.getOrderId());
		info.setPaymentStatus(o.getPaymentStatus());

		if (o.getTotalAmount() != null) {
			info.setTotalAmount(o.getTotalAmount().intValue());
		} else {
			info.setTotalAmount(0);
		}

		info.setTransactionId(o.getTransactionId());

		return info;
	}

	// 產生一組唯一的 transaction_id
	@Override
	public int updateTransactionId(int orderId, String transactionId) {
		
		Session session = sessionFactory.getCurrentSession();

		String hql = "UPDATE Orders o SET o.transactionId = :txid WHERE o.orderId = :oid";

		Query<?> q = session.createQuery(hql);
		q.setParameter("txid", transactionId);
		q.setParameter("oid", orderId);

		return q.executeUpdate();
	}

	// 檢查 transactionId 是否重複
	@Override
	public boolean existsTransactionId(String transactionId) {
		
		Session session = sessionFactory.getCurrentSession();

		String hql = "SELECT 1 FROM Orders o WHERE o.transactionId = :txid";

		Integer one = session.createQuery(hql, Integer.class).setParameter("txid", transactionId).setMaxResults(1)
				.uniqueResult();

		return one != null;
	}

	// 用 transaction_id 查訂單是否存在
	@Override
	public Orders selectByTransactionId(String transactionId) {
		
		Session session = sessionFactory.getCurrentSession();

		String hql = "FROM Orders o WHERE o.transactionId = :txid";

		return session.createQuery(hql, Orders.class).setParameter("txid", transactionId).setMaxResults(1)
				.uniqueResult();
	}

	// 查詢訂單狀態(提供前端判斷付款成功或失敗)

	@Override
	public ResultDTO selectByOrderId(int orderId) {
		
		Session session = sessionFactory.getCurrentSession();

		Orders o = session.get(Orders.class, orderId);

		if (o == null) {
			return null;
		}

		return new ResultDTO(o.getOrderId(), o.getPaymentStatus(), o.getTotalAmount(), o.getPaymentTime(),
				o.getPaymentMethod(), o.getFailureReason());
	}
}
