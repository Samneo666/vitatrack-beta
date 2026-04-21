package web.product.service;

import web.product.dto.OrderCreationResponse;
import web.product.dto.PlaceOrderRequest;

public interface PlaceOrderService {
    OrderCreationResponse checkout(int memberId, PlaceOrderRequest request);
}
