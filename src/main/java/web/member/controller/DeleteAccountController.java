package web.member.controller;

import java.util.Map;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.member.dto.DeleteMemberRequest;
import core.exception.BusinessException;
import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class DeleteAccountController {
	@Autowired
	private MemberService memberService;

	@PostMapping("/api/deleteAccount")
	public Map<String, Object> delete(@SessionAttribute(required = false)Member member, @RequestBody DeleteMemberRequest dto, HttpSession session) {
		
		if (member == null) {
			throw new BusinessException("您尚未登入，請先登入會員。");
		}
		memberService.remove(member.getEmail(),dto.getPassword());
		if (session != null) {
			session.invalidate();
		}
		return Map.of("success", true, "message", "帳號已成功註銷");
	}

}
