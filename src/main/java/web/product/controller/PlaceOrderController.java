package web.product.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.member.vo.Member;
import web.product.dto.OrderCreationResponse;
import web.product.service.PlaceOrderService;

@RestController
public class PlaceOrderController {
	@Autowired
    private PlaceOrderService placeOrderService;
	
	 // 送出結帳，建立訂單
    @PostMapping("/placeOrder")
    public OrderCreationResponse checkout(@SessionAttribute (required =false) Member member) {
        return placeOrderService.checkout(member.getMemberId());
    }
}