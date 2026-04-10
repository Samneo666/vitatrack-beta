package web.cart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.cart.dto.AddToCartItemRequest;
import web.cart.dto.AddToCartItemResponse;
import core.dto.ApiResponse;
import web.cart.service.CartService;
import core.exception.BusinessException;
import web.member.vo.Member;

@Controller
public class AddToCartController {
	@Autowired
	private CartService cartService;

	@PostMapping("/api/addToCart")
	@ResponseBody
	public ApiResponse<AddToCartItemResponse> addToCart(@RequestBody AddToCartItemRequest dto, @SessionAttribute(required = false) Member member) {

		
		if (member == null) {
			throw new BusinessException("請先登入會員!");
		}
		Integer loginMemberId = member.getMemberId();

		AddToCartItemResponse result = cartService.addToCart(loginMemberId, dto.getSku(), dto.getQuantity());

		return new ApiResponse<AddToCartItemResponse>(true, "加入成功", result);

	}

}
