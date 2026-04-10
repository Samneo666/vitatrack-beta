package web.member.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class LoginController {

	@Autowired
	private MemberService memberService;

	@PostMapping("/api/login")
	public Map<String, Object> login(@RequestBody Member member, HttpServletRequest request) {
		Member loginMember = memberService.login(member);
		//資安考量
		if (request.getSession(false) != null) {
			request.changeSessionId();
			}
		HttpSession session = request.getSession();
		session.setAttribute("member", loginMember);
		return Map.of("success", true, "message", "登入成功!" );
		
	}

}
