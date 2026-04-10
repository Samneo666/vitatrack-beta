package web.product.service;

import web.product.dto.OrderCreationResponse;

public interface PlaceOrderService {
	 OrderCreationResponse checkout(int memberId);
}
