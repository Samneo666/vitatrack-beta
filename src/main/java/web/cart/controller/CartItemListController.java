package web.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.checkout.service.CheckoutService;
import web.checkout.vo.CartRow;
import web.member.vo.Member;

@RestController
public class CartItemListController {

	@Autowired
	private CheckoutService checkoutService;

	@GetMapping("/api/getCartItem")
	public ApiResponse<List<CartRow>> getCartItem(@SessionAttribute(required = false) Member member) {

		if (member == null) {
			return new ApiResponse<>(false, "請先登入會員!", null);
		}

		List<CartRow> items = checkoutService.getCheckoutCart(member.getMemberId());
		return new ApiResponse<>(true, "success", items);

	}

}
