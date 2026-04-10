package web.member_admin.controller;



import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminLogOutController {

	@PostMapping("/api/adminLogout")
	public Map<String, Object> adminLogOut(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return Map.of("success", true, "message", "登出成功!");

	}
}
