package web.checkout.dao.impl;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import web.checkout.dao.OrderDao;
import web.checkout.vo.OrderPaymentInfo;
import web.checkout.vo.Orders;
import web.checkout.vo.ResultDTO;

/**
 * 訂單資料存取實作：以 Hibernate Session 操作 orders 資料表。
 */
@Repository
public class OrderDaoImpl implements OrderDao {

	@PersistenceContext
	private Session session;

	/**
	 * 新增一筆訂單至資料庫。
	 *
	 * @param order 要儲存的訂單物件
	 */
	@Override
	public void save(Orders order) {
		session.persist(order);
	}

	/**
	 * 以訂單編號查詢付款相關資訊（付款狀態、總金額、交易號）。
	 *
	 * @param orderId 訂單編號
	 * @return 付款資訊；若訂單不存在則回傳 null
	 */
	@Override
	public OrderPaymentInfo selectPaymentInfoByOrderId(int orderId) {

		Orders o = session.get(Orders.class, orderId);
		if (o == null) {
			return null;
		}

		// 將 Orders 實體拆解為輕量的 OrderPaymentInfo DTO
		OrderPaymentInfo info = new OrderPaymentInfo();
		info.setOrderId(o.getOrderId());
		info.setPaymentStatus(o.getPaymentStatus());

		// totalAmount 可能為 null，預設填 0 避免 NullPointerException
		if (o.getTotalAmount() != null) {
			info.setTotalAmount(o.getTotalAmount().intValue());
		} else {
			info.setTotalAmount(0);
		}

		info.setTransactionId(o.getTransactionId());

		return info;
	}

	/**
	 * 更新指定訂單的交易號（transaction_id）。
	 *
	 * @param orderId       訂單編號
	 * @param transactionId 要寫入的交易號
	 * @return 實際更新的列數
	 */
	@Override
	public int updateTransactionId(int orderId, String transactionId) {

		// 以 HQL UPDATE 直接更新單一欄位，避免載入整個 Orders 實體
		String hql = "UPDATE Orders o SET o.transactionId = :txid WHERE o.orderId = :oid";

		Query<?> q = session.createQuery(hql);
		q.setParameter("txid", transactionId);
		q.setParameter("oid", orderId);

		return q.executeUpdate();
	}

	/**
	 * 檢查指定交易號是否已存在於資料庫，用於確保唯一性。
	 *
	 * @param transactionId 要檢查的交易號
	 * @return 若已存在則回傳 true
	 */
	@Override
	public boolean existsTransactionId(String transactionId) {

		// 只查詢常數 1，命中即代表存在，效能優於 COUNT(*)
		String hql = "SELECT 1 FROM Orders o WHERE o.transactionId = :txid";

		Integer one = session.createQuery(hql, Integer.class)
				.setParameter("txid", transactionId)
				.setMaxResults(1)
				.uniqueResult();

		return one != null;
	}

	/**
	 * 以交易號查詢對應的訂單。
	 *
	 * @param transactionId ECPay 回傳的交易號
	 * @return 對應訂單；若不存在則回傳 null
	 */
	@Override
	public Orders selectByTransactionId(String transactionId) {

		// 交易號理論上唯一，取第一筆即可
		String hql = "FROM Orders o WHERE o.transactionId = :txid";

		return session.createQuery(hql, Orders.class)
				.setParameter("txid", transactionId)
				.setMaxResults(1)
				.uniqueResult();
	}

	/**
	 * 以訂單編號查詢付款結果，供前端判斷付款成功或失敗。
	 *
	 * @param orderId 訂單編號
	 * @return 付款結果 DTO；若訂單不存在則回傳 null
	 */
	@Override
	public ResultDTO selectByOrderId(int orderId) {

		Orders o = session.get(Orders.class, orderId);
		if (o == null) {
			return null;
		}

		// 將 Orders 實體轉換為前端所需的 ResultDTO
		return new ResultDTO(o.getOrderId(), o.getPaymentStatus(), o.getTotalAmount(), o.getPaymentTime(),
				o.getPaymentMethod(), o.getFailureReason());
	}
}
