package web.checkout.service;

import web.checkout.vo.EcpayCheckoutPayload;

public interface PaymentService {

	// 開關 Session + 啟動串金流（choosePayment: ECPay 付款方式，例如 "Credit"、"ATM"、"ALL"）
	EcpayCheckoutPayload createEcpayCheckout(int orderId, String choosePayment);
}
