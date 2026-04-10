package web.member.controller;


import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import web.member.service.PasswordResetTokensService;

import web.member.dto.ForgetPasswordRequest;


@RestController
public class ForgetPasswordController  {
	
	@Autowired
	private PasswordResetTokensService passwordResetTokensService;
	
	@PostMapping("/api/forgetPassword")
	public 	Map<String, Object >forgetPassword(@RequestBody ForgetPasswordRequest DTO ) {	
		passwordResetTokensService.createResetToken(DTO.getEmail());
		return Map.of("success", true,"message", "已寄出密碼重設連結");
	}
	
}
