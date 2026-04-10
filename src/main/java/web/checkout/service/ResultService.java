package web.checkout.service;

import web.checkout.vo.ResultDTO;

//查詢訂單狀態(提供前端判斷付款成功或失敗)
public interface ResultService {
	ResultDTO getOrder(int orderId);
}
