package web.member_admin.controller;



import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;

@RestController
public class AdminLogOutController {

	@PostMapping("/api/adminLogout")
	public ApiResponse<Void> adminLogOut(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return new ApiResponse<Void>( true,"登出成功!",null);
	}
}
