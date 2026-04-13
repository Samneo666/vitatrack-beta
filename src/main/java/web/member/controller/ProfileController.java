package web.member.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.member.service.MemberService;
import web.member.vo.Member;
import core.dto.ApiResponse;
import web.member.dto.MemberProfileResponse;
import web.member.dto.UpdateMemberRequest;
import core.exception.BusinessException;


@RestController
public class ProfileController {

	@Autowired
	private MemberService memberService;

	// 查看會員
	@GetMapping("/api/getProfile")
	public ApiResponse<MemberProfileResponse> profile(@SessionAttribute(required = false)Member member) {

		if (member == null) {
	        throw new BusinessException("您尚未登入，請先登入會員。");
	    }
		Member profileMember = memberService.profile(member);
		
		return new ApiResponse<>(true , "success" , new MemberProfileResponse(profileMember));
	}

	// 修改會員
	@PostMapping("/api/updateProfile")
	public ApiResponse<Void> updateProfile(@RequestBody UpdateMemberRequest memberDTO, @SessionAttribute(required = false) Member member, HttpSession session) {
		if (member == null) {
	        throw new BusinessException("您尚未登入，請先登入會員。");
	    }
		Member updatedMember = memberService.updateProfile(member, memberDTO);
		session.setAttribute("member", updatedMember);
		return new ApiResponse<>(true, "會員資訊更新成功", null);
	}

}
