package web.member.controller;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.member.service.MemberService;
import web.member.vo.Member;
import web.member_admin.dto.PageResultResponse;
import web.checkout.vo.Orders;

@RestController
public class MyOrderController {

	@Autowired
	private MemberService memberService;

	// 查看訂單資料
	@GetMapping("/api/myOrder")
	public ApiResponse<PageResultResponse<Orders>> getMyOrder(@SessionAttribute(name = "member", required = false) Member member, @RequestParam("page") int page) {
		int size = 10;
		PageResultResponse<Orders> orderDb = memberService.viewMyOrder(member, page, size);
		return new ApiResponse<>(true, "success", orderDb);
	}

}
