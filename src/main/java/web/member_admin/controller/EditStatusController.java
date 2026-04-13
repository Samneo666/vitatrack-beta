package web.member_admin.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import core.dto.ApiResponse;
import web.member.dto.EditMemberStatusRequest;

import web.member.vo.Admin;
import web.member_admin.service.MemberAdminService;

@Controller
public class EditStatusController {
	@Autowired
	private MemberAdminService memberAdminService;

	// 編輯會員狀態
	@PostMapping("/api/editStatus")
	@ResponseBody
	public ApiResponse<Void> editStatus(@SessionAttribute("admin") Admin admin,
			@RequestBody EditMemberStatusRequest editMember) {

		memberAdminService.editMemberStatus(admin, editMember);
		return new ApiResponse<Void>( true,"會員狀態更新成功!",null);
	}

}
