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

//查詢訂單狀態(提供前端判斷付款成功或失敗)
@Service
@Transactional(readOnly = true)
public class ResultServiceImpl implements ResultService {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private OrderItemDao orderItemDao;

	@Override
    public ResultDTO getOrder(int orderId) {
        return orderDao.selectByOrderId(orderId);
    }

	@Override
	public List<OrderItem> getOrderItems(int orderId) {
		return orderItemDao.selectByOrderId(orderId);
	}
}