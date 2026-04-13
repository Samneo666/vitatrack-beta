package web.member.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.member.dto.ChangePasswordRequest;
import core.exception.BusinessException;
import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class ChangePasswordController {

	@Autowired
	private MemberService memberService;

	@PostMapping("/api/changePassword")
	public ApiResponse<Void> changePassword(@SessionAttribute(required = false) Member member, HttpSession session, @RequestBody ChangePasswordRequest dto) {
		if (member == null) {
			throw new BusinessException("您尚未登入，請先登入會員。");
		}
		memberService.changePassword(member.getEmail(), dto.getOldPassword(), dto.getNewPassword(), dto.getConfirmPassword());
		session.invalidate();
		return new ApiResponse<>(true, "密碼更新成功，請重新登入", null);
	}

}
