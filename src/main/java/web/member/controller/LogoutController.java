package web.member.controller;

import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;

@RestController
public class LogoutController {

	@PostMapping("/api/logout")
	public ApiResponse<Void> logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
		return new ApiResponse<>(true, "登出成功!", null);
	}
}
