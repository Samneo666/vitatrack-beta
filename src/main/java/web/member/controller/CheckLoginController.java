package web.member.controller;

import java.util.Map;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import web.member.vo.Member;

@RestController
public class CheckLoginController {

	// MvcConfig excludePathPatterns 排除，不會被攔截器擋
	@GetMapping("/api/loginCheck")
	public Map<String, Object> checkLogin(@SessionAttribute(required = false)Member member) {
		boolean loggedIn = ( member != null);	
		return Map.of("loggedIn", loggedIn);
	}
}
