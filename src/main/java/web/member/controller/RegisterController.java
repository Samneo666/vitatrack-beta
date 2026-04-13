package web.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;
import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class RegisterController {
	@Autowired
	private MemberService memberService;

	@PostMapping("/api/register")
	public ApiResponse<Void> register(@RequestBody Member member) {
		memberService.register(member);
		return new ApiResponse<>(true, "註冊成功", null);
	}

}
