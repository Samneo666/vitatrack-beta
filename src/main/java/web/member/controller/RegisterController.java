package web.member.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import core.dto.ApiResponse;
import web.member.service.MemberService;
import web.member.vo.Member;

@RestController
public class RegisterController {
	@Autowired
	private MemberService memberService;
	
	@GetMapping("/api/check-email")
	public ResponseEntity<?> checkEmail(@RequestParam String email) {
	    
	    boolean exists = memberService.isEmailExists(email);
	    return ResponseEntity.ok(Map.of("exists", exists));
	}
	
	@PostMapping("/api/register")
	public ApiResponse<Void> register(@RequestBody Member member) {
		memberService.register(member);
		return new ApiResponse<>(true, "註冊成功", null);
	}

}
