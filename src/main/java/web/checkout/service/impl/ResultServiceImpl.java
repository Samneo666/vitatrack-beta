package web.checkout.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.dao.OrderItemDao;
import web.checkout.service.ResultService;
import web.checkout.vo.OrderItem;
import web.checkout.vo.ResultDTO;

/**
 * 訂單查詢服務實作：查詢付款結果與訂單商品明細。
 */
@Service
@Transactional(readOnly = true)
public class ResultServiceImpl implements ResultService {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private OrderItemDao orderItemDao;

	/**
	 * 以訂單編號查詢付款結果，供前端判斷付款成功或失敗。
	 *
	 * @param orderId 訂單編號
	 * @return 付款結果 DTO；若訂單不存在則回傳 null
	 */
	@Override
	public ResultDTO getOrder(int orderId) {
		return orderDao.selectByOrderId(orderId);
	}

	/**
	 * 查詢指定訂單的商品明細清單，供前端訂單確認頁顯示。
	 *
	 * @param orderId 訂單編號
	 * @return 訂單明細清單
	 */
	@Override
	public List<OrderItem> getOrderItems(int orderId) {
		return orderItemDao.selectByOrderId(orderId);
	}
}
