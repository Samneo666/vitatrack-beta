package web.checkout.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.checkout.dao.OrderItemDao;
import web.checkout.vo.OrderItem;

/**
 * 訂單明細資料存取實作：以 Hibernate Session 操作 order_item 資料表。
 */
@Repository
public class OrderItemDaoImpl implements OrderItemDao {

	@PersistenceContext
	private Session session;

	/**
	 * 新增一筆訂單明細至資料庫。
	 *
	 * @param item 要儲存的訂單明細物件
	 */
	@Override
	public void save(OrderItem item) {
		session.persist(item);
	}

	/**
	 * 查詢指定訂單的所有商品名稱，供組成 ECPay ItemName 欄位使用。
	 *
	 * @param orderId 訂單編號
	 * @return 商品名稱清單，依 itemId 排序
	 */
	@Override
	public List<String> selectProductNamesByOrderId(int orderId) {

		// 只取商品名稱，避免載入完整 OrderItem 物件
		String hql = "SELECT oi.productName FROM OrderItem oi WHERE oi.order.orderId = :oid ORDER BY oi.itemId";

		return session.createQuery(hql, String.class)
				.setParameter("oid", orderId)
				.getResultList();
	}

	/**
	 * 查詢指定訂單的完整明細清單，供前端訂單確認頁顯示。
	 *
	 * @param orderId 訂單編號
	 * @return 訂單明細清單，依 itemId 排序
	 */
	@Override
	public List<OrderItem> selectByOrderId(int orderId) {

		// 取完整 OrderItem 實體，前端需要單價、數量、小計等欄位
		String hql = "FROM OrderItem oi WHERE oi.order.orderId = :oid ORDER BY oi.itemId";

		return session.createQuery(hql, OrderItem.class)
				.setParameter("oid", orderId)
				.getResultList();
	}
}
