package web.member_admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;
import web.member_admin.dto.MemberListResponse;
import web.member_admin.dto.PageResultResponse;
import web.member_admin.service.MemberAdminService;

@RestController
public class MemberListController {
	@Autowired
	private MemberAdminService memberAdminService;

	// @param page 起始頁碼（預設從1開始)
	// @param size 每頁筆數（預設10筆）
	// 查看會員資料(列表)
	@GetMapping("/api/memberList")
	public ApiResponse<PageResultResponse<MemberListResponse>> memberList(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		return new ApiResponse<>(true, "查詢成功", memberAdminService.getMemberInfo(page, size));

	}
}
