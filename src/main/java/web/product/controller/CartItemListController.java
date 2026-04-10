package web.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.checkout.service.CheckoutService;
import web.checkout.vo.CartRow;
import web.member.vo.Member;

@RestController
public class CartItemListController {

	@Autowired
	private CheckoutService checkoutService;

	@GetMapping("/api/getCartItem")
	public List<CartRow> getCartItem(@SessionAttribute(required = false) Member member) {

		return checkoutService.getCheckoutCart(member.getMemberId());

	}

}
