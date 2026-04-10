package web.checkout.service;

import web.checkout.vo.EcpayCheckoutPayload;

public interface PaymentService {

	// 開關 Session + 啟動串金流
	EcpayCheckoutPayload createEcpayCheckout(int orderId);
}
