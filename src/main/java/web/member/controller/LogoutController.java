package web.member.controller;

import java.util.Map;


import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

	@PostMapping("/api/logout")
	public Map<String, Object> logout( HttpSession session ) {
		
		if (session != null) {
			session.invalidate();
		}
		return Map.of("success", true, "message", "登出成功!");

	}
}
