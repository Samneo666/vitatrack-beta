package web.cart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.cart.dto.UpdateCartItemRequest;
import web.cart.dto.UpdateCartItemResponse;
import web.cart.service.CartService;
import core.exception.BusinessException;
import web.member.vo.Member;

@Controller
public class UpdateCartItemController {
	@Autowired
	private CartService cartService;

	@PostMapping("/api/updateCartItem")
	@ResponseBody
	public ApiResponse<UpdateCartItemResponse> updateCartItem(@RequestBody UpdateCartItemRequest dto,
			@SessionAttribute(required = false) Member member) {

		
		if (member == null) {
			throw new BusinessException("請先登入會員!");
		}
		Integer loginMemberId = member.getMemberId();

		UpdateCartItemResponse result = cartService.updateQuantity(loginMemberId, dto.getSku(), dto.getQuantity());

		return new ApiResponse<UpdateCartItemResponse>(true, "更新成功", result);

	}

}
