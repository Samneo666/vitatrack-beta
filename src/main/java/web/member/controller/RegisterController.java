package web.member.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class RegisterController {
	@Autowired
	private MemberService memberService;

	@PostMapping("/api/register")
	public Map<String, Object> register(@RequestBody Member member) {
		memberService.register(member);
		return Map.of("success", true, "message", "註冊成功");
	}

}
