package web.checkout.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.checkout.dao.OrderDao;
import web.checkout.service.ResultService;
import web.checkout.vo.ResultDTO;

//查詢訂單狀態(提供前端判斷付款成功或失敗)
@Service
@Transactional(readOnly = true)
public class ResultServiceImpl implements ResultService {
	
	private final OrderDao orderDao;
	
	public ResultServiceImpl(OrderDao orderDao) {
		this.orderDao = orderDao;
	}

	@Override
    public ResultDTO getOrder(int orderId) {


        return orderDao.selectByOrderId( orderId);
    }
}